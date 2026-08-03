package dev.revere.alley.feature.leaderboard;

import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.bootstrap.lifecycle.Service;
import dev.revere.alley.feature.leaderboard.data.LeaderboardPlayerData;

import java.util.List;

/**
 * 排行榜服务接口，定义获取排行榜条目和强制重新计算的操作。
 * Leaderboard service interface, defining operations for retrieving leaderboard entries and forcing recalculation.
 *
 * @author Remi
 * @project alley-practice
 * @date 2/07/2025
 */
public interface LeaderboardService extends Service {
    /**
     * Gets the sorted leaderboard data for a specific kit and type.
     * 获取指定职业装备和类型的已排序排行榜数据。
     * <p>
     * This method performs a live, in-memory refresh for all online players
     * before returning the data, ensuring it is always up-to-date.
     * 此方法在返回数据之前会对所有在线玩家执行实时的内存刷新，确保数据始终是最新的。
     *
     * @param kit  The kit to get the leaderboard for.
     *             要获取排行榜数据的职业装备
     * @param type The type of leaderboard to retrieve.
     *             要检索的排行榜类型
     * @return A sorted list of LeaderboardPlayerData.
     *         已排序的 LeaderboardPlayerData 列表
     */
    List<LeaderboardPlayerData> getLeaderboardEntries(Kit kit, LeaderboardType type);

    /**
     * Triggers a full, deep recalculation of all leaderboards from the database.
     * 从数据库触发所有排行榜的完整深度重新计算。
     * This is a heavy operation and should only be used for a manual refresh command.
     * 这是一个重量级操作，仅应用于手动刷新命令。
     */
    void forceRecalculateAll();
}