package dev.revere.alley.library.menu.pagination.impl.menu;

import dev.revere.alley.library.menu.Button;
import dev.revere.alley.library.menu.Menu;
import dev.revere.alley.library.menu.pagination.PaginatedMenu;
import dev.revere.alley.library.menu.pagination.impl.button.JumpToPageButton;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class ViewAllPagesMenu extends Menu {

    @NonNull
    PaginatedMenu menu;

    @Override
    public String getTitle(Player player) {
        return "&6&lSelect A Page";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        int index = 10;
        for (int i = 1; i <= menu.getPages(player); i++) {
            buttons.put(index++, new JumpToPageButton(i, menu, menu.getPage() == i));

            if ((index - 8) % 9 == 0) {
                index += 2;
            }
        }

        this.addBorder(buttons, Material.BLACK_STAINED_GLASS_PANE, 5);

        return buttons;
    }

    @Override
    public int getSize() {
        return 9 * 5;
    }

    @Override
    public boolean isAutoUpdate() {
        return true;
    }

}
