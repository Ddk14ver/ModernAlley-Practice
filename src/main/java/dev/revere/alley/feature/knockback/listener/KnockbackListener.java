package dev.revere.alley.feature.knockback.listener;

import com.destroystokyo.paper.event.player.PlayerAttackEntityCooldownResetEvent;
import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.feature.match.MatchService;
import dev.revere.alley.feature.match.combat.legacy.LegacyCombatService;
import dev.revere.alley.feature.match.combat.legacy.LegacyProjectileData;
import dev.revere.alley.feature.match.internal.MatchServiceImpl;
import dev.revere.alley.feature.knockback.KnockbackManager;
import dev.revere.alley.feature.knockback.KnockbackProfile;
import dev.revere.alley.feature.knockback.data.PlayerKnockbackData;
import dev.revere.alley.feature.knockback.packet.MisplaceHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 04/07/2026
 *
 * Replaces vanilla knockback with profile-based computation.
 * Direction-based profile knockback with an optional modern downward stage.
 */
public class KnockbackListener implements Listener {
    private static final int LEGACY_HAZARD_IMMUNITY_TICKS = 10;

    private final KnockbackManager manager;
    private final Map<UUID, AttackCooldownSnapshot> attackCooldowns = new ConcurrentHashMap<>();

    public KnockbackListener(KnockbackManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAttackCooldownReset(PlayerAttackEntityCooldownResetEvent event) {
        this.attackCooldowns.put(event.getPlayer().getUniqueId(), new AttackCooldownSnapshot(
                event.getAttackedEntity().getUniqueId(),
                event.getCooledAttackStrength(),
                Bukkit.getCurrentTick()
        ));
    }

    // Track ground state
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent e) {
        if (e.getTo() == null) return;
        boolean onGround = e.getPlayer().isOnGround();
        PlayerKnockbackData data = manager.getPlayerData(e.getPlayer());
        data.setOnGround(onGround);
        if (onGround) data.setLastGroundY(e.getTo().getY());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBowShoot(EntityShootBowEvent event) {
        if (!(event.getProjectile() instanceof AbstractArrow arrow)) return;
        int punchLevel = event.getBow() == null ? 0 : event.getBow().getEnchantmentLevel(Enchantment.PUNCH);
        LegacyProjectileData.storeBowPunch(arrow, punchLevel);
    }

    /**
     * Bukkit admits a stronger hit during its native hurt window as delta damage.
     * Kit hit delay is a full hit interval, so it must reject that modern branch.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void enforceKitHitDelay(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) return;

        Entity source = event.getDamager();
        if (source instanceof EnderPearl pearl && !isLegacyPearlCombatHit(pearl, victim)) return;
        if (!isPlayerCombatSource(source)) return;

        PlayerKnockbackData data = manager.getPlayerData(victim);
        long now = manager.getCurrentTick();
        if (isHazardFrameActive(data)) {
            event.setCancelled(true);
            return;
        }

        int hitDelay = data.getConfiguredHitDelay();
        if (hitDelay <= 0 || event.getFinalDamage() <= 0.0) return;

        long lastHit = data.getLastAcceptedCombatHitTick();
        if (lastHit != Long.MIN_VALUE && now - lastHit < hitDelay) {
            event.setCancelled(true);
            return;
        }
        data.setLastAcceptedCombatHitTick(now);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void recordHazardDamageFrame(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!isHazardDamage(event.getCause()) || event.getFinalDamage() <= 0.0) return;

        PlayerKnockbackData data = manager.getPlayerData(victim);
        data.setLastHazardDamageTick(manager.getCurrentTick());
    }

    private boolean isHazardDamage(EntityDamageEvent.DamageCause cause) {
        return cause == EntityDamageEvent.DamageCause.FIRE
                || cause == EntityDamageEvent.DamageCause.FIRE_TICK
                || cause == EntityDamageEvent.DamageCause.LAVA
                || cause == EntityDamageEvent.DamageCause.POISON;
    }

    private boolean isPlayerCombatSource(Entity source) {
        if (source instanceof Player) return true;
        return source instanceof Projectile projectile && projectile.getShooter() instanceof Player;
    }

    private boolean isLegacyPearlCombatHit(EnderPearl pearl, Player victim) {
        if (!LegacyProjectileData.isMarked(pearl)) return false;
        if (!(pearl.getShooter() instanceof Player shooter) || shooter.equals(victim)) return false;
        return hasLegacySwordCombat(shooter);
    }

    public void applyLegacyPearlKnockback(Player attacker, Player victim) {
        PlayerKnockbackData data = manager.getPlayerData(victim);
        if (isHazardFrameActive(data)) return;

        KnockbackProfile profile = manager.getProfile(data.getProfileName());
        if (profile == null) profile = manager.getDefaultProfile();
        if (profile == null || !profile.isProjectileEnabled()) return;

        Location victimLocation = victim.getLocation();
        Location attackerLocation = attacker.getLocation();
        double dx = victimLocation.getX() - attackerLocation.getX();
        double dz = victimLocation.getZ() - attackerLocation.getZ();
        double distance = Math.hypot(dx, dz);
        if (distance < 0.001) {
            dx = Math.random() * 0.02 - 0.01;
            dz = Math.random() * 0.02 - 0.01;
            distance = Math.hypot(dx, dz);
        }

        boolean onGround = data.isOnGround();
        double horizontal = profile.getProjectileHorizontal() * profile.getProjectileHorizontalMult();
        double vertical = profile.getProjectileVertical() * profile.getProjectileVerticalMult();
        Vector knockback = new Vector(dx / distance * horizontal, vertical, dz / distance * horizontal);

        if (profile.isLegacyComboResidual()) {
            Vector residual = getLegacyResidual(data, onGround);
            if (residual != null) {
                knockback.add(residual.multiply(0.5));
            }
            data.setPendingLegacyResidual(true);
        } else {
            data.setLegacyResidualVelocity(null);
            data.setLegacyResidualTick(0L);
            data.setPendingLegacyResidual(false);
        }

        if (victimLocation.getY() - data.getLastGroundY() > profile.getYLimit()) {
            knockback.setY(0.0);
        }

        data.setVelocity(knockback);
        data.setLastDamageTick(System.currentTimeMillis());
        forceLegacyPearlKnockback(victim, data, profile, knockback);

        int configuredHitDelay = data.getConfiguredHitDelay();
        int hitDelay = configuredHitDelay >= 0
                ? KnockbackManager.toVanillaNoDamageWindow(configuredHitDelay)
                : profile.getHitDelay();
        if (victim.getMaximumNoDamageTicks() != hitDelay) {
            victim.setMaximumNoDamageTicks(hitDelay);
        }
    }

    // Compute KB immediately after damage
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void computeVelocity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL
                || event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK
                || event.getCause() == EntityDamageEvent.DamageCause.LAVA) return;

        PlayerKnockbackData vData = manager.getPlayerData(victim);
        if (isHazardFrameActive(vData)) return;

        KnockbackProfile profile = manager.getProfile(vData.getProfileName());
        if (profile == null) profile = manager.getDefaultProfile();
        if (profile == null) return;
        Entity source = event.getDamager();
        // Accepted legacy pearl hits are delivered explicitly after damage confirmation.
        // Pearl landing self-damage must never enter the generic direction calculation.
        if (source instanceof EnderPearl) return;
        boolean legacyRodHit = victim.getScoreboardTags().contains("alley_legacy_rod_kb");
        if (legacyRodHit) {
            victim.removeScoreboardTag("alley_legacy_rod_kb");
        }
        boolean legacyProjectile = source instanceof Projectile projectile
                && LegacyProjectileData.isMarked(projectile)
                && projectile.getShooter() instanceof Player shooter
                && hasLegacySwordCombat(shooter);
        AbstractArrow legacyArrow = legacyProjectile && source instanceof AbstractArrow arrow ? arrow : null;
        boolean legacyMeleeHit = source instanceof Player legacyAttacker && hasLegacySwordCombat(legacyAttacker);
        LivingEntity attacker;
        boolean isProjectile = false;

        if (source instanceof LivingEntity) {
            attacker = (LivingEntity) source;
        } else if (source instanceof Projectile projectile) {
            if (!profile.isProjectileEnabled()) {
                vData.setPendingNativeProjectileVelocityTick(manager.getCurrentTick());
                return;
            }
            ProjectileSource shooter = projectile.getShooter();
            if (!(shooter instanceof LivingEntity)) return;
            attacker = (LivingEntity) shooter;
            isProjectile = true;
        } else return;

        boolean legacyBaseProjectile = legacyRodHit || legacyProjectile;
        boolean legacyCombatHit = legacyMeleeHit || legacyBaseProjectile;

        Location vLoc = victim.getLocation();
        Location aLoc = attacker.getLocation();
        double dx = vLoc.getX() - aLoc.getX();
        double dz = vLoc.getZ() - aLoc.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (legacyArrow != null) {
            Vector arrowVelocity = legacyArrow.getVelocity();
            double horizontalSpeed = Math.hypot(arrowVelocity.getX(), arrowVelocity.getZ());
            if (horizontalSpeed > 1.0E-4) {
                dx = arrowVelocity.getX();
                dz = arrowVelocity.getZ();
                dist = horizontalSpeed;
            }
        }
        if (dist < 0.001) { dx = Math.random() * 0.02 - 0.01; dz = Math.random() * 0.02 - 0.01; dist = Math.sqrt(dx * dx + dz * dz); }

        Vector kb;
        if (isProjectile && !legacyBaseProjectile && source instanceof Projectile && !profile.isProjectileDirectionOverride()) {
            kb = source.getVelocity().clone().normalize().setY(1.0);
        } else {
            kb = new Vector(dx / dist, 1.0, dz / dist);
        }

        boolean onGround = vData.isOnGround();
        if (legacyArrow != null) {
            // Legacy arrows use the normal 1.8 hit strength. Their direction comes from
            // the projectile and their magnitude still follows the bow draw strength.
            double drawForce = LegacyProjectileData.getBowForce(legacyArrow);
            double horizontal = onGround ? profile.getHorizontalGround() : profile.getHorizontalAir();
            double vertical = onGround ? profile.getVerticalGround() : profile.getVerticalAir();
            kb.setX(kb.getX() * horizontal * drawForce);
            kb.setY(kb.getY() * vertical * drawForce);
            kb.setZ(kb.getZ() * horizontal * drawForce);
        } else if ((isProjectile && legacyArrow == null) || legacyRodHit) {
            // Thrown projectiles and synthetic rod hits use the projectile profile.
            kb.setX(kb.getX() * profile.getProjectileHorizontal());
            kb.setY(kb.getY() * profile.getProjectileVertical());
            kb.setZ(kb.getZ() * profile.getProjectileHorizontal());
            // Apply extra multiplier if configured
            kb.setX(kb.getX() * profile.getProjectileHorizontalMult());
            kb.setY(kb.getY() * profile.getProjectileVerticalMult());
            kb.setZ(kb.getZ() * profile.getProjectileHorizontalMult());
        } else {
            double hor = onGround ? profile.getHorizontalGround() : profile.getHorizontalAir();
            double ver = onGround ? profile.getVerticalGround() : profile.getVerticalAir();

            kb.setX(kb.getX() * hor);
            kb.setY(kb.getY() * ver);
            kb.setZ(kb.getZ() * hor);
            if (!legacyBaseProjectile) {
                Player playerAttacker = attacker instanceof Player ? (Player) attacker : null;
                float attackCooldown = playerAttacker == null
                        ? 1.0f
                        : getAttackCooldown(playerAttacker, victim);
                int knockbackLevel = 0;
                if (attacker.getEquipment() != null) {
                    var hand = attacker.getEquipment().getItemInMainHand();
                    if (!hand.getType().isAir()) knockbackLevel = hand.getEnchantmentLevel(Enchantment.KNOCKBACK);
                }
                boolean sprinting = playerAttacker != null && playerAttacker.isSprinting();
                if (sprinting && profile.isCooldownAffectsKb()) {
                    if (attackCooldown <= 0.9f) sprinting = false;
                }
                int bonusLevels = knockbackLevel + (sprinting ? 1 : 0);
                if (bonusLevels > 0) {
                    double yaw = Math.toRadians(aLoc.getYaw());
                    double horizontalBonus = bonusLevels * profile.getHorizontalSprintExtra();
                    kb.setX(kb.getX() - Math.sin(yaw) * horizontalBonus);
                    kb.setZ(kb.getZ() + Math.cos(yaw) * horizontalBonus);
                    // Vanilla only applies one small vertical bonus, regardless of enchant level.
                    kb.setY(kb.getY() + profile.getVerticalSprintExtra());
                }
            }
        }

        if (source instanceof AbstractArrow arrow) {
            int punchLevel = Math.max(LegacyProjectileData.getPunchLevel(arrow), arrow.getKnockbackStrength());
            Vector arrowVelocity = arrow.getVelocity();
            double horizontalLength = Math.hypot(arrowVelocity.getX(), arrowVelocity.getZ());
            if (punchLevel > 0 && horizontalLength > 1.0E-4) {
                kb.setX(kb.getX() + arrowVelocity.getX() / horizontalLength * 0.6 * punchLevel);
                kb.setY(kb.getY() + 0.1);
                kb.setZ(kb.getZ() + arrowVelocity.getZ() / horizontalLength * 0.6 * punchLevel);
            }
        }

        if (legacyCombatHit) {
            Vector residual = getLegacyResidual(vData, onGround);
            if (residual != null) {
                kb.add(residual.multiply(0.5));
            }

            boolean retainResidual = profile.isLegacyComboResidual();
            if (!retainResidual) {
                vData.setLegacyResidualVelocity(null);
                vData.setLegacyResidualTick(0L);
            }
            vData.setPendingLegacyResidual(retainResidual);
        }

        // Y limit: if victim is > yLimit blocks above last ground, cancel vertical KB
        if ((!isProjectile || legacyBaseProjectile) && vLoc.getY() - vData.getLastGroundY() > profile.getYLimit()) {
            kb.setY(0.0);
        }
        vData.setVelocity(kb);
        vData.setLastDamageTick(System.currentTimeMillis());

        if (legacyBaseProjectile) {
            deliverPendingLegacyKnockback(victim, vData, profile, kb);
        }

        // Record attack for misplace handler
        if (attacker instanceof Player && !isProjectile && !legacyRodHit) {
            MisplaceHandler mh = manager.getMisplaceHandler();
            if (mh != null) mh.recordAttack((Player) attacker, victim);
            if (profile.isStopSprint()) {
                ((Player) attacker).setSprinting(false);
            }
        }

        // Bukkit opens its damage gate at half of this field, so kit delays use a 2x window.
        int configuredHitDelay = vData.getConfiguredHitDelay();
        int hitDelay = configuredHitDelay >= 0
                ? KnockbackManager.toVanillaNoDamageWindow(configuredHitDelay)
                : profile.getHitDelay();
        if (victim.getMaximumNoDamageTicks() != hitDelay) {
            victim.setMaximumNoDamageTicks(hitDelay);
        }
        // Entity interaction range (attack reach)
        if (attacker instanceof Player) {
            try {
                ((Player) attacker).getAttribute(org.bukkit.attribute.Attribute.ENTITY_INTERACTION_RANGE)
                        .setBaseValue(profile.getEntityInteractionRange());
            } catch (Exception ignored) {}
        }
    }

    // Apply cached velocity
    @EventHandler(priority = EventPriority.LOWEST)
    public void applyVelocity(PlayerVelocityEvent event) {
        PlayerKnockbackData data = manager.getPlayerData(event.getPlayer());
        Vector calculated = data.getVelocity();
        long pendingNativeTick = data.getPendingNativeProjectileVelocityTick();
        boolean nativeProjectile = pendingNativeTick != Long.MIN_VALUE
                && manager.getCurrentTick() >= pendingNativeTick
                && manager.getCurrentTick() - pendingNativeTick <= 1L;
        if (calculated == null && !nativeProjectile) return;

        KnockbackProfile profile = manager.getProfile(data.getProfileName());
        if (profile == null) profile = manager.getDefaultProfile();
        if (profile == null) {
            data.setVelocity(null);
            data.clearPendingNativeProjectileVelocity();
            return;
        }

        if (calculated != null) {
            applyDownwardKnockback(event.getPlayer(), profile, calculated, event.getVelocity());
            event.setVelocity(calculated);
        } else if (profile.isDisableDownwardKb() && event.getVelocity().getY() < 0.0) {
            Vector nativeVelocity = event.getVelocity().clone();
            nativeVelocity.setY(0.0);
            event.setVelocity(nativeVelocity);
        }

        if (data.isPendingLegacyResidual()) {
            data.setLegacyResidualVelocity(event.getVelocity().clone());
            data.setLegacyResidualTick(Bukkit.getCurrentTick());
            data.setPendingLegacyResidual(false);
        }
        data.setVelocity(null);
        data.clearPendingNativeProjectileVelocity();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        this.attackCooldowns.remove(e.getPlayer().getUniqueId());
        manager.removePlayer(e.getPlayer().getUniqueId());
    }

    private float getAttackCooldown(Player attacker, Player victim) {
        AttackCooldownSnapshot snapshot = this.attackCooldowns.remove(attacker.getUniqueId());
        if (snapshot != null
                && snapshot.tick() == Bukkit.getCurrentTick()
                && snapshot.targetId().equals(victim.getUniqueId())) {
            return snapshot.strength();
        }
        return attacker.getAttackCooldown();
    }

    private boolean hasLegacySwordCombat(Player player) {
        LegacyCombatService legacyCombat = getLegacyCombatService();
        return legacyCombat != null && legacyCombat.hasSwordBlockKB(player.getUniqueId());
    }

    private LegacyCombatService getLegacyCombatService() {
        MatchService matchService = AlleyPlugin.getInstance().getService(MatchService.class);
        if (!(matchService instanceof MatchServiceImpl service)) {
            return null;
        }
        return service.getLegacyCombatService();
    }

    private boolean isHazardFrameActive(PlayerKnockbackData data) {
        long lastHazardDamage = data.getLastHazardDamageTick();
        long now = manager.getCurrentTick();
        return lastHazardDamage != Long.MIN_VALUE
                && now >= lastHazardDamage
                && now - lastHazardDamage < LEGACY_HAZARD_IMMUNITY_TICKS;
    }

    private Vector getLegacyResidual(PlayerKnockbackData data, boolean onGround) {
        Vector residual = data.getLegacyResidualVelocity();
        if (residual == null) return null;

        long elapsed = Bukkit.getCurrentTick() - data.getLegacyResidualTick();
        if (elapsed < 0 || elapsed > 60) {
            data.setLegacyResidualVelocity(null);
            data.setLegacyResidualTick(0L);
            return null;
        }

        Vector decayed = residual.clone();
        double horizontalFriction = onGround ? 0.546 : 0.91;
        for (long tick = 0; tick < elapsed; tick++) {
            decayed.setX(decayed.getX() * horizontalFriction);
            decayed.setY((decayed.getY() - 0.08) * 0.98);
            decayed.setZ(decayed.getZ() * horizontalFriction);
        }

        if (decayed.lengthSquared() < 1.0E-6) {
            data.setLegacyResidualVelocity(null);
            data.setLegacyResidualTick(0L);
            return null;
        }

        data.setLegacyResidualVelocity(decayed.clone());
        data.setLegacyResidualTick(Bukkit.getCurrentTick());
        return decayed;
    }

    /**
     * Synthetic and zero-damage legacy projectile hits do not always emit a
     * velocity event. Force the computed vector next tick only if it is still pending.
     */
    private void deliverPendingLegacyKnockback(Player player, PlayerKnockbackData data,
                                               KnockbackProfile profile, Vector expected) {
        Bukkit.getScheduler().runTask(AlleyPlugin.getInstance(), () -> {
            Vector pending = data.getVelocity();
            if (!player.isOnline() || player.isDead() || pending != expected) return;
            applyDownwardKnockback(player, profile, pending, player.getVelocity());
            player.setVelocity(pending);
            if (data.getVelocity() == pending) {
                if (data.isPendingLegacyResidual()) {
                    data.setLegacyResidualVelocity(pending.clone());
                    data.setLegacyResidualTick(Bukkit.getCurrentTick());
                    data.setPendingLegacyResidual(false);
                }
                data.setVelocity(null);
            }
        });
    }

    /** Modern pearl impact code may overwrite a velocity event after it fires. */
    private void forceLegacyPearlKnockback(Player player, PlayerKnockbackData data,
                                          KnockbackProfile profile, Vector expected) {
        Bukkit.getScheduler().runTask(AlleyPlugin.getInstance(), () -> {
            if (!player.isOnline() || player.isDead()) return;
            if (isHazardFrameActive(data)) {
                if (data.getVelocity() == expected) data.setVelocity(null);
                data.setPendingLegacyResidual(false);
                return;
            }

            Vector forced = expected.clone();
            applyDownwardKnockback(player, profile, forced, player.getVelocity());
            player.setVelocity(forced);

            if (data.isPendingLegacyResidual()) {
                data.setLegacyResidualVelocity(forced.clone());
                data.setLegacyResidualTick(Bukkit.getCurrentTick());
                data.setPendingLegacyResidual(false);
            }
            if (data.getVelocity() == expected) {
                data.setVelocity(null);
            }
        });
    }

    /**
     * Uses the NMS-produced velocity event as the authoritative modern vertical.
     * Horizontal knockback remains owned by the configured KB profile.
     */
    private void applyDownwardKnockback(Player victim, KnockbackProfile profile, Vector knockback, Vector nativeVelocity) {
        if (profile.isDisableDownwardKb()) {
            knockback.setY(Math.max(0.0, knockback.getY()));
            return;
        }

        if (!victim.isOnGround()) {
            knockback.setY(nativeVelocity.getY());
        }
    }

    private record AttackCooldownSnapshot(UUID targetId, float strength, int tick) {
    }
}
