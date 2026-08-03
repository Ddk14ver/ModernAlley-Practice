package dev.revere.alley.feature.server;

import dev.revere.alley.bootstrap.lifecycle.Service;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Set;

/**
 * @author Remi
 * @project alley-practice
 * @date 2/07/2025
 */
public interface ServerService extends Service {
    /**
     * Checks if players are currently allowed to join matchmaking queues.
     * 检查玩家当前是否被允许加入匹配队列。
     *
     * @return true if queueing is allowed, false otherwise.
     *         如果允许排队则为 true，否则为 false。
     */
    boolean isQueueingAllowed();

    /**
     * Sets whether players are allowed to join matchmaking queues.
     * 设置是否允许玩家加入匹配队列。
     *
     * @param allowed The new queueing status.
     *                新的排队状态。
     */
    void setQueueingAllowed(boolean allowed);

    /**
     * Forcefully ends all active matches on the server.
     * 强制结束服务器上所有正在进行的比赛。
     *
     * @param issuer The staff member who initiated this action (can be null for console).
     *               发起此操作的管理员（如果是控制台则可以为 null）。
     */
    void endAllMatches(Player issuer);

    /**
     * Disbands all active parties on the server.
     * 解散服务器上所有活跃的队伍。
     *
     * @param issuer The staff member who initiated this action (can be null for console).
     *               发起此操作的管理员（如果是控制台则可以为 null）。
     */
    void disbandAllParties(Player issuer);

    /**
     * Removes all players from all matchmaking queues.
     * 将所有玩家从所有匹配队列中移除。
     *
     * @param issuer The staff member who initiated this action (can be null for console).
     *               发起此操作的管理员（如果是控制台则可以为 null）。
     */
    void clearAllQueues(Player issuer);

    /**
     * Loads the list of materials that are blocked from crafting.
     * 加载被禁止合成的材料列表。
     * This method should be called during server startup to initialize the blocked crafting materials.
     * 此方法应在服务器启动时调用，以初始化被禁止合成的材料。
     */
    void loadBlockedCraftingItems();

    /**
     * Retrieves a list of materials that are blocked from crafting.
     * 获取被禁止合成的材料列表。
     *
     * @return A list of blocked crafting materials.
     *         被禁止合成的材料列表。
     */
    Set<Material> getBlockedCraftingItems();

    /**
     * Adds a material to the list of blocked crafting materials.
     * 将材料添加到被禁止合成列表中。
     *
     * @param material The material to block from crafting.
     *                 要禁止合成的材料。
     */
    void removeFromBlockedCraftingList(Material material);

    /**
     * Removes a material from the list of blocked crafting materials.
     * 从被禁止合成列表中移除材料。
     *
     * @param material The material to unblock from crafting.
     *                 要解除禁止合成的材料。
     */
    void addToBlockedCraftingList(Material material);

    /**
     * Checks if an item is craftable.
     * 检查物品是否可合成。
     *
     * @param material The material to check.
     *                 要检查的材料。
     * @return true if the crafting recipe is valid, false otherwise.
     *         如果合成配方有效则为 true，否则为 false。
     */
    boolean isCraftable(Material material);

    /**
     * Saves the current crafting conditions to the configuration file.
     * 将当前的合成状态保存到配置文件中。
     * This method should be called when the server is shutting down or when
     * the crafting conditions are modified.
     * 此方法应在服务器关闭或合成状态被修改时调用。
     */
    void saveBlockedItems(Material material);
}