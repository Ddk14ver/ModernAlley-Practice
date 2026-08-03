package dev.revere.alley.feature.tournament.engine;

import dev.revere.alley.bootstrap.lifecycle.Service;
import dev.revere.alley.feature.tournament.model.Tournament;

/**
 * @author Remi
 * @project alley-practice
 * @date 8/08/2025
 */
public interface TournamentEngine extends Service {
    /**
     * Initializes and returns a new tournament from configuration.
     * 根据配置初始化并返回一个新的锦标赛。
     *
     * @param configuration The tournament setup configuration.
     *                      锦标赛的初始配置。
     * @return The initialized tournament instance.
     *         初始化后的锦标赛实例。
     */
    Tournament initializeTournament(TournamentConfiguration configuration);

    /**
     * Processes an external event against a tournament, advancing or mutating
     * its lifecycle via the configured strategy.
     * 通过配置的策略处理锦标赛的外部事件，推进或改变其生命周期。
     *
     * @param tournament The tournament to mutate.
     *                   要改变的锦标赛。
     * @param event      The event that occurred.
     *                   发生的事件。
     */
    void processEvent(Tournament tournament, TournamentEvent event);
}