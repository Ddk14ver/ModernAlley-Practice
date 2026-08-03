package dev.revere.alley.feature.leaderboard;

import lombok.Getter;

/**
 * 排行榜类型枚举，定义各种排行榜类别（排位、非排位、连胜、FFA、锦标赛等）。
 * Leaderboard type enumeration, defining various leaderboard categories (ranked, unranked, win streak, FFA, tournament, etc.).
 *
 * @author Emmy
 * @project Alley
 * @date 17/11/2024 - 14:13
 */
@Getter
public enum LeaderboardType {
    UNRANKED("Unranked (All time)"),
    UNRANKED_MONTHLY("Unranked (Monthly)"),
    WIN_STREAK("Win Streak"),
    FFA("FFA"),
    RANKED("Ranked"),
    TOURNAMENT("Tournament");

    private final String name;

    /**
     * Constructor for the EnumLeaderboardType.
     * EnumLeaderboardType 的构造方法。
     *
     * @param name The name of the leaderboard type.
     *             排行榜类型的名称
     */
    LeaderboardType(String name) {
        this.name = name;
    }
}