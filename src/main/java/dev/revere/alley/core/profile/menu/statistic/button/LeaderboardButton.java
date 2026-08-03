package dev.revere.alley.core.profile.menu.statistic.button;

import dev.revere.alley.library.menu.Button;
import dev.revere.alley.feature.leaderboard.menu.LeaderboardMenu;
import dev.revere.alley.common.item.ItemBuilder;
import dev.revere.alley.common.text.CC;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

/**
 * @author Remi
 * @project Alley
 * @date 5/26/2024
 */
public class LeaderboardButton extends Button {

    /**
     * Gets the item to display in the menu.
     * 获取要在菜单中显示的物品。
     *
     * @param player the player viewing the menu
     *               正在查看菜单的玩家
     * @return the item to display
     *         要显示的物品
     */
    @Override
    public ItemStack getButtonItem(Player player) {
        return new ItemBuilder(Material.ENDER_EYE)
                .name("&6&lLeaderboards")
                .lore(
                        CC.MENU_BAR,
                        "&7All of the leaderboards are displayed here.",
                        "&7You can view top wins, losses, and more.",
                        "",
                        "&aClick to view the leaderboards.",
                        CC.MENU_BAR
                )
                .build();
    }

    /**
     * Handles the click event for the button.
     * 处理按钮的点击事件。
     *
     * @param player    the player who clicked the button
     *                  点击按钮的玩家
     * @param clickType the type of click
     *                  点击类型
     */
    @Override
    public void clicked(Player player, ClickType clickType) {
        if (clickType == ClickType.MIDDLE || clickType == ClickType.RIGHT || clickType == ClickType.NUMBER_KEY || clickType == ClickType.DROP || clickType == ClickType.SHIFT_LEFT || clickType == ClickType.SHIFT_RIGHT) {
            return;
        }
        this.playNeutral(player);
        new LeaderboardMenu().openMenu(player);
    }
}
