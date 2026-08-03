package dev.revere.alley.feature.item;

import dev.revere.alley.bootstrap.lifecycle.Service;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @author Emmy
 * 作者 Emmy
 * @project alley-practice
 * 项目 alley-practice
 * @since 18/07/2025
 * 自 18/07/2025
 */
public interface ItemService extends Service {
    /**
     * Retrieves the golden head item stack.
     * 获取金头物品堆叠。
     *
     * @return The ItemStack representing the golden head.
     * 表示金头的 ItemStack。
     */
    ItemStack getGoldenHead();

    /**
     * Performs the consume action for a golden head item when used by a player.
     * 当玩家使用金头物品时执行消耗操作。
     *
     * @param player The player who is consuming the item.
     * 正在消耗该物品的玩家。
     * @param item The ItemStack representing the golden head being consumed.
     * 表示正在消耗的金头的 ItemStack。
     */
    void performHeadConsume(Player player, ItemStack item);
}