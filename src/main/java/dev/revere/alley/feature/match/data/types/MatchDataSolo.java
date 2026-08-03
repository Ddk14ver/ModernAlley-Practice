package dev.revere.alley.feature.match.data.types;

import dev.revere.alley.feature.match.data.MatchData;
import lombok.Getter;

import java.util.UUID;

/**
 * @author Emmy
 * @project Alley
 * @since 29/05/2025
 */
@Getter
public class MatchDataSolo extends MatchData {
    private final UUID winner;
    private final UUID loser;

    /**
     * Constructor for the MatchDataSoloImpl class.
     * MatchDataSoloImpl类的构造函数。
     *
     * @param kit    The kit used in the match.
     *               比赛中使用的工具包。
     * @param arena  The arena where the match took place.
     *               比赛发生的竞技场。
     * @param winner The UUID of the winning player.
     *               获胜玩家的UUID。
     * @param loser  The UUID of the losing player.
     *               失败玩家的UUID。
     */
    public MatchDataSolo(String kit, String arena, UUID winner, UUID loser) {
        super(kit, arena);
        this.loser = loser;
        this.winner = winner;
    }
}