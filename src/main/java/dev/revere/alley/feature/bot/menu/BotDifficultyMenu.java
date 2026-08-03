package dev.revere.alley.feature.bot.menu;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.feature.bot.BotDifficultyProfile;
import dev.revere.alley.feature.bot.BotService;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.queue.menu.sub.BotQueueMenu;
import dev.revere.alley.library.menu.Button;
import dev.revere.alley.library.menu.Menu;
import dev.revere.alley.library.menu.impl.BackButton;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class BotDifficultyMenu extends Menu {
    private final Kit kit;

    @Override
    public String getTitle(Player player) {
        return "&6&lBot Duel: Difficulty";
    }

    @Override
    public int getSize() {
        return 45;
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        buttons.put(0, new BackButton(new BotQueueMenu()));

        int[] preferredSlots = {11, 13, 15, 20, 22, 24, 29, 31, 33};
        int index = 0;
        for (BotDifficultyProfile profile : AlleyPlugin.getInstance().getService(BotService.class).getProfiles().values()) {
            if (index >= preferredSlots.length) break;
            buttons.put(preferredSlots[index++], new BotDifficultyButton(kit, profile));
        }

        this.addBorder(buttons, Material.BLACK_STAINED_GLASS_PANE, 5);
        return buttons;
    }
}
