package dev.revere.alley.feature.match.data.types;

import dev.revere.alley.feature.match.data.MatchData;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

/**
 * @author Emmy
 * @project Alley
 * @since 29/05/2025
 */
@Getter
public class MatchDataTeam extends MatchData {
    private final List<UUID> players;
    private final List<UUID> opponentPlayers;

    private final List<UUID> winners;

    private final String winnerTeam;

    /**
     * Constructor for the MatchDataTeamImpl class.
     * MatchDataTeamImpl类的构造函数。
     *
     * @param kit              The kit used in the match.
     *                         比赛中使用的工具包。
     * @param arena            The arena where the match took place.
     *                         比赛发生的竞技场。
     * @param players          The list of UUIDs of players on the team.
     *                         队伍中玩家的UUID列表。
     * @param opponentPlayers  The list of UUIDs of players on the opposing team.
     *                         对方队伍中玩家的UUID列表。
     * @param winners          The list of UUIDs of winning players.
     *                         获胜玩家的UUID列表。
     * @param winnerTeam       The name of the winning team.
     *                         获胜队伍的名称。
     */
    public MatchDataTeam(String kit, String arena, List<UUID> players, List<UUID> opponentPlayers, List<UUID> winners, String winnerTeam) {
        super(kit, arena);
        this.players = players;
        this.opponentPlayers = opponentPlayers;
        this.winners = winners;
        this.winnerTeam = winnerTeam;
    }

}