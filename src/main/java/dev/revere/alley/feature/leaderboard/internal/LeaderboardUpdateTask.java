package dev.revere.alley.feature.leaderboard.internal;

import org.bukkit.scheduler.BukkitRunnable;
import lombok.RequiredArgsConstructor;

/**
 * 排行榜更新后台任务，用于定期刷新排行榜数据。
 * Leaderboard update background task, used to periodically refresh leaderboard data.
 *
 * @author Emmy
 * @project Alley
 * @since 04/03/2025
 */
@RequiredArgsConstructor
public class LeaderboardUpdateTask extends BukkitRunnable {
    private final LeaderboardServiceImpl leaderboardService;

    @Override
    public void run() {
        this.leaderboardService.forceRecalculateAll();
    }
}
