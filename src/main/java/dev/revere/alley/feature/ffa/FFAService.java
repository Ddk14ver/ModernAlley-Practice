package dev.revere.alley.feature.ffa;

import dev.revere.alley.feature.arena.Arena;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.bootstrap.lifecycle.Service;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

/**
 * @author Remi
 * @project alley-practice
 * @date 2/07/2025
 */
public interface FFAService extends Service {
    /**
     * Gets a list of all active, persistent FFA matches.
     * 获取所有活跃的、持久的FFA比赛列表。
     * @return An unmodifiable list of FFA matches.
     *         一个不可修改的FFA比赛列表。
     */
    List<FFAMatch> getMatches();

    /**
     * Gets a list of all kits that are enabled for FFA mode.
     * 获取所有启用了FFA模式的工具包列表。
     * @return An unmodifiable list of FFA-enabled kits.
     *         一个不可修改的启用FFA的工具包列表。
     */
    List<Kit> getFfaKits();

    /**
     * Creates a new FFA match with the given parameters.
     * 使用给定的参数创建一个新的FFA比赛。
     *
     * @param arena      The arena the match is being played in
     *                   比赛所在的竞技场
     * @param kit        The kit the players are using
     *                   玩家使用的工具包
     * @param maxPlayers The maximum amount of players allowed in the match
     *                   比赛允许的最大玩家数量
     */
    void createFFAMatch(Arena arena, Kit kit, int maxPlayers);

    /**
     * Removes the persistent FFA match associated with a kit.
     * Online players in that match are returned to the lobby first.
     *
     * @param kit the kit whose FFA match should be removed
     */
    void removeFFAMatch(Kit kit);

    /**
     * Finds the FFA match that a specific player is currently in.
     * 查找指定玩家当前所在的FFA比赛。
     * @param player The player to search for.
     *               要搜索的玩家。
     * @return An Optional containing the AbstractFFAMatch if the player is in one.
     *         如果玩家在某个比赛中，则返回包含该FFA比赛的Optional。
     */
    Optional<FFAMatch> getMatchByPlayer(Player player);

    /**
     * Gets a persistent FFA match by its associated kit name.
     * 通过关联的工具包名称获取持久的FFA比赛。
     * @param kitName The name of the kit.
     *                工具包的名称。
     * @return The AbstractFFAMatch for that kit, or null if none exists.
     *         该工具包对应的FFA比赛，如果不存在则返回null。
     */
    FFAMatch getFFAMatch(String kitName);

    /**
     * An overloaded method to find the FFA match a player is in.
     * 一个重载方法，用于查找玩家所在的FFA比赛。
     * @param player The player to search for.
     *               要搜索的玩家。
     * @return The AbstractFFAMatch, or null if the player is not in one.
     *         FFA比赛对象，如果玩家不在任何比赛中则返回null。
     */
    FFAMatch getFFAMatch(Player player);

    /**
     * Forcefully reloads all FFA matches. This will kick all current players
     * 强制重新加载所有FFA比赛。这将踢出所有当前玩家，
     * and re-initialize the matches based on the current kit configurations.
     * 并根据当前的工具包配置重新初始化比赛。
     */
    void reloadFFAKits();

    /**
     * Checks if a specific kit is eligible for FFA mode.
     * 检查特定的工具包是否符合FFA模式的条件。
     * @param kit The kit to check.
     *            要检查的工具包。
     * @return true if the kit is eligible for FFA, false otherwise.
     *         如果工具包符合FFA条件则返回true，否则返回false。
     */
    boolean isNotEligibleForFFA(Kit kit);
}
