package dev.revere.alley.feature.title.menu;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.core.profile.menu.setting.PracticeSettingsMenu;
import dev.revere.alley.library.menu.Button;
import dev.revere.alley.library.menu.impl.BackButton;
import dev.revere.alley.library.menu.pagination.PaginatedMenu;
import dev.revere.alley.feature.title.TitleService;
import dev.revere.alley.feature.title.internal.TitleServiceImpl;
import dev.revere.alley.feature.title.model.TitleRecord;
import dev.revere.alley.core.profile.Profile;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.Material;

import java.util.*;

/**
 * @author Emmy
 * @project Alley
 * @since 22/04/2025
 */
@AllArgsConstructor
public class TitleMenu extends PaginatedMenu {
    private final Profile profile;

    @Override
    public String getPrePaginatedTitle(Player player) {
        return "&6&lYour Titles";
    }

    @Override
    public Map<Integer, Button> getGlobalButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        this.addGlassHeader(buttons, Material.BLACK_STAINED_GLASS_PANE);
        buttons.put(40, new BackButton(new PracticeSettingsMenu()));
        return buttons;
    }

    @Override
    public Map<Integer, Button> getAllPagesButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        TitleServiceImpl titleService = (TitleServiceImpl) AlleyPlugin.getInstance().getService(TitleService.class);
        List<TitleRecord> titles = titleService.getSortedTitles();

        int slot = 0;
        for (TitleRecord title : titles) {
            // Only show enabled titles
            if (!title.isEnabled()) continue;

            slot = this.validateSlot(slot);
            buttons.put(slot++, new TitleButton(this.profile, title));
        }

        this.addGlassToAvoidedSlots(buttons);

        return buttons;
    }

    @Override
    public int getSize() {
        return 9 * 5;
    }
}
