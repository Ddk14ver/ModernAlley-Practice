package dev.revere.alley.feature.tournament.formation;

import dev.revere.alley.bootstrap.lifecycle.Service;
import dev.revere.alley.feature.tournament.model.Tournament;
import dev.revere.alley.feature.tournament.model.TournamentParticipant;

import java.util.List;
/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 10/07/2026
 */
public interface TournamentTeamFormationService extends Service {
    /**
     * Forms balanced teams for a tournament.
     * 为锦标赛组建平衡的队伍。
     *
     * @param tournament The tournament to form teams for
     *                   需要组建队伍的锦标赛
     * @return List of formed teams
     *         组建完成的队伍列表
     */
    List<TournamentParticipant> formTeamsForTournament(Tournament tournament);
}