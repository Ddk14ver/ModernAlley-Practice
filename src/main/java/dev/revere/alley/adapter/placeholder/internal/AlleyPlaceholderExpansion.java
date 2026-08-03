package dev.revere.alley.adapter.placeholder.internal;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.constants.PluginConstant;
import dev.revere.alley.feature.level.LevelService;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.data.ProfileData;
import dev.revere.alley.common.text.CC;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * @author Emmy
 * @project Alley
 * @since 21/05/2025
 */
public class AlleyPlaceholderExpansion extends PlaceholderExpansion {

    /*
     * Examples:
     * 示例：
     *
     * %alley_division_<kit_name>% | returns the player's division in the specified kit
     * %alley_division_<kit_name>% | 返回玩家在指定套件中的段位
     * %alley_global-elo% | returns the player's global Elo
     * %alley_global-elo% | 返回玩家的全局 Elo
     */

    protected final AlleyPlugin plugin;
    protected final String notAvailableString;

    /**
     * Constructor for the AlleyPlaceholderExpansion class.
     * AlleyPlaceholderExpansion 类的构造函数。
     *
     * @param plugin The Alley bootstrap instance.
     *               Alley 引导程序实例。
     */
    public AlleyPlaceholderExpansion(AlleyPlugin plugin) {
        this.plugin = plugin;
        this.notAvailableString = "&cN/A";
    }

    @Override
    public @NotNull String getIdentifier() {
        return this.plugin.getService(PluginConstant.class).getName();
    }

    @Override
    public @NotNull String getAuthor() {
        return this.plugin.getDescription().getAuthors().get(0);
    }

    @Override
    public @NotNull String getVersion() {
        return this.plugin.getService(PluginConstant.class).getVersion();
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return null;
        }

        ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);
        Profile profile = profileService.getProfile(player.getUniqueId());
        ProfileData profileData = profile.getProfileData();

        if (profileData == null) {
            return this.notAvailableString;
        }

        if (params.startsWith("leaderboard-position-")) {
            //String kitName = params.substring("leaderboard-position-".length());
            return this.notAvailableString; // Get Leaderboard entry after implementing the leaderboard system properly
            // 在正确实现排行榜系统后获取排行榜条目
        } else if (params.startsWith("division_")) {
            String kitName = params.substring("division_".length());
            String division = profileData.getUnrankedKitData().get(kitName).getDivision().getName();
            return division == null ? this.notAvailableString : division;
        }

        switch (params.toLowerCase()) {
            case "player-global-elo":
                return String.valueOf(profileData.getElo());
            case "player-unranked-wins":
                return String.valueOf(profileData.getTotalWins());
            case "player-unranked-losses":
                return String.valueOf(profileData.getTotalLosses());
            case "player-ranked-wins":
                return String.valueOf(profileData.getRankedWins());
            case "player-ranked-losses":
                return String.valueOf(profileData.getRankedLosses());
            case "player-level":
                return Objects.requireNonNull(CC.translate(this.plugin.getService(LevelService.class).getLevel(profileData.getElo()).getDisplayName()), this.notAvailableString);
            case "player-coins":
                return String.valueOf(profileData.getCoins());
            case "player-cps":
                return String.valueOf(dev.revere.alley.feature.cps.CPSListener.getCpsManager().getCPS(player.getUniqueId()));
            case "player-max-cps":
                return String.valueOf(dev.revere.alley.feature.cps.CPSListener.getCpsManager().getMaxCPS(player.getUniqueId()));
        }

        return null;
    }
}