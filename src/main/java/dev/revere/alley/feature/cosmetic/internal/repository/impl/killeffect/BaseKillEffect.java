package dev.revere.alley.feature.cosmetic.internal.repository.impl.killeffect;

import dev.revere.alley.feature.cosmetic.model.BaseCosmetic;
import org.bukkit.entity.Player;

/**
 * @author Remi
 *   作者: Remi
 * @project alley-practice
 *   项目: alley-practice
 * @date 6/08/2025
 *   日期: 6/08/2025
 */
public abstract class BaseKillEffect extends BaseCosmetic {
    /**
     * Executes the kill effect for the specified player.
     *   为指定玩家执行击杀效果。
     * This method is called when the player gets a kill.
     *   当玩家完成击杀时调用此方法。
     *
     * @param player The player who executed the kill.
     *       执行击杀的玩家。
     */
    public abstract void execute(Player player);
}