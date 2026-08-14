package dev.revere.alley.feature.cosmetic;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Remi
 * @project alley-practice
 * @date 4/08/2025
 */
public class CosmeticListener implements Listener {
    private static final Map<UUID, Long> lastMoveTimes = new ConcurrentHashMap<>();

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() != event.getTo().getBlockX() ||
                event.getFrom().getBlockY() != event.getTo().getBlockY() ||
                event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
            lastMoveTimes.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        lastMoveTimes.remove(event.getPlayer().getUniqueId());
    }

    /**
     * Checks if a player has been standing still longer than the specified delay.
     * 检查玩家静止不动的时间是否超过指定的延迟。
     *
     * @param player      The player to check.
     * @param player      要检查的玩家。
     * @param delayMillis The required delay in milliseconds.
     * @param delayMillis 所需的延迟（毫秒）。
     * @return true if the player is considered still.
     * @return 如果玩家被视为静止，则返回 true。
     */
    public static boolean isPlayerStill(Player player, long delayMillis) {
        return (System.currentTimeMillis() - lastMoveTimes.getOrDefault(player.getUniqueId(), 0L)) > delayMillis;
    }
}
