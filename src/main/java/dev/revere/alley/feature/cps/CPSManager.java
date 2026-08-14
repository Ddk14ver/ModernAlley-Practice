package dev.revere.alley.feature.cps;

import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * Tracks clicks per second using a sliding 1-second window.
 * Peak (max) CPS is updated immediately when a click enters the window.
 */
public class CPSManager {
    private static final long WINDOW_NANOS = TimeUnit.SECONDS.toNanos(1L);

    /** Player UUID to click timestamps in nanoseconds within the last second. */
    private final Map<UUID, ConcurrentLinkedQueue<Long>> clicks = new ConcurrentHashMap<>();

    /** Player UUID to highest observed CPS. */
    private final Map<UUID, Integer> maxCps = new ConcurrentHashMap<>();

    /**
     * Records a left-click for the given player.
     * Called for each main-hand arm-animation packet.
     */
    public void recordClick(UUID uuid) {
        long now = System.nanoTime();
        ConcurrentLinkedQueue<Long> queue = clicks.computeIfAbsent(
                uuid, ignored -> new ConcurrentLinkedQueue<>());
        queue.offer(now);
        evictExpired(queue, now);
        maxCps.merge(uuid, queue.size(), Math::max);
    }

    /** Convenience overload. */
    public void recordClick(Player player) {
        if (player == null) return;
        recordClick(player.getUniqueId());
    }

    /** Current CPS: number of clicks recorded in the last second. */
    public int getCPS(UUID uuid) {
        if (uuid == null) return 0;
        ConcurrentLinkedQueue<Long> queue = clicks.get(uuid);
        if (queue == null) return 0;
        evictExpired(queue, System.nanoTime());
        return queue.size();
    }

    /** Convenience overload. */
    public int getCPS(Player player) {
        if (player == null) return 0;
        return getCPS(player.getUniqueId());
    }

    /** Highest CPS observed this session for the player. */
    public int getMaxCPS(UUID uuid) {
        if (uuid == null) return 0;
        return maxCps.getOrDefault(uuid, 0);
    }

    /** Convenience overload. */
    public int getMaxCPS(Player player) {
        if (player == null) return 0;
        return getMaxCPS(player.getUniqueId());
    }

    /** Resets the max-CPS counter (called at match start). */
    public void resetMaxCPS(UUID uuid) {
        maxCps.remove(uuid);
    }

    /** Removes a player completely (called on quit). */
    public void remove(UUID uuid) {
        clicks.remove(uuid);
        maxCps.remove(uuid);
    }

    /**
     * Must be called every tick to evict expired timestamps.
     */
    public void tick() {
        long now = System.nanoTime();
        for (Map.Entry<UUID, ConcurrentLinkedQueue<Long>> entry : clicks.entrySet()) {
            ConcurrentLinkedQueue<Long> queue = entry.getValue();
            evictExpired(queue, now);
            if (queue.isEmpty()) clicks.remove(entry.getKey(), queue);
        }
    }

    private void evictExpired(ConcurrentLinkedQueue<Long> queue, long now) {
        long cutoff = now - WINDOW_NANOS;
        Long head;
        while ((head = queue.peek()) != null && head < cutoff) {
            queue.poll();
        }
    }
}
