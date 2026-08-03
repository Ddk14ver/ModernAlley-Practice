package dev.revere.alley.feature.queue.menu.sub;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.feature.bot.BotService;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.kit.KitService;
import dev.revere.alley.feature.queue.menu.button.BotButton;
import dev.revere.alley.library.menu.Button;
import dev.revere.alley.library.menu.pagination.PaginatedMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class BotQueueMenu extends PaginatedMenu {
    @Override
    public String getPrePaginatedTitle(Player player) {
        return "&6&lBot Duel: Select Kit";
    }

    @Override
    public int getMaxItemsPerPage() {
        return 28;
    }

    @Override
    public int getSize() {
        return 54;
    }

    @Override
    public Map<Integer, Button> getGlobalButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        this.addGlassHeader(buttons, Material.BLACK_STAINED_GLASS_PANE);
        for (int slot = 45; slot < 54; slot++) {
            buttons.put(slot, Button.placeholder(Material.BLACK_STAINED_GLASS_PANE, ""));
        }
        return buttons;
    }

    @Override
    public Map<Integer, Button> getAllPagesButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        BotService botService = AlleyPlugin.getInstance().getService(BotService.class);
        int index = 0;
        for (Kit kit : AlleyPlugin.getInstance().getService(KitService.class).getKits()) {
            if (botService.isKitSupported(kit)) buttons.put(index++, new BotButton(kit));
        }
        return buttons;
    }
}
