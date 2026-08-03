package dev.revere.alley.feature.tournament.match.internal;

import dev.revere.alley.bootstrap.annotation.Service;
import dev.revere.alley.feature.match.model.GameParticipant;
import dev.revere.alley.feature.match.model.TeamGameParticipant;
import dev.revere.alley.feature.match.model.internal.MatchGamePlayer;
import dev.revere.alley.feature.tournament.match.MatchParticipantFactory;
import dev.revere.alley.feature.tournament.model.TournamentParticipant;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * @author Remi
 * @project alley-practice
 * @date 8/08/2025
 */
@Service(provides = MatchParticipantFactory.class, priority = 1100)
public class DefaultMatchParticipantFactory implements MatchParticipantFactory {
    @Override
    public GameParticipant<MatchGamePlayer> buildParticipant(TournamentParticipant participant) {
        List<Player> online = participant.getOnlinePlayers();
        if (online.isEmpty()) {
            return new GameParticipant<>(new MatchGamePlayer(participant.getLeaderUuid(), participant.getLeaderName()));
        }

        if (online.size() == 1) {
            Player leader = online.get(0);
            return new GameParticipant<>(new MatchGamePlayer(leader.getUniqueId(), leader.getName()));
        }

        Player leader = leaderOrFirst(online, participant);
        TeamGameParticipant<MatchGamePlayer> teamParticipant = new TeamGameParticipant<>(new MatchGamePlayer(leader.getUniqueId(), leader.getName()));

        for (Player player : online) {
            if (player.getUniqueId().equals(leader.getUniqueId())) {
                continue;
            }
            teamParticipant.addPlayer(new MatchGamePlayer(player.getUniqueId(), player.getName()));
        }
        return teamParticipant;
    }

    /**
     * Picks the leader if online, otherwise first online member.
     * 如果领队在线则选择领队，否则选择第一个在线成员。
     *
     * @param online      Online players.
     *                    在线玩家列表。
     * @param participant The tournament team.
     *                    锦标赛队伍。
     * @return The chosen leader player.
     *         被选中的领队玩家。
     */
    private Player leaderOrFirst(List<Player> online, TournamentParticipant participant) {
        for (Player player : online) {
            if (player.getUniqueId().equals(participant.getLeaderUuid())) {
                return player;
            }
        }
        return online.get(0);
    }
}