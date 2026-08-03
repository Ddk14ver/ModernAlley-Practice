package dev.revere.alley.common.reflect.internal.types;

import dev.revere.alley.common.reflect.Reflection;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @author Emmy
 * @project alley-practice
 * @since 14/07/2025
 */
public class VirtualStackReflectionServiceImpl implements Reflection {
    /**
     * Sets the amount of the item in the player's hand,
     * 设置玩家手中物品的数量，
     * even beyond normal max stack size using ItemMeta manipulation.
     * 甚至可以通过 ItemMeta 操作超过正常的最大堆叠大小。
     *
     * @param player the player whose held item to modify
     *               要修改其手中物品的玩家
     * @param amount the amount to set (can exceed normal stack max)
     *               要设置的数量（可以超过正常的堆叠上限）
     * @throws Exception if reflect fails
     *                   如果反射失败
     */
    public void setVirtualStackAmount(Player player, int amount) throws Exception {
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || item.getType() == Material.AIR) {
            throw new IllegalArgumentException("Player must be holding an item.");
        }

        item.setAmount(amount);
        player.setItemInHand(item);
    }
}
