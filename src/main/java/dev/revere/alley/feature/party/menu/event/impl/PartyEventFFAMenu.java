package dev.revere.alley.feature.party.menu.event.impl;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.feature.kit.setting.types.mode.KitSettingPartyFFA;
import dev.revere.alley.feature.party.menu.event.impl.button.PartyEventFFAButton;
import dev.revere.alley.feature.party.menu.event.impl.button.PartyEventSplitButton;
import dev.revere.alley.feature.queue.Queue;
import dev.revere.alley.feature.queue.QueueService;
import dev.revere.alley.library.menu.Button;
import dev.revere.alley.library.menu.Menu;
import org.bukkit.entity.Player;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

/**
 * 自由混战活动菜单，供队长选择自由混战的装备包。
 * @author Emmy
 * @project Alley
 * @date 08/10/2024 - 18:39
 */
public class PartyEventFFAMenu extends Menu {
    @Override
    public String getTitle(Player player) {
        return "&6&lSelect a kit";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        final Map<Integer, Button> buttons = new HashMap<>();

        int slot = 10;
        for (Queue queue : AlleyPlugin.getInstance().getService(QueueService.class).getQueues()) {
            if (!queue.isRanked() &&
                    !queue.isDuos() &&
                    queue.getKit().isEnabled() &&
                    queue.getKit().isSettingEnabled(KitSettingPartyFFA.class)) {
                slot = this.skipIfSlotCrossingBorder(slot);
                buttons.put(slot++, new PartyEventFFAButton(queue.getKit()));
            }
        }

        this.addGlass(buttons, Material.BLACK_STAINED_GLASS_PANE);

        return buttons;
    }

    @Override
    public int getSize() {
        return 9 * 6;
    }
}
