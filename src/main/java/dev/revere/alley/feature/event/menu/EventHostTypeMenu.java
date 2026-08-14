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

public class EventHostTypeMenu extends Menu {
    private static final int[] SLOTS = {10, 12, 14, 16};

    @Override
    public String getTitle(Player player) {
        return "&6&lHost an Event";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        EventType[] types = EventType.values();
        for (int index = 0; index < types.length; index++) {
            buttons.put(SLOTS[index], new EventTypeButton(types[index]));
        }
        addGlass(buttons, Material.BLACK_STAINED_GLASS_PANE);
        return buttons;
    }

    @Override
    public int getSize() {
        return 27;
    }

    @RequiredArgsConstructor
    private static class EventTypeButton extends Button {
        private final EventType type;

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(this.type.getIcon())
                    .name("&6&l" + this.type.getDisplayName())
                    .lore(
                            CC.MENU_BAR,
                            "&7" + this.type.getDescription(),
                            "",
                            this.type == EventType.SUMO
                                    ? "&aClick to select the event mode."
                                    : "&aClick to select a kit.",
                            CC.MENU_BAR
                    )
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType != ClickType.LEFT) return;
            if (this.type == EventType.SUMO) {
                new EventHostModeMenu(this.type).openMenu(player);
            } else {
                new EventHostKitMenu(this.type, this.type.getDefaultMode()).openMenu(player);
            }
            playNeutral(player);
        }
    }
}
