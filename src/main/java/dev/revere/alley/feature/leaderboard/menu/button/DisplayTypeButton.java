package dev.revere.alley.feature.leaderboard.menu.button;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.library.menu.Button;
import dev.revere.alley.feature.leaderboard.LeaderboardType;
import dev.revere.alley.feature.leaderboard.menu.LeaderboardMenu;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.common.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 显示类型切换按钮，允许玩家在排行榜菜单中切换不同的排行榜类型（排位、非排位、连胜等）。
 * Display type toggle button, allowing players to switch between different leaderboard types (ranked, unranked, win streak, etc.) in the leaderboard menu.
 *
 * @author Remi
 * @project Alley
 * @date 5/26/2024
 */
public class DisplayTypeButton extends Button {
    protected final AlleyPlugin plugin = AlleyPlugin.getInstance();

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
        ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);
        Profile profile = profileService.getProfile(player.getUniqueId());
        LeaderboardType currentType = profile.getLeaderboardType();

        List<String> lore = new ArrayList<>();
        for (LeaderboardType type : LeaderboardType.values()) {
            lore.add((currentType == type ? "&6│ &6" : "&6│ &7") + type.getName());
        }
        lore.add("");
        lore.add("&aClick to change the display type.");

        return new ItemBuilder(Material.ENDER_EYE)
                .name("&6&lDisplay Type")
                .lore(lore)
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
        ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);
        Profile profile = profileService.getProfile(player.getUniqueId());

        LeaderboardType currentType = profile.getLeaderboardType();
        LeaderboardType[] types = LeaderboardType.values();
        int currentIndex = currentType.ordinal();

        switch (clickType) {
            case LEFT:
                currentIndex = (currentIndex + 1) % types.length;
                break;
            case RIGHT:
                currentIndex = (currentIndex - 1 + types.length) % types.length;
                break;
        }

        LeaderboardType newType = types[currentIndex];
        profile.setLeaderboardType(newType);
        new LeaderboardMenu().openMenu(player);
        this.playNeutral(player);
    }
}