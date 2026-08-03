package dev.revere.alley.feature.queue.menu.button;

import dev.revere.alley.common.item.ItemBuilder;
import dev.revere.alley.feature.bot.menu.BotDifficultyMenu;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.library.menu.Button;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
public class BotButton extends Button {
    private final Kit kit;

    @Override
    public ItemStack getButtonItem(Player player) {
        return new ItemBuilder(kit.getIconItemOrDefault())
                .name("&6&l" + kit.getDisplayName())
                .lore(
                        "&7Fight a configurable combat bot.",
                        "",
                        "&7Kit: &f" + kit.getName(),
                        "&7AI Mode: &f" + kit.getBotAiMode().name(),
                        "",
                        "&aClick to select difficulty."
                )
                .hideMeta()
                .build();
    }

    @Override
    public void clicked(Player player, ClickType clickType) {
        if (clickType != ClickType.LEFT) return;
        new BotDifficultyMenu(this.kit).openMenu(player);
        this.playNeutral(player);
    }
}
