package dev.revere.alley.common;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.EnumSet;
import java.util.Set;

/**
 * @author Emmy
 * @project Alley
 * @date 02/01/2025 - 19:13
 */
@UtilityClass
public class InventoryUtil {
    private final Set<Material> LEATHER_ARMOR = EnumSet.of(
            Material.LEATHER_HELMET,
            Material.LEATHER_CHESTPLATE,
            Material.LEATHER_LEGGINGS,
            Material.LEATHER_BOOTS
    );

    /**
     * Checks if a material is a dyeable block type (wool or terracotta).
     * 检查材质是否为可染色方块类型（羊毛或陶瓦）。
     */
    private boolean isDyeableBlock(Material material) {
        String name = material.name();
        return name.endsWith("_WOOL") || name.endsWith("_TERRACOTTA");
    }

    /**
     * Applies a specified TeamColor to a player's inventory,
     * coloring any dyeable blocks and leather armor.
     * 将指定的队伍颜色应用到玩家的背包中，
     * 对所有可染色方块和皮革装备进行着色。
     *
     * @param player    The player whose inventory will be colored.
     *                  要对其背包进行着色的玩家。
     * @param teamColor The TeamColor data to apply.
     *                  要应用的队伍颜色数据。
     */
    public void applyTeamColorToInventory(Player player, TeamColor teamColor) {
        if (player == null || !player.isOnline() || teamColor == null) {
            return;
        }

        PlayerInventory inventory = player.getInventory();

        colorItems(inventory.getContents(), teamColor);
        colorItems(inventory.getArmorContents(), teamColor);

        player.updateInventory();
    }

    /**
     * A private helper to iterate over an array of items and apply coloring.
     * 私有辅助方法，遍历物品数组并应用着色。
     */
    private void colorItems(ItemStack[] items, TeamColor teamColor) {
        for (ItemStack item : items) {
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }

            if (isDyeableBlock(item.getType())) {
                item.setType(teamColor.mapToColoredMaterial(item.getType()));
            }

            if (LEATHER_ARMOR.contains(item.getType())) {
                ItemMeta meta = item.getItemMeta();
                if (meta instanceof LeatherArmorMeta) {
                    LeatherArmorMeta leatherMeta = (LeatherArmorMeta) meta;
                    leatherMeta.setColor(teamColor.getArmorColor());
                    item.setItemMeta(leatherMeta);
                }
            }
        }
    }

    /**
     * Clone an array of ItemStacks to ensure deep copy.
     * 克隆ItemStack数组以确保深拷贝。
     *
     * @param items the original array
     *              原始数组
     * @return a cloned array
     *         克隆后的数组
     */
    public ItemStack[] cloneItemStackArray(ItemStack[] items) {
        if (items == null) return null;

        ItemStack[] cloned = new ItemStack[items.length];
        for (int i = 0; i < items.length; i++) {
            cloned[i] = items[i] != null ? items[i].clone() : null;
        }
        return cloned;
    }

    /**
     * Give a specific item to a player.
     * 给玩家一个特定的物品。
     *
     * @param player   the player to give the item to
     *                 要给予物品的玩家
     * @param material the material of the item to give
     *                 要给予的物品材质
     */
    public void giveItem(Player player, Material material, int amount) {
        player.getInventory().addItem(new ItemStack(material, amount));
    }

    /**
     * Represents a set of color data used for team-based item coloring.
     * 表示用于基于队伍的物品着色的一组颜色数据。
     */
    @Getter
    public enum TeamColor {
        BLUE(Color.fromRGB(0, 102, 255), (short) 11, "BLUE"),
        RED(Color.fromRGB(255, 0, 0), (short) 14, "RED");

        private final Color armorColor;
        private final short blockDataValue;
        private final String colorPrefix;

        TeamColor(Color armorColor, short blockDataValue, String colorPrefix) {
            this.armorColor = armorColor;
            this.blockDataValue = blockDataValue;
            this.colorPrefix = colorPrefix;
        }

        /**
         * Maps a legacy dyeable block material to this team's colored variant.
         * 将旧版可染色方块材质映射到该队伍的彩色变体。
         */
        public Material mapToColoredMaterial(Material original) {
            String name = original.name();
            try {
                if (name.endsWith("_WOOL")) {
                    return Material.valueOf(colorPrefix + "_WOOL");
                } else if (name.endsWith("_TERRACOTTA")) {
                    return Material.valueOf(colorPrefix + "_TERRACOTTA");
                }
            } catch (IllegalArgumentException ignored) {
            }
            return original;
        }
    }
}
