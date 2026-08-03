package dev.revere.alley.feature.knockback.hitbox;

import dev.revere.alley.feature.knockback.KnockbackManager;
import dev.revere.alley.feature.knockback.KnockbackProfile;
import dev.revere.alley.feature.knockback.data.PlayerKnockbackData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 04/07/2026
 *
 * Instant server-side hit detection with configurable AABB.
 * Raytrace fires synchronously on swing — no delay, no move dependency.
 */
public class HitDetection implements Listener {
    private final KnockbackManager manager;
    /** Tracks last swing tick to prevent double-processing in same tick. */
    private final Map<UUID, Integer> lastSwingTick = new ConcurrentHashMap<>();

    public HitDetection(KnockbackManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onSwing(PlayerAnimationEvent e) {
        if (e.getAnimationType() != PlayerAnimationType.ARM_SWING) return;
        Player attacker = e.getPlayer();

        // Only trigger on melee weapons — not fishing rods, buckets, blocks, etc.
        if (!isMeleeWeapon(attacker.getInventory().getItemInMainHand().getType())) return;

        PlayerKnockbackData data = manager.getPlayerData(attacker);
        KnockbackProfile profile = manager.getProfile(data.getProfileName());
        if (profile == null) return;
        if (profile.getHitboxLength() <= 0.6 && profile.getHitboxHeight() <= 1.8) return;

        int currentTick = org.bukkit.Bukkit.getCurrentTick();
        Integer lastTick = lastSwingTick.put(attacker.getUniqueId(), currentTick);
        if (lastTick != null && lastTick == currentTick) return; // deduplicate

        // Instant synchronous raytrace — no delay
        detectAndAttack(attacker, profile);
    }

    // Cancel last swing on right-click or item change to prevent stale swings leaking into rod casts
    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            lastSwingTick.remove(e.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent e) {
        lastSwingTick.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        lastSwingTick.remove(e.getPlayer().getUniqueId());
    }

    private boolean isMeleeWeapon(Material mat) {
        return switch (mat) {
            case WOODEN_SWORD, STONE_SWORD, IRON_SWORD, GOLDEN_SWORD, DIAMOND_SWORD, NETHERITE_SWORD,
                 WOODEN_AXE, STONE_AXE, IRON_AXE, GOLDEN_AXE, DIAMOND_AXE, NETHERITE_AXE,
                 WOODEN_PICKAXE, STONE_PICKAXE, IRON_PICKAXE, GOLDEN_PICKAXE, DIAMOND_PICKAXE, NETHERITE_PICKAXE,
                 WOODEN_SHOVEL, STONE_SHOVEL, IRON_SHOVEL, GOLDEN_SHOVEL, DIAMOND_SHOVEL, NETHERITE_SHOVEL,
                 WOODEN_HOE, STONE_HOE, IRON_HOE, GOLDEN_HOE, DIAMOND_HOE, NETHERITE_HOE,
                 TRIDENT, MACE, STICK, AIR -> true;
            default -> false;
        };
    }

    private void detectAndAttack(Player attacker, KnockbackProfile profile) {
        Location eye = attacker.getEyeLocation();
        Vector dir = eye.getDirection();
        Ray ray = new Ray(eye.toVector(), dir);

        AABB expandedBox = AABB.fromSize(profile.getHitboxLength(), profile.getHitboxHeight());
        AABB vanillaBox = AABB.fromSize(0.6, 1.8); // vanilla 1.21 hitbox
        double maxDist = 3.5;

        Player closest = null;
        boolean alreadyHitByVanilla = false;

        for (Entity entity : attacker.getNearbyEntities(maxDist + 2, maxDist + 2, maxDist + 2)) {
            if (!(entity instanceof Player victim)) continue;
            if (victim.getUniqueId().equals(attacker.getUniqueId())) continue;
            if (victim.isDead()) continue;

            Vector hit = expandedBox.translate(victim.getLocation().toVector()).intersectsRay(ray, 0f, (float) maxDist);
            if (hit != null) {
                // Check if vanilla (0.6-wide) hitbox would also hit this player
                if (vanillaBox.translate(victim.getLocation().toVector()).intersectsRay(ray, 0f, (float) maxDist) != null) {
                    alreadyHitByVanilla = true; // vanilla will handle this — don't double-attack
                    break;
                }
                double d = eye.toVector().distance(hit);
                if (d < 3.5 && closest == null) { closest = victim; }
            }
        }

        // Only fire custom attack if vanilla would have MISSED
        if (closest == null || alreadyHitByVanilla) return;

        // Block check
        Location vEye = closest.getEyeLocation();
        Vector toVictim = vEye.toVector().subtract(eye.toVector()).normalize();
        Location cur = eye.clone();
        double totalDist = eye.distance(vEye);
        for (double d = 0; d < totalDist; d += 0.5) {
            cur.add(toVictim.clone().multiply(0.5));
            if (cur.getBlock().getType().isSolid()) return;
        }

        // Fire attack immediately
        PlayerKnockbackData aData = manager.getPlayerData(attacker);
        aData.setServerSideHit(true);
        attacker.attack(closest);
        aData.setServerSideHit(false);
        if (profile.isStopSprint()) {
            attacker.setSprinting(false);
        }
    }
}
