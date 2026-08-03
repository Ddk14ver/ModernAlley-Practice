package dev.revere.alley.visual.nametag;

import dev.revere.alley.bootstrap.lifecycle.Service;
import org.bukkit.entity.Player;

/**
 * 名字标签服务接口，定义更新玩家状态的核心方法。
 * @author Remi
 * @project alley-practice
 * @date 2/07/2025
 */
public interface NametagService extends Service {
    /**
     * This is the main method to call when a player's state changes (e.g., joining/leaving a match).
     * It triggers a full, two-way re-evaluation of nametags between the specified player
     * and all other online players.
     * 当玩家状态发生变化时（如加入/离开比赛）调用的主要方法。
     * 触发指定玩家与所有其他在线玩家之间名字标签的完整双向重新评估。
     *
     * @param player The player whose state has changed.
     *        状态发生变化的玩家。
     */
    void updatePlayerState(Player player);
}