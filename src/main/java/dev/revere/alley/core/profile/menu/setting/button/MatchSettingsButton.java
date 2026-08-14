package dev.revere.alley.core.profile.menu.setting.button;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.item.ItemBuilder;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.core.profile.data.types.ProfileSettingData;
import dev.revere.alley.core.profile.menu.setting.enums.MatchSettingType;
import dev.revere.alley.feature.cosmetic.menu.CosmeticTypeMenu;
import dev.revere.alley.feature.cosmetic.model.CosmeticType;
import dev.revere.alley.feature.visibility.VisibilityService;
import dev.revere.alley.library.menu.Button;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
public class MatchSettingsButton extends Button {
    private static final int[] PING_RANGES = {0, 30, 50, 100, 200};

    private final MatchSettingType settingType;

    @Override
    public ItemStack getButtonItem(Player player) {
        ProfileSettingData settings = getSettings(player);
        return new ItemBuilder(this.settingType.material)
                .name(this.settingType.displayName)
                .lore(this.settingType.loreProvider.apply(settings))
                .hideMeta()
                .build();
    }

    @Override
    public void clicked(Player player, ClickType clickType) {
        if (clickType != ClickType.LEFT) return;

        Profile profile = AlleyPlugin.getInstance().getService(ProfileService.class)
                .getProfile(player.getUniqueId());
        ProfileSettingData settings = profile.getProfileData().getSettingData();

        switch (this.settingType) {
            case SHOW_CPS -> settings.setShowMatchCps(!settings.isShowMatchCps());
            case SHOW_PING -> settings.setShowMatchPing(!settings.isShowMatchPing());
            case SHOW_OPPONENT -> settings.setShowMatchOpponent(!settings.isShowMatchOpponent());
            case MVP_MUSIC -> settings.setMatchMvpMusicEnabled(!settings.isMatchMvpMusicEnabled());
            case FLY_ON_LOSS -> settings.setFlyOnLoss(!settings.isFlyOnLoss());
            case FLY_ON_WIN -> settings.setFlyOnWin(!settings.isFlyOnWin());
            case QUEUE_PING_RANGE -> settings.setQueuePingRange(nextPingRange(settings.getQueuePingRange()));
            case KILL_EFFECTS -> {
                new CosmeticTypeMenu(CosmeticType.KILL_EFFECT).openMenu(player);
                this.playNeutral(player);
                return;
            }
            case SWING_SLOWLY -> settings.setSwingSlowlyEnabled(!settings.isSwingSlowlyEnabled());
            case ALLOW_SPECTATORS -> settings.setAllowSpectators(!settings.isAllowSpectators());
            case DISABLE_PUBLIC_CHAT -> settings.setDisablePublicChatWhenInMatch(!settings.isDisablePublicChatWhenInMatch());
            case HIDE_OTHER_SPECTATORS -> {
                settings.setHideOtherSpectators(!settings.isHideOtherSpectators());
                AlleyPlugin.getInstance().getService(VisibilityService.class).updateVisibility(player);
            }
        }

        profile.save();
        this.playNeutral(player);
    }

    private ProfileSettingData getSettings(Player player) {
        return AlleyPlugin.getInstance().getService(ProfileService.class)
                .getProfile(player.getUniqueId()).getProfileData().getSettingData();
    }

    private int nextPingRange(int current) {
        for (int index = 0; index < PING_RANGES.length; index++) {
            if (PING_RANGES[index] == current) {
                return PING_RANGES[(index + 1) % PING_RANGES.length];
            }
        }
        return PING_RANGES[0];
    }
}
