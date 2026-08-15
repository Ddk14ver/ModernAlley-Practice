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
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityKnockbackByEntityEvent;
import org.bukkit.event.entity.EntityKnockbackEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.inventory.ItemStack;
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
    private static final double LEGACY_ARROW_BASE_KNOCKBACK = 0.4D;
    private static final double LEGACY_ARROW_MAX_VERTICAL = 0.4000000059604645D;

    private final KnockbackManager manager;
    private final Map<UUID, AttackCooldownSnapshot> attackCooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, PendingLegacyAttackerSlowdown> pendingLegacyAttackerSlowdowns = new ConcurrentHashMap<>();

    public KnockbackListener(KnockbackManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAttackCooldownReset(PlayerAttackEntityCooldownResetEvent event) {
        if (event.getAttackedEntity() instanceof Player victim) {
            PlayerKnockbackData victimData = manager.getPlayerData(victim);
            if (victimData.getProfileName() != null) {
                manager.applyHitDelayWindow(victim);
            }
        }
        if (hasLegacySwordCombat(event.getPlayer())) {
            // 1.8 has no attack-strength ticker. Cancelling the reset keeps
            // subsequent legacy hits at full strength without altering other kits.
            event.setCancelled(true);
            this.attackCooldowns.remove(event.getPlayer().getUniqueId());
            return;
        }
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
        manager.updateMovementState(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        MisplaceHandler handler = manager.getMisplaceHandler();
        if (handler != null) {
            // Pre-teleport relative/absolute movement packets are invalid after
            // a world, round, respawn, or arena teleport.
            handler.clearPlayer(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBowShoot(EntityShootBowEvent event) {
        if (!(event.getProjectile() instanceof AbstractArrow arrow)) return;
        int punchLevel = event.getBow() == null ? 0 : event.getBow().getEnchantmentLevel(Enchantment.PUNCH);
        LegacyProjectileData.storeBowPunch(arrow, punchLevel);
    }

    /** Captures the pre-event hurt-window state for damage-only supplements. */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void beginLegacyDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!hasLegacySwordCombat(victim)) return;

        // Do not cancel by damage category. Both 1.8 and modern NMS compare the
        // incoming amount with lastDamage inside this window: smaller/equal hits
        // are rejected, while a larger hit applies only the difference.
        manager.beginLegacyDamage(victim);
        // Keep the profile window authoritative for the NMS work that follows
        // this event, without changing the pre-event state used above to decide
        // whether this is a fresh hit or a damage delta.
        manager.applyHitDelayWindow(victim);
    }

    public void applyLegacyPearlKnockback(Player attacker, Player victim, Vector impactVelocity) {
        PlayerKnockbackData data = manager.getPlayerData(victim);

        KnockbackProfile profile = manager.getProfile(data.getProfileName());
        if (profile == null) profile = manager.getDefaultProfile();
        if (profile == null || !profile.isProjectileEnabled()) return;

        Location victimLocation = victim.getLocation();
        Vector direction = impactVelocity == null ? new Vector() : impactVelocity.clone();
        double dx = direction.getX();
        double dz = direction.getZ();
        double distance = Math.hypot(dx, dz);
        if (distance < 0.001) {
            direction = attacker.getLocation().getDirection();
            dx = direction.getX();
            dz = direction.getZ();
            distance = Math.hypot(dx, dz);
            if (distance < 0.001) {
                dx = 0.0;
                dz = 1.0;
                distance = 1.0;
            }
        }

        double horizontal = profile.getProjectileHorizontal() * profile.getProjectileHorizontalMult();
        double vertical = profile.getProjectileVertical() * profile.getProjectileVerticalMult();
        Vector impulse = new Vector(dx / distance * horizontal, vertical, dz / distance * horizontal);
        Vector knockback = isLegacyBaseKnockbackResisted(victim)
                ? victim.getVelocity().clone()
                : inheritLegacyMotion(victim, impulse);

        if (victimLocation.getY() - data.getLastGroundY() > profile.getYLimit()) {
            knockback.setY(0.0);
        }

        data.setVelocity(knockback);
        data.setLastDamageTick(System.currentTimeMillis());
        forceLegacyPearlKnockback(victim, data, knockback);

        manager.applyHitDelayWindow(victim);
    }

    // Compute KB after all damage listeners have finished, but before NMS applies it.
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void computeVelocity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (victim.isDead() || victim.getGameMode() == GameMode.SPECTATOR) return;
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL
                || event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK
                || event.getCause() == EntityDamageEvent.DamageCause.LAVA
                // Sweep attacks deal 1 point of flat damage with no knockback in vanilla.
                // Routing them through the profile knockback both adds phantom KB and
                // re-triggers misplace/delay bookkeeping on a hit the victim could not
                // even see coming; the vanilla hurt-resistance window already gates them.
                // 跳劈（横扫）在原版中只造成1点固定伤害且无击退。如果走配置文件击退，
                // 会产生虚假击退并重复触发misplace/delay的记录；原版无敌帧窗口已对其限制。
                || event.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) return;

        PlayerKnockbackData vData = manager.getPlayerData(victim);

        KnockbackProfile profile = manager.getProfile(vData.getProfileName());
        if (profile == null) profile = manager.getDefaultProfile();
        if (profile == null) return;
        Entity source = event.getDamager();
        // Accepted legacy pearl hits are delivered explicitly after damage confirmation.
        // Pearl landing self-damage must never enter the generic direction calculation.
        if (source instanceof EnderPearl) return;
        boolean taggedLegacyRodHit = victim.getScoreboardTags().contains("alley_legacy_rod_kb");
        if (taggedLegacyRodHit) {
            victim.removeScoreboardTag("alley_legacy_rod_kb");
        }
        boolean legacyVictim = hasLegacySwordCombat(victim);
        boolean legacyRodHit = legacyVictim && taggedLegacyRodHit;
        boolean legacyProjectile = legacyVictim && source instanceof Projectile projectile
                && LegacyProjectileData.isMarked(projectile)
                && projectile.getShooter() instanceof Player shooter
                && hasLegacySwordCombat(shooter);
        AbstractArrow legacyArrow = legacyProjectile && source instanceof AbstractArrow arrow ? arrow : null;
        boolean legacyMeleeHit = legacyVictim
                && source instanceof Player legacyAttacker
                && hasLegacySwordCombat(legacyAttacker);
        LivingEntity attacker;
        boolean isProjectile = false;

        if (source instanceof LivingEntity) {
            attacker = (LivingEntity) source;
        } else if (source instanceof Projectile projectile) {
            if (!profile.isProjectileEnabled()) {
                if (legacyVictim && manager.wasInsideHurtResistanceWindow(victim)) {
                    // Even with profile projectile KB disabled, a larger legacy
                    // projectile hit in the hurt window is damage-only.
                    UUID shooterId = projectile.getShooter() instanceof Entity shooter
                            ? shooter.getUniqueId() : null;
                    vData.markLegacyDamageSupplement(
                            Bukkit.getCurrentTick(), source.getUniqueId(), shooterId);
                } else {
                    vData.setPendingNativeProjectileVelocityTick(manager.getCurrentTick());
                }
                return;
            }
            ProjectileSource shooter = projectile.getShooter();
            if (!(shooter instanceof LivingEntity livingShooter)) {
                if (legacyVictim && manager.wasInsideHurtResistanceWindow(victim)) {
                    UUID shooterId = shooter instanceof Entity entity ? entity.getUniqueId() : null;
                    vData.markLegacyDamageSupplement(
                            Bukkit.getCurrentTick(), source.getUniqueId(), shooterId);
                }
                return;
            }
            attacker = livingShooter;
            isProjectile = true;
        } else return;

        boolean legacyBaseProjectile = legacyRodHit || legacyProjectile;
        boolean legacyCombatHit = legacyMeleeHit || legacyBaseProjectile;
        // Use the state captured before other listeners (notably Boxing's
        // zero-damage handler) can manually arm the current event's window.
        boolean legacyHurtResistant = legacyVictim
                && manager.wasInsideHurtResistanceWindow(victim);
        // Paper/NMS may still fire an event for a larger hit inside the hurt
        // window, but that branch only applies the damage delta.  It must not
        // receive a second legacy velocity, Punch bonus, or combo residual.
        if (legacyHurtResistant) {
            vData.markLegacyDamageSupplement(
                    Bukkit.getCurrentTick(), source.getUniqueId(), attacker.getUniqueId());
            return;
        }

        Location vLoc = victim.getLocation();
        Location aLoc = attacker.getLocation();
        double attackerDx = vLoc.getX() - aLoc.getX();
        double attackerDz = vLoc.getZ() - aLoc.getZ();
        double attackerDistance = Math.sqrt(attackerDx * attackerDx + attackerDz * attackerDz);
        double dx = attackerDx;
        double dz = attackerDz;
        double dist = attackerDistance;
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

        boolean legacyBaseKnockbackResisted = legacyCombatHit
                && isLegacyBaseKnockbackResisted(victim);
        Vector kb;
        boolean legacyShouldStopSprint = false;
        if (isProjectile && !legacyBaseProjectile && source instanceof Projectile && !profile.isProjectileDirectionOverride()) {
            kb = source.getVelocity().clone().normalize().setY(1.0);
        } else {
            kb = new Vector(dx / dist, 1.0, dz / dist);
        }

        boolean onGround = vData.isOnGround();
        if (legacyArrow != null) {
            kb = legacyBaseKnockbackResisted
                    ? victim.getVelocity().clone()
                    : createLegacyArrowKnockback(victim, attackerDx, attackerDz, attackerDistance);
        } else if ((isProjectile && legacyArrow == null) || legacyRodHit) {
            // Thrown projectiles and synthetic rod hits use the projectile profile.
            kb.setX(kb.getX() * profile.getProjectileHorizontal());
            kb.setY(kb.getY() * profile.getProjectileVertical());
            kb.setZ(kb.getZ() * profile.getProjectileHorizontal());
            // Apply extra multiplier if configured
            kb.setX(kb.getX() * profile.getProjectileHorizontalMult());
            kb.setY(kb.getY() * profile.getProjectileVerticalMult());
            kb.setZ(kb.getZ() * profile.getProjectileHorizontalMult());
            if (legacyBaseKnockbackResisted) {
                kb = victim.getVelocity().clone();
            } else if (legacyBaseProjectile) {
                kb = inheritLegacyMotion(victim, kb);
            }
        } else {
            double hor = onGround ? profile.getHorizontalGround() : profile.getHorizontalAir();
            double ver = onGround ? profile.getVerticalGround() : profile.getVerticalAir();

            kb.setX(kb.getX() * hor);
            kb.setY(kb.getY() * ver);
            kb.setZ(kb.getZ() * hor);
            if (legacyBaseKnockbackResisted) {
                kb = victim.getVelocity().clone();
            } else if (legacyMeleeHit) {
                kb = inheritLegacyMotion(victim, kb);
            }
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
                    legacyShouldStopSprint = legacyMeleeHit;
                    double yaw = Math.toRadians(aLoc.getYaw());
                    double horizontalBonus = bonusLevels * profile.getHorizontalSprintExtra();
                    kb.setX(kb.getX() - Math.sin(yaw) * horizontalBonus);
                    kb.setZ(kb.getZ() + Math.cos(yaw) * horizontalBonus);
                    // 1.8 applies this addVelocity after the base knockback,
                    // including when resistance rejected that base entirely.
                    if (legacyMeleeHit) {
                        kb.setY(kb.getY() + profile.getVerticalSprintExtra());
                    } else {
                        kb.setY(Math.min(0.4, kb.getY() + profile.getVerticalSprintExtra()));
                    }
                    // AutoClick calls Player#attack from the server thread and
                    // applies the vanilla 0.6 attacker slowdown itself. Do not
                    // queue the next-tick fallback as that would multiply the
                    // already reduced velocity a second time (0.36).
                    if (legacyMeleeHit && playerAttacker != null
                            && !manager.getPlayerData(playerAttacker).isServerSideHit()) {
                        queueLegacyAttackerSlowdownFallback(playerAttacker, victim);
                    }
                }
            }
        }

        if (source instanceof AbstractArrow arrow) {
            int punchLevel = getArrowPunch(arrow);
            Vector arrowVelocity = arrow.getVelocity();
            double horizontalLength = Math.hypot(arrowVelocity.getX(), arrowVelocity.getZ());
            if (punchLevel > 0 && horizontalLength > 1.0E-4) {
                kb.setX(kb.getX() + arrowVelocity.getX() / horizontalLength * 0.6 * punchLevel);
                kb.setY(kb.getY() + 0.1);
                kb.setZ(kb.getZ() + arrowVelocity.getZ() / horizontalLength * 0.6 * punchLevel);
            }
        }

        // Y limit: if victim is > yLimit blocks above last ground, cancel vertical KB
        if ((!isProjectile || (legacyBaseProjectile && legacyArrow == null))
                && vLoc.getY() - vData.getLastGroundY() > profile.getYLimit()) {
            kb.setY(0.0);
        }
        vData.setVelocity(kb);
        vData.setLastDamageTick(System.currentTimeMillis());

        if (legacyCombatHit && !legacyHurtResistant) {
            deliverPendingLegacyKnockback(victim, vData, kb);
        }

        // Record attack for misplace handler
        if (attacker instanceof Player && !isProjectile && !legacyRodHit) {
            MisplaceHandler mh = manager.getMisplaceHandler();
            if (mh != null) mh.recordAttack((Player) attacker, victim);
            // stop_sprint conflicts with cooldown_affects_kb: clearing sprint makes
            // isSprinting() read false on every later hit, so the modern sprint bonus
            // would never apply regardless of the attacker's cooldown. With
            // cooldown_affects_kb enabled, let the client's sprint intent drive the
            // bonus, exactly like vanilla 1.21.11.
            // stop_sprint与cooldown_affects_kb冲突：强制清除冲刺会让后续每次攻击的
            // isSprinting()都为false，冲刺加成（哪怕满冷却）永远不会生效。
            // 启用cooldown_affects_kb时，冲刺状态交给客户端意图决定（与1.21.11原版一致）。
            if (legacyShouldStopSprint
                    || (!legacyMeleeHit && profile.isStopSprint() && !profile.isCooldownAffectsKb())) {
                ((Player) attacker).setSprinting(false);
            }
        }

        manager.applyHitDelayWindow(victim);
    }

    /**
     * Applies the knockback enchantment bonus to minecarts and TNT minecarts when hit by
     * a player, pushing them in the attacker's facing direction using the same per-level
     * horizontal bonus the profile gives to players.
     * 当玩家击中矿车/TNT矿车时，为其施加击退附魔加成，使用与玩家相同的每级水平加成，
     * 沿攻击者朝向将其推开。
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void computeMinecartKnockback(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Minecart minecart)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) return;

        ItemStack hand = attacker.getEquipment() != null ? attacker.getEquipment().getItemInMainHand() : null;
        if (hand == null || hand.getType().isAir()) return;
        int knockbackLevel = hand.getEnchantmentLevel(Enchantment.KNOCKBACK);
        if (knockbackLevel <= 0) return;

        KnockbackProfile profile = manager.getProfile(manager.getPlayerData(attacker).getProfileName());
        if (profile == null) profile = manager.getDefaultProfile();
        // Use the profile's full base horizontal knockback per level so the push is clearly
        // felt; the old sprint_extra (0.25/level) was too weak to overcome minecart rail friction.
        double perLevel = profile != null ? profile.getHorizontalGround() : 0.5;

        double yaw = Math.toRadians(attacker.getLocation().getYaw());
        double factor = knockbackLevel * perLevel;
        Vector velocity = minecart.getVelocity();
        velocity.add(new Vector(-Math.sin(yaw) * factor, 0.0, Math.cos(yaw) * factor));
        minecart.setVelocity(velocity);
    }

    /**
     * Player and arrow attack code can add an extra native knockback after the
     * damage event has returned. A legacy damage supplement is damage-only, so
     * cancel that post-event knockback as well as skipping the profile velocity.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void suppressLegacyDamageSupplementKnockback(EntityKnockbackEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;

        PlayerKnockbackData data = manager.getPlayerData(victim);
        long supplementTick = data.getLegacyDamageSupplementTick();
        long currentTick = Bukkit.getCurrentTick();
        if (supplementTick != currentTick) {
            if (supplementTick != Long.MIN_VALUE && supplementTick < currentTick) {
                data.clearLegacyDamageSupplement();
            }
            return;
        }

        if (event instanceof EntityKnockbackByEntityEvent byEntity) {
            Entity sourceEntity = byEntity.getSourceEntity();
            if (sourceEntity == null) return;
            UUID sourceId = sourceEntity.getUniqueId();
            if (!sourceId.equals(data.getLegacyDamageSupplementSource())
                    && !sourceId.equals(data.getLegacyDamageSupplementAttacker())) {
                return;
            }
        } else if (event.getCause() != EntityKnockbackEvent.KnockbackCause.ENTITY_ATTACK
                && event.getCause() != EntityKnockbackEvent.KnockbackCause.DAMAGE) {
            // Do not let an unrelated explosion, shield block, or piston push
            // in the same tick consume the attack supplement marker.
            return;
        }

        event.setCancelled(true);
    }

    /**
     * Downward suppression must also cover native knockback that does not produce
     * a PlayerVelocityEvent (notably some projectile and server-side paths).
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void suppressDisabledDownwardKnockback(EntityKnockbackEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;

        PlayerKnockbackData data = manager.getPlayerData(victim);
        KnockbackProfile profile = manager.getProfile(data.getProfileName());
        if (profile == null) profile = manager.getDefaultProfile();
        if (profile == null || !profile.isDisableDownwardKb()) return;

        Vector knockback = event.getFinalKnockback();
        if (knockback != null && knockback.getY() < 0.0D) {
            event.setFinalKnockback(knockback.clone().setY(0.0D));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void observeNativeLegacyAttackerKnockback(EntityKnockbackByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)
                || !(event.getSourceEntity() instanceof Player attacker)) {
            return;
        }

        PendingLegacyAttackerSlowdown pending = pendingLegacyAttackerSlowdowns.get(attacker.getUniqueId());
        if (pending != null
                && pending.tick() == Bukkit.getCurrentTick()
                && pending.victimId().equals(victim.getUniqueId())) {
            pending.markNativeKnockbackObserved();
        }
    }

    // Apply cached velocity
    @EventHandler(priority = EventPriority.LOWEST)
    public void applyVelocity(PlayerVelocityEvent event) {
        PlayerKnockbackData data = manager.getPlayerData(event.getPlayer());
        if (data.isSuppressLegacyPearlVelocity()) {
            data.setCaptureServerControlledVelocity(false);
            data.setServerControlledVelocity(null);
            event.setCancelled(true);
            return;
        }

        Vector pending = data.getVelocity();
        long pendingNativeTick = data.getPendingNativeProjectileVelocityTick();
        boolean nativeProjectile = pendingNativeTick != Long.MIN_VALUE
                && manager.getCurrentTick() >= pendingNativeTick
                && manager.getCurrentTick() - pendingNativeTick <= 1L;
        data.setCaptureServerControlledVelocity(
                data.isServerControlled() && (pending != null || nativeProjectile));
        Vector velocity = consumePendingKnockback(event.getPlayer(), event.getVelocity());
        if (velocity != null) {
            event.setVelocity(velocity);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void retainServerControlledVelocity(PlayerVelocityEvent event) {
        PlayerKnockbackData data = manager.getPlayerData(event.getPlayer());
        if (!data.isCaptureServerControlledVelocity()) return;

        data.setCaptureServerControlledVelocity(false);
        data.setServerControlledVelocity(event.isCancelled() ? null : event.getVelocity().clone());
    }

    public Vector consumePendingKnockback(Player player, Vector nativeVelocity) {
        PlayerKnockbackData data = manager.getPlayerData(player);
        Vector calculated = data.getVelocity();
        long pendingNativeTick = data.getPendingNativeProjectileVelocityTick();
        boolean nativeProjectile = pendingNativeTick != Long.MIN_VALUE
                && manager.getCurrentTick() >= pendingNativeTick
                && manager.getCurrentTick() - pendingNativeTick <= 1L;
        if (calculated == null && !nativeProjectile) {
            if (pendingNativeTick != Long.MIN_VALUE
                    && manager.getCurrentTick() - pendingNativeTick > 1L) {
                data.clearPendingNativeProjectileVelocity();
            }
            return null;
        }
        KnockbackProfile profile = manager.getProfile(data.getProfileName());
        if (profile == null) profile = manager.getDefaultProfile();
        if (profile == null) {
            data.setVelocity(null);
            data.clearPendingNativeProjectileVelocity();
            return null;
        }

        Vector applied = null;
        if (calculated != null) {
            applyDownwardKnockback(player, profile, calculated, nativeVelocity);
            applied = calculated;
        } else if (profile.isDisableDownwardKb() && nativeVelocity.getY() < 0.0) {
            applied = nativeVelocity.clone();
            applied.setY(0.0);
        }

        data.setVelocity(null);
        data.clearPendingNativeProjectileVelocity();
        if (applied != null) {
            data.setLastKnockbackApplicationTick(Bukkit.getCurrentTick());
            data.setLastAppliedKnockbackVelocity(applied);
        }
        return applied;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        this.attackCooldowns.remove(e.getPlayer().getUniqueId());
        this.pendingLegacyAttackerSlowdowns.remove(e.getPlayer().getUniqueId());
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

    private int getArrowPunch(AbstractArrow arrow) {
        return Math.max(LegacyProjectileData.getPunchLevel(arrow), arrow.getKnockbackStrength());
    }

    /** Mirrors EntityLivingBase#knockBack for the accepted base portion of a 1.8 arrow hit. */
    private Vector createLegacyArrowKnockback(Player victim, double directionX,
                                               double directionZ, double horizontalDistance) {
        if (horizontalDistance < 0.001D) {
            directionX = Math.random() * 0.02D - 0.01D;
            directionZ = Math.random() * 0.02D - 0.01D;
            horizontalDistance = Math.sqrt(directionX * directionX + directionZ * directionZ);
        }

        return inheritLegacyMotion(victim, new Vector(
                directionX / horizontalDistance * LEGACY_ARROW_BASE_KNOCKBACK,
                LEGACY_ARROW_BASE_KNOCKBACK,
                directionZ / horizontalDistance * LEGACY_ARROW_BASE_KNOCKBACK));
    }

    /** Mirrors the motion/2 stage of EntityLivingBase#knockBack. */
    private Vector inheritLegacyMotion(Player victim, Vector knockback) {
        Vector current = getLegacyInheritedMotion(victim, knockback);
        return new Vector(
                current.getX() * 0.5D + knockback.getX(),
                Math.min(LEGACY_ARROW_MAX_VERTICAL,
                        current.getY() * 0.5D + knockback.getY()),
                current.getZ() * 0.5D + knockback.getZ());
    }

    /**
     * 1.8's knockBack() halves leftover knockback motion, not WASD walk speed.
     * Packet players report walking through position packets, and bots have real
     * W/strafe velocity, so {@link Player#getVelocity()} is the wrong input for
     * both. Inherit only the decayed residual of the last delivered knockback.
     */
    private Vector getLegacyInheritedMotion(Player victim, Vector knockback) {
        PlayerKnockbackData data = manager.getPlayerData(victim);
        Vector previousKnockback = data.getLastAppliedKnockbackVelocity();
        long elapsed = Bukkit.getCurrentTick() - data.getLastKnockbackApplicationTick();
        if (previousKnockback == null || elapsed < 0L || elapsed > 20L) {
            return new Vector();
        }

        Vector inherited = new Vector();
        double horizontalLength = Math.hypot(knockback.getX(), knockback.getZ());
        if (horizontalLength >= 1.0E-8D) {
            double directionX = knockback.getX() / horizontalLength;
            double directionZ = knockback.getZ() / horizontalLength;
            double previousProjection = Math.max(0.0D,
                    previousKnockback.getX() * directionX + previousKnockback.getZ() * directionZ);
            double friction = victim.isOnGround() ? 0.546D : 0.91D;
            double residual = previousProjection * Math.pow(friction, elapsed);
            inherited.setX(directionX * residual);
            inherited.setZ(directionZ * residual);
        }
        double residualY = previousKnockback.getY();
        for (long tick = 0L; tick < elapsed; tick++) {
            residualY = (residualY - 0.08D) * 0.98D;
        }
        inherited.setY(Math.max(0.0D, residualY));
        return inherited;
    }

    /** 1.8 treats knockback resistance as a per-hit probability, not a multiplier. */
    private boolean isLegacyBaseKnockbackResisted(Player victim) {
        AttributeInstance resistance = victim.getAttribute(Attribute.KNOCKBACK_RESISTANCE);
        if (resistance == null) return false;

        double chance = Math.max(0.0D, Math.min(1.0D, resistance.getValue()));
        return chance > 0.0D && Math.random() < chance;
    }

    /**
     * Synthetic and zero-damage legacy projectile hits do not always emit a
     * velocity event. Force the computed vector next tick only if it is still pending.
     */
    private void deliverPendingLegacyKnockback(Player player, PlayerKnockbackData data,
                                               Vector expected) {
        Bukkit.getScheduler().runTask(AlleyPlugin.getInstance(), () -> {
            Vector pending = data.getVelocity();
            // A server-side Player can have no client connection while its
            // world entity remains alive and valid.
            if ((!player.isOnline() && !player.isValid()) || player.isDead()) {
                if (pending == expected) {
                    data.setVelocity(null);
                }
                return;
            }
            if (pending != expected) return;
            Vector delivered = consumePendingKnockback(player, player.getVelocity());
            if (delivered != null) player.setVelocity(delivered);
        });
    }

    /** Modern pearl impact code may overwrite a velocity event after it fires. */
    private void forceLegacyPearlKnockback(Player player, PlayerKnockbackData data,
                                          Vector expected) {
        Bukkit.getScheduler().runTask(AlleyPlugin.getInstance(), () -> {
            // A native Bot is a valid server-side Player without a real client.
            if ((!player.isOnline() && !player.isValid()) || player.isDead()) {
                if (data.getVelocity() == expected) {
                    data.setVelocity(null);
                }
                return;
            }
            if (data.getVelocity() != expected) return;

            Vector delivered = consumePendingKnockback(player, player.getVelocity());
            if (delivered != null) player.setVelocity(delivered);
        });
    }

    /**
     * Native player attacks already apply the 0.6 horizontal attacker slowdown after
     * their EntityKnockbackByEntityEvent. Only supply it when that native branch did not run.
     */
    private void queueLegacyAttackerSlowdownFallback(Player attacker, Player victim) {
        UUID attackerId = attacker.getUniqueId();
        PendingLegacyAttackerSlowdown pending = new PendingLegacyAttackerSlowdown(
                victim.getUniqueId(), Bukkit.getCurrentTick(), attacker.getVelocity().clone());
        pendingLegacyAttackerSlowdowns.put(attackerId, pending);

        Bukkit.getScheduler().runTask(AlleyPlugin.getInstance(), () -> {
            if (!pendingLegacyAttackerSlowdowns.remove(attackerId, pending)
                    || pending.nativeKnockbackObserved()
                    || !attacker.isOnline()
                    || attacker.isDead()
                    || !hasLegacySwordCombat(attacker)) {
                return;
            }

            Vector hitVelocity = pending.hitVelocity();
            Vector currentVelocity = attacker.getVelocity();
            attacker.setVelocity(new Vector(
                    hitVelocity.getX() * 0.6D,
                    currentVelocity.getY(),
                    hitVelocity.getZ() * 0.6D));
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

        if (hasLegacySwordCombat(victim)) {
            return;
        }

        if (!victim.isOnGround()) {
            knockback.setY(nativeVelocity.getY());
        }
    }

    private record AttackCooldownSnapshot(UUID targetId, float strength, int tick) {
    }

    private static final class PendingLegacyAttackerSlowdown {
        private final UUID victimId;
        private final int tick;
        private final Vector hitVelocity;
        private boolean nativeKnockbackObserved;

        private PendingLegacyAttackerSlowdown(UUID victimId, int tick, Vector hitVelocity) {
            this.victimId = victimId;
            this.tick = tick;
            this.hitVelocity = hitVelocity;
        }

        private UUID victimId() {
            return victimId;
        }

        private int tick() {
            return tick;
        }

        private Vector hitVelocity() {
            return hitVelocity;
        }

        private boolean nativeKnockbackObserved() {
            return nativeKnockbackObserved;
        }

        private void markNativeKnockbackObserved() {
            nativeKnockbackObserved = true;
        }
    }
}
