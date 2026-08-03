package dev.revere.alley.feature.tournament.execution;

import dev.revere.alley.feature.tournament.model.Tournament;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Remi
 * @project alley-practice
 * @date 8/08/2025
 */
@Getter
@RequiredArgsConstructor
public class ExecutionResult {
    private final ExecutionStatus status;
    private final String message;
    private final Tournament updatedTournament;

    /**
     * Status of an execution result.
     * 执行结果的状态。
     */
    public enum ExecutionStatus {
        CONTINUE,
        TRANSITION,
        COMPLETE,
        ERROR
    }

    /**
     * Creates a continue result.
     * 创建一个继续执行的结果。
     *
     * @param tournament The current tournament.
     *                   当前的锦标赛。
     * @return The continue result.
     *         继续执行的结果。
     */
    public static ExecutionResult continueExecution(Tournament tournament) {
        return new ExecutionResult(ExecutionStatus.CONTINUE, null, tournament);
    }

    /**
     * Creates a transition result with message.
     * 创建一个带消息的状态转换结果。
     *
     * @param tournament The tournament.
     *                   锦标赛。
     * @param message    A message describing the transition.
     *                   描述转换的消息。
     * @return The transition result.
     *         状态转换的结果。
     */
    public static ExecutionResult transitionState(
            Tournament tournament, String message) {
        return new ExecutionResult(ExecutionStatus.TRANSITION, message, tournament);
    }

    /**
     * Creates a completion result with message.
     * 创建一个带消息的完成结果。
     *
     * @param tournament The tournament.
     *                   锦标赛。
     * @param message    A message describing the completion.
     *                   描述完成的消息。
     * @return The completion result.
     *         完成的结果。
     */
    public static ExecutionResult complete(Tournament tournament, String message) {
        return new ExecutionResult(ExecutionStatus.COMPLETE, message, tournament);
    }
}