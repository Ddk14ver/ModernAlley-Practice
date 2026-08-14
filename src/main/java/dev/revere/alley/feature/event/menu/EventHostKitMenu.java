package dev.revere.alley.feature.event.menu;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.item.ItemBuilder;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.feature.event.EventMode;
import dev.revere.alley.feature.event.EventService;
import dev.revere.alley.feature.event.EventType;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.kit.KitService;
import dev.revere.alley.feature.kit.setting.types.mode.KitSettingSumo;
import dev.revere.alley.library.menu.Button;
import dev.revere.alley.library.menu.pagination.PaginatedMenu;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class EventHostKitMenu extends PaginatedMenu {
    private final EventType type;
    private final EventMode mode;

    @Override
    public String getPrePaginatedTitle(Player player) {
        return "&6Select an Event Kit";
    }

    @Override
    public Map<Integer, Button> getGlobalButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        addGlassHeader(buttons, Material.BLACK_STAINED_GLASS_PANE);
        return buttons;
    }

    @Override
    public Map<Integer, Button> getAllPagesButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        int slot = 0;
        for (Kit kit : AlleyPlugin.getInstance().getService(KitService.class).getKits()) {
            if (!kit.isEnabled()) continue;
            if (this.type == EventType.SUMO && !kit.isSettingEnabled(KitSettingSumo.class)) continue;
            slot = validateSlot(slot);
            buttons.put(slot++, new KitButton(this.type, this.mode, kit));
        }
        addGlassToAvoidedSlots(buttons);
        return buttons;
    }

    @Override
    public int getSize() {
        return 45;
    }

    @RequiredArgsConstructor
    private static class KitButton extends Button {
        private final EventType type;
        private final EventMode mode;
        private final Kit kit;

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(this.kit.getIconItemOrDefault())
                    .name(this.kit.getMenuTitle())
                    .lore(
                            CC.MENU_BAR,
                            "&fEvent: &6" + this.type.getDisplayName(),
                            "&fMode: &6" + this.mode.getDisplayName(),
                            "&fKit: &6" + this.kit.getDisplayName(),
                            "",
                            "&aClick to queue this event.",
                            CC.MENU_BAR
                    )
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType != ClickType.LEFT) return;
            player.closeInventory();
            EventService service = AlleyPlugin.getInstance().getService(EventService.class);
            if (service.hostEvent(player, this.type, this.mode, this.kit) != null) {
                playSuccess(player);
            } else {
                playFail(player);
            }
        }
    }
}
