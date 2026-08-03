package dev.revere.alley.feature.leaderboard.menu.button;

import dev.revere.alley.library.menu.Button;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.leaderboard.data.LeaderboardPlayerData;
import dev.revere.alley.feature.leaderboard.LeaderboardType;
import dev.revere.alley.common.item.ItemBuilder;
import dev.revere.alley.common.text.CC;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 排行榜职业装备按钮，在排行榜菜单中展示每个职业装备的排行榜数据。
 * Leaderboard kit button, displaying the leaderboard data for each kit in the leaderboard menu.
 *
 * @author Emmy
 * @project Alley
 * @date 3/3/2025
 */
public class LeaderboardKitButton extends Button {
    private final Kit kit;
    private final List<LeaderboardPlayerData> leaderboard;
    private final LeaderboardType type;

    public LeaderboardKitButton(Kit kit, List<LeaderboardPlayerData> leaderboard, LeaderboardType type) {
        this.kit = kit;
        this.leaderboard = leaderboard;
        this.type = type;
    }

    @Override
    public ItemStack getButtonItem(Player player) {
        ItemBuilder builder = new ItemBuilder(this.kit.getIconItemOrDefault())
                .name(this.kit.getMenuTitle())
                .durability(this.kit.getDurability())
                .hideMeta();

        switch (this.type) {
            case RANKED:
                return builder.lore(generateLore("Elo")).build();
            case UNRANKED:
                return builder.lore(generateLore("Wins")).build();
            case FFA:
                return builder.lore(generateLore("Kills")).build();
            case WIN_STREAK:
                return builder.lore(generateLore("Wins")).build();
            case UNRANKED_MONTHLY:
            case TOURNAMENT:
            default:
                return this.inDevelopment();
        }
    }

    /**
     * A reusable helper method to generate the lore for any numerical leaderboard.
     * 一个可复用的辅助方法，用于为任何数值型排行榜生成物品描述（lore）。
     *
     * @param statName The name of the statistic being displayed (e.g., "Elo", "Wins").
     *                 正在显示的统计名称（例如 "Elo"、"Wins"）
     * @return A list of formatted strings for the item's lore.
     *         物品描述的格式化字符串列表
     */
    private List<String> generateLore(String statName) {
        if (this.leaderboard.isEmpty()) {
            return Arrays.asList("", "&7No entries yet for this kit.");
        }

        List<String> lore = new ArrayList<>();
        lore.add(CC.MENU_BAR);

        List<String> topEntries = this.leaderboard.stream()
                .limit(10)
                .map(data -> {
                    int currentRank = this.leaderboard.indexOf(data) + 1;

                    String rankPrefix;
                    switch (currentRank) {
                        case 1:
                            rankPrefix = "&6&l✫" + currentRank;
                            break;
                        case 2:
                            rankPrefix = "&7&l✫" + currentRank;
                            break;
                        case 3:
                            rankPrefix = "&c&l✫" + currentRank;
                            break;
                        default:
                            rankPrefix = "&6" + currentRank + ".";
                            break;
                    }

                    return rankPrefix + " &f" + data.getName() + " &7- &f" + data.getValue() + " " + statName;
                })
                .collect(Collectors.toList());

        lore.addAll(topEntries);
        lore.add(CC.MENU_BAR);
        return lore;
    }

    /**
     * Returns a placeholder item for leaderboards that are not yet implemented.
     * 为尚未实现的排行榜返回一个占位物品。
     */
    private ItemStack inDevelopment() {
        return new ItemBuilder(Material.BARRIER)
                .name("&c&lComing Soon")
                .lore(
                        CC.MENU_BAR,
                        "&7This leaderboard is currently",
                        "&7being worked on. Please check",
                        "&7back later.",
                        CC.MENU_BAR
                )
                .build();
    }
}
