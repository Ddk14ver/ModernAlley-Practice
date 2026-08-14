package dev.revere.alley.feature.bot.menu;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.item.ItemBuilder;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.feature.bot.CustomBotProfile;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.library.menu.Button;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public final class CustomBotButton extends Button {
    private final Kit kit;

    @Override
    public ItemStack getButtonItem(Player player) {
        Profile profile = AlleyPlugin.getInstance().getService(ProfileService.class)
                .getProfile(player.getUniqueId());
        CustomBotProfile custom = profile.getProfileData().getCustomBotProfile();
        if (custom == null) custom = new CustomBotProfile();
        return new ItemBuilder(Material.PLAYER_HEAD)
                .name("&d&lCustom Bot")
                .lore(CC.MENU_BAR,
                        "&7Name: &f" + custom.getName(),
                        "&7Skin: &f" + (custom.getSkinName().isBlank() ? "Default" : custom.getSkinName()),
                        "&7CPS / Reach: &f" + custom.getCps() + " / " + custom.getMaxReach(),
                        "",
                        "&7Configure the bot's identity,",
                        "&7skin, combat values and actions.",
                        "",
                        "&aClick to configure.",
                        CC.MENU_BAR)
                .hideMeta()
                .build();
    }

    @Override
    public void clicked(Player player, ClickType clickType) {
        if (!clickType.isLeftClick()) return;
        new CustomBotMenu(this.kit).openMenu(player);
        playNeutral(player);
    }
}
