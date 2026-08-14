package dev.revere.alley.core.profile.menu.setting;

import dev.revere.alley.core.profile.menu.setting.button.MatchHistoryButton;
import dev.revere.alley.core.profile.menu.setting.button.MatchSettingsButton;
import dev.revere.alley.core.profile.menu.setting.enums.MatchSettingType;
import dev.revere.alley.library.menu.Button;
import dev.revere.alley.library.menu.Menu;
import dev.revere.alley.library.menu.impl.BackButton;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 26/07/2026
 */
public class MatchSettingsMenu extends Menu {
    @Override
    public String getTitle(Player player) {
        return "&6&lMatch Settings";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        for (MatchSettingType type : MatchSettingType.values()) {
            buttons.put(type.slot, new MatchSettingsButton(type));
        }

        this.addBorder(buttons, Material.BLACK_STAINED_GLASS_PANE, 4);
        buttons.put(0, new BackButton(new PracticeSettingsMenu()));
        buttons.put(8, new MatchHistoryButton());
        return buttons;
    }

    @Override
    public int getSize() {
        return 9 * 4;
    }
}
