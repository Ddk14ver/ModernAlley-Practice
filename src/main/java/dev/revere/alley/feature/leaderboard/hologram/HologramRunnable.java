package dev.revere.alley.feature.leaderboard.hologram;

import dev.revere.alley.AlleyPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 * @author Alley
 * @project Alley
 * @since 02/07/2025
 *
 * Periodic update task for a hologram. Runs every 30 seconds (600 ticks).
 * 全息图的定期更新任务。每30秒（600刻）运行一次。
 */
public class HologramRunnable extends BukkitRunnable {
    private final Hologram hologram;
    private BukkitTask task;

    /** Update interval in ticks (30 seconds = 600 ticks). */
    private static final long UPDATE_INTERVAL = 600L;

    /** Initial delay before first update (3 seconds). */
    private static final long INITIAL_DELAY = 60L;

    public HologramRunnable(Hologram hologram) {
        this.hologram = hologram;
    }

    public Hologram getHologram() {
        return this.hologram;
    }

    /**
     * Starts the periodic update task.
     */
    public void start() {
        if (this.task != null) return;
        this.task = this.runTaskTimer(AlleyPlugin.getInstance(), INITIAL_DELAY, UPDATE_INTERVAL);
    }

    /**
     * Cancels the periodic update task.
     */
    public void cancel() {
        if (this.task != null) {
            this.task.cancel();
            this.task = null;
        }
    }

    @Override
    public void run() {
        if (this.hologram.isEnabled() && this.hologram.getBaseLocation() != null) {
            this.hologram.updateContent();
        }
    }
}
