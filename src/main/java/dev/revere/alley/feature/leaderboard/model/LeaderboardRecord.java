package dev.revere.alley.feature.leaderboard.model;

import dev.revere.alley.feature.leaderboard.data.LeaderboardPlayerData;
import dev.revere.alley.feature.leaderboard.LeaderboardType;
import lombok.Getter;

import java.util.List;

/**
 * 排行榜记录模型，表示某一特定类型和职业装备的完整排行榜快照。
 * Leaderboard record model, representing a complete leaderboard snapshot for a specific type and kit.
 *
 * @author Emmy
 * @project Alley
 * @since 03/03/2025
 */
@Getter
public class LeaderboardRecord {
    private final LeaderboardType type;
    private final List<LeaderboardPlayerData> participants;

    /**
     * Constructor for the LeaderboardRecord class.
     * LeaderboardRecord 类的构造方法。
     *
     * @param type         The type of the leaderboard.
     *                     排行榜的类型
     * @param participants The participants of the leaderboard.
     *                     排行榜的参与者列表
     */
    public LeaderboardRecord(LeaderboardType type, List<LeaderboardPlayerData> participants) {
        this.type = type;
        this.participants = participants;
    }
}
