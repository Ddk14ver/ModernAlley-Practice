package dev.revere.alley.feature.queue;

import dev.revere.alley.library.menu.Menu;
import dev.revere.alley.feature.queue.internal.QueueTask;
import dev.revere.alley.bootstrap.lifecycle.Service;

import java.util.List;

/**
 * @author Remi
 * @project alley-practice
 * @date 2/07/2025
 */
public interface QueueService extends Service {
    /**
     * Gets the list of all active queues.
     * 获取所有活跃队列的列表。
     *
     * @return A list of Queue instances.
     *         Queue 实例的列表。
     */
    List<Queue> getQueues();

    /**
     * Gets the menu UI for players to select a queue.
     * 获取供玩家选择队列的菜单界面。
     *
     * @return The active Menu instance for queues.
     *         队列的活动菜单实例。
     */
    Menu getQueueMenu();

    /**
     * Clears and re-populates all queues based on the currently loaded kits.
     * 清除并根据当前加载的装备包重新填充所有队列。
     */
    void reloadQueues();

    /**
     * Gets the total number of players currently playing in a specific game type (e.g., Ranked, FFA).
     * 获取当前在特定游戏类型（例如 Ranked、FFA）中游玩的玩家总数。
     *
     * @param queueName The name of the game type.
     *                  游戏类型的名称。
     * @return The number of players.
     *         玩家数量。
     */
    int getPlayerCountOfGameType(String queueName);

    QueueTask getQueueTask();
}