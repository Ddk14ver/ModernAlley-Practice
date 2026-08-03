package dev.revere.alley.feature.division.menu;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.core.profile.menu.setting.PracticeSettingsMenu;
import dev.revere.alley.library.menu.Button;
import dev.revere.alley.library.menu.Menu;
import dev.revere.alley.library.menu.impl.BackButton;
import dev.revere.alley.feature.division.Division;
import dev.revere.alley.feature.division.DivisionService;
import org.bukkit.entity.Player;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Emmy
 * 作者：Emmy
 * @project Alley
 * 项目：Alley
 * @since 25/01/2025
 * 自：25/01/2025
 */
public class DivisionsMenu extends Menu {
    @Override
    public String getTitle(Player player) {
        return "&6&lDivisions";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        final Map<Integer, Button> buttons = new HashMap<>();

        int slot = 10;
        for (Division division : AlleyPlugin.getInstance().getService(DivisionService.class).getDivisions()) {
            buttons.put(slot++, new DivisionButton(division));
            if (slot == 17 || slot == 26 || slot == 35 || slot == 44 || slot == 53) {
                slot += 2;
            }
        }

        this.addBorder(buttons, Material.BLACK_STAINED_GLASS_PANE, 6);

        buttons.put(49, new BackButton(new PracticeSettingsMenu()));

        return buttons;
    }

    @Override
    public int getSize() {
        return 9 * 6;
    }
}