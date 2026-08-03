package dev.revere.alley.feature.tournament.execution;

import dev.revere.alley.bootstrap.lifecycle.Service;
import dev.revere.alley.feature.tournament.engine.TournamentEvent;
import dev.revere.alley.feature.tournament.model.Tournament;

/**
 * @author Remi
 * @project alley-practice
 * @date 8/08/2025
 */
public interface TournamentExecutionStrategy extends Service {
    /**
     * Handles an external event (join, leave, match end, timers) during
     * tournament execution.
     * 在锦标赛执行期间处理外部事件（加入、离开、比赛结束、计时器）。
     *
     * @param tournament The tournament receiving the event.
     *                   接收事件的锦标赛。
     * @param event      The event to process.
     *                   要处理的事件。
     * @return The result of event processing.
     *         事件处理的结果。
     */
    ExecutionResult handleEvent(Tournament tournament, TournamentEvent event);
}