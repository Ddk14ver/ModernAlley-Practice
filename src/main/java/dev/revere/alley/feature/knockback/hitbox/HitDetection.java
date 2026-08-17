package dev.revere.alley.feature.knockback.hitbox;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.feature.knockback.KnockbackManager;
import dev.revere.alley.feature.knockback.KnockbackProfile;
import dev.revere.alley.feature.knockback.data.PlayerKnockbackData;
import dev.revere.alley.feature.match.MatchService;
import dev.revere.alley.feature.match.combat.legacy.LegacyCombatService;
import dev.revere.alley.feature.match.combat.legacy.LegacyHitboxes;
import dev.revere.alley.feature.match.internal.MatchServiceImpl;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adds configured hitbox reach without replacing normal client attacks.
 */
public class HitDetection implements Listener {
    private final KnockbackManager manager;
    private final Map<UUID, Integer> lastSwingTick = new ConcurrentHashMap<>();
    private final Map<UUID, PendingFallback> pendingFallbacks = new ConcurrentHashMap<>();
    private final Map<UUID, AcceptedHit> acceptedHits = new ConcurrentHashMap<>();

    public HitDetection(KnockbackManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onSwing(PlayerAnimationEvent event) {
        if (event.getAnimationType() != PlayerAnimationType.ARM_SWING) return;
        Player attacker = event.getPlayer();
        if (!isMeleeWeapon(attacker.getInventory().getItemInMainHand().getType())) return;

        KnockbackProfile profile = manager.getAppliedProfile(attacker);
        if (profile == null) return;
        if (!hasLegacySwordCombat(attacker)
                && profile.getHitboxLength() <= 0.6D && profile.getHitboxHeight() <= 1.8D) return;

        int currentTick = Bukkit.getCurrentTick();
        Integer lastTick = lastSwingTick.put(attacker.getUniqueId(), currentTick);
        if (lastTick != null && lastTick == currentTick) return;

        detectAndAttack(attacker, profile, currentTick);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMeleeAttack(EntityDamageByEntityEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) return;
        if (!(event.getDamager() instanceof Player attacker)
                || !(event.getEntity() instanceof Player victim)) return;

        int currentTick = Bukkit.getCurrentTick();
        acceptedHits.put(attacker.getUniqueId(), new AcceptedHit(victim.getUniqueId(), currentTick));
        PendingFallback pending = pendingFallbacks.get(attacker.getUniqueId());
        if (pending != null && pending.target().getUniqueId().equals(victim.getUniqueId())
                && currentTick >= pending.swingTick()) {
            pendingFallbacks.remove(attacker.getUniqueId());
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR
                || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            lastSwingTick.remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        clearPendingSwing(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        clearPlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        clearPlayer(event.getPlayer().getUniqueId());
    }

    /** Runs from KnockbackManager's existing single tick task. */
    public void tick() {
        int currentTick = Bukkit.getCurrentTick();
        acceptedHits.entrySet().removeIf(entry -> currentTick - entry.getValue().tick() > 1);

        for (Map.Entry<UUID, PendingFallback> entry : pendingFallbacks.entrySet()) {
            PendingFallback pending = entry.getValue();
            if (currentTick <= pending.swingTick()) continue;
            if (!pendingFallbacks.remove(entry.getKey(), pending)) continue;

            Player attacker = pending.attacker();
            Player target = pending.target();
            if (!isUsableAttacker(attacker) || !isUsableTarget(target)
                    || attacker.getWorld() != target.getWorld()) continue;

            AcceptedHit accepted = acceptedHits.get(attacker.getUniqueId());
            if (accepted != null && accepted.targetId().equals(target.getUniqueId())
                    && accepted.tick() >= pending.swingTick()) continue;
            if (manager.isInsideHurtResistanceWindow(target)) continue;

            KnockbackProfile profile = manager.getAppliedProfile(attacker);
            if (profile == null) continue;
            fireAttack(attacker, target, profile);
        }
    }

    public void clearPlayer(UUID playerId) {
        clearPendingSwing(playerId);
        acceptedHits.remove(playerId);
        pendingFallbacks.entrySet().removeIf(entry ->
                entry.getValue().target().getUniqueId().equals(playerId));
        acceptedHits.entrySet().removeIf(entry -> entry.getValue().targetId().equals(playerId));
    }

    public void clear() {
        lastSwingTick.clear();
        pendingFallbacks.clear();
        acceptedHits.clear();
    }

    private void detectAndAttack(Player attacker, KnockbackProfile profile, int currentTick) {
        Location eye = attacker.getEyeLocation();
        Vector direction = eye.getDirection();
        Ray ray = new Ray(eye.toVector(), direction);

        boolean legacyMelee = hasLegacySwordCombat(attacker);
        AABB expandedBox = null;
        if (!legacyMelee) {
            double halfWidth = profile.getHitboxLength() / 2.0D;
            double verticalExpansion = Math.max(0.0D, (profile.getHitboxHeight() - 1.8D) / 2.0D);
            expandedBox = new AABB(
                    new Vector(-halfWidth, -verticalExpansion, -halfWidth),
                    new Vector(halfWidth, profile.getHitboxHeight() - verticalExpansion, halfWidth));
        }
        AABB vanillaBox = AABB.fromSize(0.6D, 1.8D);
        double maxDistance = getMaxDistance(profile);
        if (maxDistance <= 0.0D) return;

        Player closest = null;
        Vector closestHit = null;
        double closestDistance = maxDistance;
        boolean vanillaHit = false;

        for (Entity entity : attacker.getNearbyEntities(
                maxDistance + 2.0D, maxDistance + 2.0D, maxDistance + 2.0D)) {
            if (!(entity instanceof Player victim) || !isUsableTarget(victim)
                    || victim.getUniqueId().equals(attacker.getUniqueId())) continue;

            Vector origin = victim.getLocation().toVector();
            Vector hit = legacyMelee
                    ? AABB.fromBoundingBox(LegacyHitboxes.meleeTarget(victim))
                            .intersectsRay(ray, 0.0F, (float) maxDistance)
                    : expandedBox.translate(origin).intersectsRay(
                            ray, 0.0F, (float) maxDistance);
            if (hit == null) continue;

            double distance = eye.toVector().distance(hit);
            if (distance <= closestDistance) {
                closest = victim;
                closestHit = hit;
                closestDistance = distance;
                vanillaHit = (legacyMelee
                        ? AABB.fromBoundingBox(victim.getBoundingBox())
                        : vanillaBox.translate(origin))
                        .intersectsRay(ray, 0.0F, (float) maxDistance) != null;
            }
        }

        if (closest == null || closestHit == null || isBlocked(eye, closestHit)) return;

        if (vanillaHit) {
            // Do not turn a genuine 1.8 hurt-window empty click into a delayed hit.
            if (manager.isInsideHurtResistanceWindow(closest)) return;
            AcceptedHit accepted = acceptedHits.get(attacker.getUniqueId());
            if (accepted != null && accepted.targetId().equals(closest.getUniqueId())
                    && accepted.tick() == currentTick) return;
            pendingFallbacks.put(attacker.getUniqueId(),
                    new PendingFallback(attacker, closest, currentTick));
            return;
        }

        fireAttack(attacker, closest, profile);
    }

    private boolean isBlocked(Location eye, Vector hit) {
        Vector offset = hit.clone().subtract(eye.toVector());
        double distance = offset.length();
        if (distance <= 1.0E-6D) return false;
        RayTraceResult block = eye.getWorld().rayTraceBlocks(
                eye, offset.multiply(1.0D / distance), distance,
                FluidCollisionMode.NEVER, true);
        return block != null;
    }

    private void fireAttack(Player attacker, Player target, KnockbackProfile profile) {
        PlayerKnockbackData data = manager.getPlayerData(attacker);
        boolean hasKnockbackEnchant = attacker.getInventory().getItemInMainHand()
                .getEnchantmentLevel(Enchantment.KNOCKBACK) > 0;
        boolean applyLegacySlowdown = manager.isLegacyKnockback(attacker)
                && (manager.hasLegacySprintKnockback(attacker) || hasKnockbackEnchant);
        Vector velocityBeforeAttack = applyLegacySlowdown ? attacker.getVelocity().clone() : null;
        data.setServerSideHit(true);
        try {
            attacker.attack(target);
        } finally {
            data.setServerSideHit(false);
        }
        if (velocityBeforeAttack != null) {
            Vector current = attacker.getVelocity();
            double slowdown = profile.getLegacyAttackerHorizontalSlowdown();
            manager.applyLegacyAttackerHorizontalMotion(
                    attacker, velocityBeforeAttack, current.getY(), slowdown);
        }
        if (profile.isStopSprint() && applyLegacySlowdown) attacker.setSprinting(false);
    }

    private double getMaxDistance(KnockbackProfile profile) {
        return Math.max(0.0D, Math.min(3.0D, profile.getEntityInteractionRange()));
    }

    private boolean isUsableTarget(Player player) {
        return player != null && player.isValid() && !player.isDead()
                && player.getGameMode() != GameMode.SPECTATOR;
    }

    private boolean isUsableAttacker(Player player) {
        return isUsableTarget(player)
                && isMeleeWeapon(player.getInventory().getItemInMainHand().getType());
    }

    private boolean hasLegacySwordCombat(Player player) {
        MatchService matchService = AlleyPlugin.getInstance().getService(MatchService.class);
        if (!(matchService instanceof MatchServiceImpl service)) return false;
        LegacyCombatService legacyCombat = service.getLegacyCombatService();
        return legacyCombat != null && legacyCombat.hasSwordBlockKB(player.getUniqueId());
    }

    private boolean isMeleeWeapon(Material material) {
        return switch (material) {
            case WOODEN_SWORD, STONE_SWORD, IRON_SWORD, GOLDEN_SWORD, DIAMOND_SWORD, NETHERITE_SWORD,
                 WOODEN_AXE, STONE_AXE, IRON_AXE, GOLDEN_AXE, DIAMOND_AXE, NETHERITE_AXE,
                 WOODEN_PICKAXE, STONE_PICKAXE, IRON_PICKAXE, GOLDEN_PICKAXE, DIAMOND_PICKAXE, NETHERITE_PICKAXE,
                 WOODEN_SHOVEL, STONE_SHOVEL, IRON_SHOVEL, GOLDEN_SHOVEL, DIAMOND_SHOVEL, NETHERITE_SHOVEL,
                 WOODEN_HOE, STONE_HOE, IRON_HOE, GOLDEN_HOE, DIAMOND_HOE, NETHERITE_HOE,
                 TRIDENT, MACE, STICK, AIR -> true;
            default -> false;
        };
    }

    private void clearPendingSwing(UUID playerId) {
        lastSwingTick.remove(playerId);
        pendingFallbacks.remove(playerId);
    }

    private record PendingFallback(Player attacker, Player target, int swingTick) { }
    private record AcceptedHit(UUID targetId, int tick) { }
}
