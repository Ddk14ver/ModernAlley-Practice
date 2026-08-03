package dev.revere.alley.library.assemble;

import dev.revere.alley.bootstrap.lifecycle.Service;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

/**
 * @author Remi
 * @project alley-practice
 * @date 2/07/2025
 */
public interface AssembleService extends Service {
    /**
     * Gets the map of all active scoreboard instances.
     * 获取所有活跃的记分板实例的映射。
     *
     * @return A map where the key is the player's UUID and the value is their AssembleBoard.
     *         一个映射，键为玩家的 UUID，值为其 AssembleBoard。
     */
    Map<UUID, AssembleBoard> getBoards();

    /**
     * Gets the adapter that is currently providing content (title and lines) to the scoreboard.
     * 获取当前向记分板提供内容（标题和行）的适配器。
     *
     * @return The active IAssembleAdapter instance.
     *         当前活跃的 IAssembleAdapter 实例。
     */
    AssembleAdapter getAdapter();

    boolean isCallEvents();

    /**
     * Creates and registers a new scoreboard for a player.
     * 为玩家创建并注册新的记分板。
     * This should be called when a player joins.
     * 此方法应在玩家加入时调用。
     *
     * @param player The player to create the board for.
     *        要为其创建记分板的玩家。
     */
    void createBoard(Player player);

    /**
     * Removes the scoreboard for a player.
     * 移除玩家的记分板。
     * This should be called when a player quits.
     * 此方法应在玩家退出时调用。
     *
     * @param player The player whose board to remove.
     *        要移除其记分板的玩家。
     */
    void removeBoard(Player player);
}
