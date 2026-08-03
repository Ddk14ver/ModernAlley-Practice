package dev.revere.alley.feature.tournament.match;

import dev.revere.alley.bootstrap.lifecycle.Service;
import dev.revere.alley.feature.match.Match;
import dev.revere.alley.feature.tournament.model.Tournament;
import dev.revere.alley.feature.tournament.model.TournamentParticipant;

import java.util.List;

/**
 * @author Remi
 * @project alley-practice
 * @date 8/08/2025
 */
public interface MatchOrchestrator extends Service {
    /**
     * Creates matches for the current round of participants.
     * 为当前轮的参与者创建比赛。
     *
     * @param tournament The tournament to create matches for.
     *                    要为其创建比赛的锦标赛。
     * @param participants The participants to match up.
     *                      要配对的参与者。
     * @return The orchestration result.
     *         编排结果。
     */
    MatchOrchestrationResult createRoundMatches(
            Tournament tournament, List<TournamentParticipant> participants);

    /**
     * Processes the completion of a tournament match.
     * 处理锦标赛比赛的完成。
     *
     * @param match The completed match.
     *              已完成的比赛。
     * @param tournament The tournament the match belongs to.
     *                    该比赛所属的锦标赛。
     * @return The processing result.
     *         处理结果。
     */
    MatchProcessingResult processMatchResult(Match match, Tournament tournament);

    /**
     * Checks if all active matches in a tournament round are complete.
     * 检查锦标赛某一轮中所有活跃比赛是否都已完成。
     *
     * @param tournament The tournament to check.
     *                    要检查的锦标赛。
     * @return True if all matches are complete.
     *         如果所有比赛都已完成则返回true。
     */
    boolean isRoundComplete(Tournament tournament);
}