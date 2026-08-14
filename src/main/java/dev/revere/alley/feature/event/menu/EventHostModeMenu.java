package dev.revere.alley.feature.event.menu;

import dev.revere.alley.common.item.ItemBuilder;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.feature.event.EventMode;
import dev.revere.alley.feature.event.EventType;
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
public class EventHostModeMenu extends Menu {
    private final EventType type;

    @Override
    public String getTitle(Player player) {
        return "&6&lSelect Event Mode";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        buttons.put(11, new ModeButton(this.type, EventMode.BRACKETS, Material.IRON_SWORD));
        buttons.put(15, new ModeButton(this.type, EventMode.LAST_MAN_STANDING, Material.TNT));
        addGlass(buttons, Material.BLACK_STAINED_GLASS_PANE);
        return buttons;
    }

    @Override
    public int getSize() {
        return 27;
    }

    @RequiredArgsConstructor
    private static class ModeButton extends Button {
        private final EventType type;
        private final EventMode mode;
        private final Material material;

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(this.material)
                    .name("&6&l" + this.mode.getDisplayName())
                    .lore(
                            CC.MENU_BAR,
                            this.mode == EventMode.BRACKETS
                                    ? "&7Fight through elimination rounds."
                                    : "&7All players fight at the same time.",
                            "",
                            "&aClick to select a kit.",
                            CC.MENU_BAR
                    )
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType != ClickType.LEFT) return;
            new EventHostKitMenu(this.type, this.mode).openMenu(player);
            playNeutral(player);
        }
    }
}
