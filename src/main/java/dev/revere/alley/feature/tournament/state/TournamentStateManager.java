package dev.revere.alley.feature.tournament.state;

import dev.revere.alley.bootstrap.lifecycle.Service;
import dev.revere.alley.feature.tournament.model.Tournament;
import dev.revere.alley.feature.tournament.model.TournamentState;

/**
 * @author Remi
 * @project alley-practice
 * @date 8/08/2025
 */
public interface TournamentStateManager extends Service {
    /**
     * Validates if a state transition is allowed.
     * 验证是否允许状态转换。
     *
     * @param tournament The tournament to check.
     *                   要检查的锦标赛。
     * @param newState The target state.
     *                 目标状态。
     * @return True if transition is valid.
     *         如果转换有效则返回 true。
     */
    boolean canTransitionTo(Tournament tournament, TournamentState newState);

    /**
     * Executes a validated state transition, mutating the tournament.
     * 执行经过验证的状态转换，修改锦标赛状态。
     *
     * @param tournament The tournament to transition.
     *                   要转换的锦标赛。
     * @param newState The target state.
     *                 目标状态。
     * @return The updated tournament.
     *         更新后的锦标赛。
     */
    Tournament transitionState(Tournament tournament, TournamentState newState);
}