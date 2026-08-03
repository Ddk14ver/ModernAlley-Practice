package dev.revere.alley.feature.layout.menu.button;

import dev.revere.alley.common.item.ItemBuilder;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.layout.menu.LayoutSelectionMenu;
import dev.revere.alley.library.menu.Button;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 13/07/2026
 */

@AllArgsConstructor
public class LayoutButton extends Button {
    private final Kit kit;

    @Override
    public ItemStack getButtonItem(Player player) {
        return new ItemBuilder(this.kit.getIconItemOrDefault())
                .name("&6&l" + this.kit.getDisplayName())
                .durability(this.kit.getDurability())
                .lore(CC.MENU_BAR, "&aClick to manage layouts.", CC.MENU_BAR)
                .hideMeta().build();
    }

    @Override
    public void clicked(Player player, ClickType clickType) {
        if (clickType != ClickType.LEFT) return;
        Profile profile = dev.revere.alley.AlleyPlugin.getInstance()
                .getService(ProfileService.class).getProfile(player.getUniqueId());
        new LayoutSelectionMenu(this.kit, profile).openMenu(player);
    }
}
