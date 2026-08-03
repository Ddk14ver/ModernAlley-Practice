package dev.revere.alley.feature.leaderboard.menu;

import com.google.common.collect.Maps;
import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.library.menu.Button;
import dev.revere.alley.library.menu.Menu;
import dev.revere.alley.feature.kit.KitService;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.kit.setting.types.mode.KitSettingRanked;
import dev.revere.alley.feature.leaderboard.LeaderboardService;
import dev.revere.alley.feature.leaderboard.data.LeaderboardPlayerData;
import dev.revere.alley.feature.leaderboard.LeaderboardType;
import dev.revere.alley.feature.leaderboard.menu.button.DisplayTypeButton;
import dev.revere.alley.feature.leaderboard.menu.button.LeaderboardKitButton;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.menu.statistic.button.StatisticsButton;
import org.bukkit.entity.Player;
import org.bukkit.Material;

import java.util.List;
import java.util.Map;

/**
 * 排行榜菜单类，构建并显示排行榜 GUI 界面，包含类型切换和职业装备排行按钮。
 * Leaderboard menu class, building and displaying the leaderboard GUI with type switching and kit rank buttons.
 *
 * @author Emmy
 * @project Alley
 * @date 25/05/2024 - 14:51
 */
public class LeaderboardMenu extends Menu {
    protected final AlleyPlugin plugin = AlleyPlugin.getInstance();

    @Override
    public String getTitle(Player player) {
        ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);
        Profile profile = profileService.getProfile(player.getUniqueId());

        switch (profile.getLeaderboardType()) {
            case RANKED:
                return "&6&lRanked Leaderboards";
            case UNRANKED:
                return "&6&lUnranked Leaderboards";
            case UNRANKED_MONTHLY:
                return "&6&lMonthly Leaderboards";
            case FFA:
                return "&6&lFFA Leaderboards";
            case TOURNAMENT:
                return "&6&lTournament Leaderboards";
            case WIN_STREAK:
                return "&6&lWin Streak Leaderboards";
            default:
                return "&6&lLeaderboards";
        }
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        final Map<Integer, Button> buttons = Maps.newHashMap();
        ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);
        Profile profile = profileService.getProfile(player.getUniqueId());
        LeaderboardType currentType = profile.getLeaderboardType();
        LeaderboardService leaderboardService = AlleyPlugin.getInstance().getService(LeaderboardService.class);

        buttons.put(2, new StatisticsButton());
        buttons.put(6, new DisplayTypeButton());

        int slot = 10;  // declare slot here once
        // 在此处声明 slot 变量，避免重复初始化

        for (Kit kit : AlleyPlugin.getInstance().getService(KitService.class).getKits()) {
            if (!kit.isEnabled() || kit.getIcon() == null) continue;

            List<LeaderboardPlayerData> leaderboard = leaderboardService.getLeaderboardEntries(kit, currentType);

            switch (currentType) {
                case RANKED:
                    if (!kit.isSettingEnabled(KitSettingRanked.class)) {
                        break;
                    }
                case UNRANKED:
                case UNRANKED_MONTHLY:
                case TOURNAMENT:
                case WIN_STREAK:
                case FFA:
                    slot = this.skipIfSlotCrossingBorder(slot);
                    buttons.put(slot++, new LeaderboardKitButton(kit, leaderboard, currentType));
                    break;
            }
        }

        this.addBorder(buttons, Material.BLACK_STAINED_GLASS_PANE, 5);

        return buttons;
    }

    @Override
    public int getSize() {
        return 5 * 9;
    }
}