package dev.revere.alley.feature.leaderboard.data;

import dev.revere.alley.feature.kit.Kit;
import lombok.Data;

import java.util.UUID;

/**
 * 排行榜玩家数据模型，存储排行榜中单个玩家的信息。
 * Leaderboard player data model, storing information about a single player on the leaderboard.
 *
 * @author Emmy
 * @project Alley
 * @date 3/3/2025
 */
@Data
public class LeaderboardPlayerData {
    private final String name;
    private final UUID uuid;
    private final Kit kit;
    private int value;

    /**
     * Constructor for the LeaderboardEntry class.
     * LeaderboardEntry 类的构造方法。
     *
     * @param name  The name of the player
     *              玩家的名称
     * @param uuid  The UUID of the player
     *              玩家的 UUID
     * @param kit   The kit of the player
     *              玩家的职业装备
     * @param value The value of the player
     *              玩家的统计数值
     */
    public LeaderboardPlayerData(String name, UUID uuid, Kit kit, int value) {
        this.name = name;
        this.uuid = uuid;
        this.kit = kit;
        this.value = value;
    }
}