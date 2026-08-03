package dev.revere.alley.feature.bot.menu;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.item.ItemBuilder;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.feature.bot.BotAiMode;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.kit.KitService;
import dev.revere.alley.feature.kit.menu.KitSettingsMenu;
import dev.revere.alley.library.menu.Button;
import dev.revere.alley.library.menu.Menu;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class BotAiModeMenu extends Menu {
    private final Kit kit;

    @Override
    public String getTitle(Player player) {
        return "&6&lBot AI: " + this.kit.getDisplayName();
    }

    @Override
    public int getSize() {
        return 27;
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        int[] slots = {10, 12, 14, 16};
        BotAiMode[] modes = BotAiMode.values();
        for (int index = 0; index < modes.length && index < slots.length; index++) {
            buttons.put(slots[index], new ModeButton(this.kit, modes[index]));
        }
        buttons.put(22, new BackButton(this.kit));
        this.addBorder(buttons, Material.BLACK_STAINED_GLASS_PANE, 3);
        return buttons;
    }

    @RequiredArgsConstructor
    private static class ModeButton extends Button {
        private final Kit kit;
        private final BotAiMode mode;

        @Override
        public ItemStack getButtonItem(Player player) {
            boolean selected = this.kit.getBotAiMode() == this.mode;
            return new ItemBuilder(this.mode.getIcon())
                    .name((selected ? "&a&l" : "&e&l") + this.mode.getDisplayName())
                    .lore(
                            CC.MENU_BAR,
                            "&7" + this.mode.getDescription(),
                            "",
                            "&7Selected: " + (selected ? "&aYes" : "&cNo"),
                            "",
                            selected ? "&aCurrently active." : "&eClick to select.",
                            CC.MENU_BAR
                    )
                    .glow(selected)
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType != ClickType.LEFT) return;
            this.kit.setBotAiMode(this.mode);
            AlleyPlugin.getInstance().getService(KitService.class).saveKit(this.kit);
            player.sendMessage(CC.translate("&aBot AI mode for &6" + this.kit.getName()
                    + " &ais now &6" + this.mode.name() + "&a."));
            new BotAiModeMenu(this.kit).openMenu(player);
            this.playSuccess(player);
        }
    }

    @RequiredArgsConstructor
    private static class BackButton extends Button {
        private final Kit kit;

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.ARROW)
                    .name("&e&lBack")
                    .lore("&7Return to kit settings.")
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType != ClickType.LEFT) return;
            new KitSettingsMenu(this.kit).openMenu(player);
            this.playNeutral(player);
        }
    }
}
