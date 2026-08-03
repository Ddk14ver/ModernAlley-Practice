package dev.revere.alley.feature.tournament.task;

import dev.revere.alley.bootstrap.lifecycle.Service;

/**
 * @author Remi
 * @project alley-practice
 * @date 8/08/2025
 */
public interface TournamentCountdownService extends Service {
    /**
     * Gets the current start countdown task runnable.
     * 获取当前的开始倒计时任务可运行对象。
     *
     * @return The TournamentStartTask, or null if none is active.
     *         TournamentStartTask 实例，如果没有活跃的任务则返回 null。
     */
    TournamentStartTask getStartTask();

    /**
     * Gets the current round start countdown task runnable.
     * 获取当前的回合开始倒计时任务可运行对象。
     *
     * @return The TournamentRoundStartTask, or null if none is active.
     *         TournamentRoundStartTask 实例，如果没有活跃的任务则返回 null。
     */
    TournamentRoundStartTask getRoundStartTask();

    /**
     * Registers the active start countdown runnable.
     * 注册活跃的开始倒计时可运行对象。
     *
     * @param task The start countdown runnable.
     *             开始倒计时的可运行对象。
     */
    void setStartTask(TournamentStartTask task);

    /**
     * Clears the currently registered start countdown runnable.
     * 清除当前注册的开始倒计时可运行对象。
     */
    void clearStartTask();

    /**
     * Registers the active round start countdown runnable.
     * 注册活跃的回合开始倒计时可运行对象。
     *
     * @param task The round start countdown runnable.
     *             回合开始倒计时的可运行对象。
     */
    void setRoundStartTask(TournamentRoundStartTask task);

    /**
     * Clears the currently registered round start countdown runnable.
     * 清除当前注册的回合开始倒计时可运行对象。
     */
    void clearRoundStartTask();
}