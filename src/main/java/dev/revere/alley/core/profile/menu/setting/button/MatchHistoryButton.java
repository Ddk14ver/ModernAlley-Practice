package dev.revere.alley.core.profile.menu.setting.button;

import dev.revere.alley.common.item.ItemBuilder;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.profile.menu.match.MatchHistorySelectKitMenu;
import dev.revere.alley.library.menu.Button;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class MatchHistoryButton extends Button {
    @Override
    public ItemStack getButtonItem(Player player) {
        return new ItemBuilder(Material.BOOK)
                .name("&6&lMatch History")
                .lore(
                        CC.MENU_BAR,
                        "&7View your previous matches.",
                        "",
                        "&aClick to view.",
                        CC.MENU_BAR
                )
                .build();
    }

    @Override
    public void clicked(Player player, ClickType clickType) {
        if (clickType != ClickType.LEFT) return;
        this.playNeutral(player);
        new MatchHistorySelectKitMenu().openMenu(player);
    }
}
