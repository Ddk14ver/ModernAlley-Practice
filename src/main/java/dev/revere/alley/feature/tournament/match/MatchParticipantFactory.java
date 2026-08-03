package dev.revere.alley.feature.tournament.match;

import dev.revere.alley.bootstrap.lifecycle.Service;
import dev.revere.alley.feature.match.model.GameParticipant;
import dev.revere.alley.feature.match.model.internal.MatchGamePlayer;
import dev.revere.alley.feature.tournament.model.TournamentParticipant;

/**
 * @author Remi
 * @project alley-practice
 * @date 8/08/2025
 */
public interface MatchParticipantFactory extends Service {
    /**
     * Creates a GameParticipant for a tournament team, producing a
     * TeamGameParticipant for teams (>1) or a solo participant for 1.
     * 为锦标赛队伍创建 GameParticipant，队伍人数大于1时生成 TeamGameParticipant，
     * 单人时生成单人参与者。
     *
     * @param participant The tournament team.
     *                    锦标赛队伍。
     * @return The constructed GameParticipant.
     *         构建好的 GameParticipant。
     */
    GameParticipant<MatchGamePlayer> buildParticipant(TournamentParticipant participant);
}