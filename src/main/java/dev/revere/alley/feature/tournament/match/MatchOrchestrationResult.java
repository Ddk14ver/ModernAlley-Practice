package dev.revere.alley.feature.tournament.match;

import dev.revere.alley.feature.match.Match;
import dev.revere.alley.feature.tournament.model.TournamentParticipant;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * @author Remi
 * @project alley-practice
 * @date 8/08/2025
 */
@Getter
@RequiredArgsConstructor
public class MatchOrchestrationResult {
    private final List<Match> createdMatches;
    private final List<TournamentParticipant> byeParticipants;
    private final boolean successful;
    private final String errorMessage;

    /**
     * Creates a success result with created matches and byes.
     * 创建一个包含已创建比赛和轮空参与者的成功结果。
     *
     * @param matches Created matches.
     *                已创建的比赛列表。
     * @param byes    Bye participants.
     *                轮空参与者列表。
     * @return Success result.
     *         成功结果。
     */
    public static MatchOrchestrationResult success(List<Match> matches, List<TournamentParticipant> byes) {
        return new MatchOrchestrationResult(matches, byes, true, null);
    }

    /**
     * Creates a failure result with error message.
     * 创建一个包含错误消息的失败结果。
     *
     * @param error The error message.
     *              错误消息。
     * @return Failure result.
     *         失败结果。
     */
    public static MatchOrchestrationResult failure(String error) {
        return new MatchOrchestrationResult(null, null, false, error);
    }
}