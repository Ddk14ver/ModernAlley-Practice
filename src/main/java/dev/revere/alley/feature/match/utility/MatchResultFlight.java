package dev.revere.alley.feature.match.utility;

import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks the short-lived flight state granted by a match result. This is kept
 * separate from the player's saved preference so it can survive death/respawn
 * without turning lobby flight into a permanent permission.
 */
public final class MatchResultFlight {
    private static final Set<UUID> PENDING = ConcurrentHashMap.newKeySet();

    private MatchResultFlight() {
    }

    public static void enable(Player player) {
        if (player == null) {
            return;
        }

        PENDING.add(player.getUniqueId());
        apply(player);
    }

    public static void apply(Player player) {
        if (player == null || !PENDING.contains(player.getUniqueId()) || !player.isOnline()) {
            return;
        }

        player.setAllowFlight(true);
        if (!player.isDead()) {
            player.setFlying(true);
        }
    }

    public static boolean isPending(Player player) {
        return player != null && PENDING.contains(player.getUniqueId());
    }

    public static void clear(Player player) {
        if (player != null) {
            PENDING.remove(player.getUniqueId());
        }
    }
}
