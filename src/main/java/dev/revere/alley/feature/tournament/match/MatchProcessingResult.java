package dev.revere.alley.feature.tournament.match;

import dev.revere.alley.feature.tournament.model.TournamentParticipant;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Remi
 * @project alley-practice
 * @date 8/08/2025
 */
@Getter
@RequiredArgsConstructor
public class MatchProcessingResult {
    private final TournamentParticipant winner;
    private final TournamentParticipant loser;
    private final boolean successful;
    private final String errorMessage;

    /**
     * Creates a success result with winner and loser.
     * 创建一个包含获胜者和失败者的成功结果。
     *
     * @param winner The winning participant.
     *               获胜的参与者。
     * @param loser The losing participant.
     *              失败的参与者。
     * @return Success result.
     *         成功结果。
     */
    public static MatchProcessingResult success(TournamentParticipant winner, TournamentParticipant loser) {
        return new MatchProcessingResult(winner, loser, true, null);
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
    public static MatchProcessingResult failure(String error) {
        return new MatchProcessingResult(null, null, false, error);
    }
}