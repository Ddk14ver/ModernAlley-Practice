package dev.revere.alley.feature.event.menu;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.item.ItemBuilder;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.feature.event.EventService;
import dev.revere.alley.feature.event.EventState;
import dev.revere.alley.feature.event.HostedEvent;
import dev.revere.alley.library.menu.Button;
import dev.revere.alley.library.menu.Menu;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class EventsMenu extends Menu {
    @Override
    public String getTitle(Player player) {
        return "&6&lActive Events";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        int slot = 10;
        for (HostedEvent event : AlleyPlugin.getInstance().getService(EventService.class).getEvents()) {
            buttons.put(slot++, new EventButton(event));
            if (slot == 17 || slot == 26 || slot == 35) slot += 2;
        }
        addBorder(buttons, Material.BLACK_STAINED_GLASS_PANE, 5);
        return buttons;
    }

    @Override
    public int getSize() {
        return 45;
    }

    @RequiredArgsConstructor
    private static class EventButton extends Button {
        private final HostedEvent event;

        @Override
        public ItemStack getButtonItem(Player player) {
            String action = this.event.getState() == EventState.STARTING
                    ? "&aClick to join the event."
                    : this.event.getState() == EventState.QUEUED
                    ? "&7Waiting for the current event to end."
                    : "&7The event is already running.";
            return new ItemBuilder(this.event.getType().getIcon())
                    .name("&6&l" + this.event.getDisplayName() + " &7(#" + this.event.getNumericId() + ")")
                    .lore(
                            CC.MENU_BAR,
                            "&fHost: &6" + this.event.getHostName(),
                            "&fKit: &6" + this.event.getKit().getDisplayName(),
                            "&fMode: &6" + this.event.getMode().getDisplayName(),
                            "&fState: &6" + this.event.getState().getDisplayName(),
                            "&fPlayers: &6" + this.event.getParticipants().size() + "&7/&6" + this.event.getMaxPlayers(),
                            "",
                            action,
                            CC.MENU_BAR
                    )
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType != ClickType.LEFT || this.event.getState() != EventState.STARTING) return;
            EventService service = AlleyPlugin.getInstance().getService(EventService.class);
            if (service.joinEvent(player, this.event)) {
                player.closeInventory();
                playSuccess(player);
            } else {
                playFail(player);
            }
        }
    }
}
