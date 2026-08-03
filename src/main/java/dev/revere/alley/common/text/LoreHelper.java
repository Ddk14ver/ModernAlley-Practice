package dev.revere.alley.common.text;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.constants.PluginConstant;
import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Utility class for generating lore display strings.
 * 用于生成物品描述显示字符串的工具类。
 *
 * @author Emmy
 * @project Alley
 * @since 21/04/2025
 */
@UtilityClass
public class LoreHelper {
    /**
     * Returns a string representation of a boolean value indicating toggled status.
     * 返回表示切换状态的布尔值的字符串表示。
     *
     * @param value The boolean value to represent.
     *              要表示的布尔值。
     * @return A formatted string indicating whether the feature is toggled or not.
     *         一个格式化字符串，指示功能是否已切换。
     */
    public String displayToggled(boolean value) {
        String returnValue = value ? "&a&l✔ &6Toggled" : "&c&l✘ &cNot Toggled";
        return ChatColor.translateAlternateColorCodes('&', "&f&l│ " + returnValue);
    }
    /**
     * Returns a string representation of a boolean value.
     * 返回布尔值的字符串表示。
     *
     * @param value The boolean value to represent.
     *              要表示的布尔值。
     */
    public String displayEnabled(boolean value) {
        String returnValue = value ? "&6Enabled" : "&cDisabled";
        return ChatColor.translateAlternateColorCodes('&', "&f&l│ " + returnValue);
    }

    /**
     * Returns a string representation of a boolean value indicating visibility.
     * 返回表示可见性的布尔值的字符串表示。
     *
     * @param value The boolean value to represent.
     *              要表示的布尔值。
     */
    public String displayShown(boolean value) {
        String returnValue = value ? "&6Shown" : "&cHidden";
        return ChatColor.translateAlternateColorCodes('&', "&f&l│ " + returnValue);
    }

    /**
     * Returns a string representation of a boolean value indicating status.
     * 返回表示状态的布尔值的字符串表示。
     *
     * @param value The boolean value to represent.
     *              要表示的布尔值。
     */
    public String displayStatus(boolean value) {
        String returnValue = value ? "&aEnabled" : "&cDisabled";
        return ChatColor.translateAlternateColorCodes('&', "&6│ &6Status: &f" + returnValue);
    }

    /**
     * Returns a string representation of a boolean value indicating a tick or cross.
     * 返回表示对勾或叉号的布尔值的字符串表示。
     *
     * @param value The boolean value to represent.
     *              要表示的布尔值。
     */
    public String displaySymbol(boolean value) {
        String returnValue = value ? "&a&l✔" : "&c&l✘";
        return ChatColor.translateAlternateColorCodes('&', "&f&l│ " + returnValue);
    }

    /**
     * Represents equipment selection lore for a player based on a permission.
     * 基于权限为玩家表示装备选择描述。
     *
     * @param player        The player to check.
     *                      要检查的玩家。
     * @param permission    The permission required to select.
     *                      选择所需的权限。
     * @param inUse         Whether the item is in use or not.
     *                      物品是否正在使用。
     * @param clickToAction The action to perform when clicked.
     *                      点击时要执行的操作。
     */
    public String selectionLoreWithPermission(Player player, String permission, boolean inUse, String clickToAction) {
        if (player.hasPermission(permission) && inUse) {
            return "&a&lSELECTED";
        } else if (player.hasPermission(permission) && !inUse) {
            return "&aClick to " + clickToAction + "!";
        } else {
            return AlleyPlugin.getInstance().getService(PluginConstant.class).getPermissionLackMessage();
        }
    }

    /**
     * Represents equipment selection lore for a player.
     * 为玩家表示装备选择描述。
     *
     * @param inUse         Whether the item is in use or not.
     *                      物品是否正在使用。
     * @param clickToAction The action to perform when clicked.
     *                      点击时要执行的操作。
     */
    public String selectionLore(boolean inUse, String clickToAction) {
        if (inUse) {
            return "&aSelected.";
        } else {
            return "&aClick to " + clickToAction + ".";
        }
    }
}