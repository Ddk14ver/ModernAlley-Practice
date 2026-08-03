package dev.revere.alley.feature.staff.menu;

import dev.revere.alley.common.item.ItemBuilder;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.library.menu.Button;
import dev.revere.alley.library.menu.Menu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.*;
/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 10/07/2026
 *
 * Troll submenu — 5 troll actions against a target player.
 */
public class TrollMenu extends Menu {
    private final Player target;

    public TrollMenu(Player target) {
        this.target = target;
    }

    @Override public String getTitle(Player p) { return "&c&lTroll: " + target.getName(); }
    @Override public int getSize() { return 27; }

    @Override
    public Map<Integer, Button> getButtons(Player viewer) {
        Map<Integer, Button> buttons = new HashMap<>();
        buttons.put(11, trollButton(Material.BEACON, "&6&lDonut", "/donut " + target.getName(), "Spawns a ring of boats around the player."));
        buttons.put(12, trollButton(Material.TNT, "&c&lTroll", "/troll " + target.getName(), "Random troll effect."));
        buttons.put(13, trollButton(Material.PISTON, "&e&lPush", "/push " + target.getName() + " 10", "Pushes the player away."));
        buttons.put(14, trollButton(Material.FIREWORK_ROCKET, "&a&lLaunch", "/launch " + target.getName(), "Launches the player into the air."));
        buttons.put(15, trollButton(Material.REDSTONE, "&4&lHeart Attack", "/heartattack " + target.getName(), "Fake heart attack effect."));
        return buttons;
    }

    private Button trollButton(Material mat, String name, String cmd, String desc) {
        return new Button() {
            public ItemStack getButtonItem(Player p) {
                return new ItemBuilder(mat).name(name).lore(CC.MENU_BAR, "&7" + desc, "", "&aClick to execute.", CC.MENU_BAR).hideMeta().build();
            }
            public void clicked(Player p, ClickType c) {
                if (c == ClickType.LEFT) {
                    p.performCommand(cmd.substring(1)); // strip leading /
                    p.closeInventory();
                }
            }
        };
    }
}
