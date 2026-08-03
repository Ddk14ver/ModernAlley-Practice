package dev.revere.alley.feature.leaderboard.menu;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.item.ItemBuilder;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.feature.leaderboard.LeaderboardType;
import dev.revere.alley.feature.leaderboard.hologram.Hologram;
import dev.revere.alley.feature.leaderboard.hologram.HologramManager;
import dev.revere.alley.feature.leaderboard.hologram.LeaderboardHologram;
import dev.revere.alley.library.menu.Button;
import dev.revere.alley.library.menu.Menu;
import lombok.AllArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * @author Alley
 * @project Alley
 * @since 02/07/2025
 *
 * GUI for managing leaderboard holograms visually.
 * 可视化管理排行榜全息图的GUI。
 */
public class HologramMenu extends Menu {

    @Override
    public String getTitle(Player player) {
        return "&6&lHologram Manager";
    }

    @Override
    public int getSize() {
        return 9 * 6;
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        HologramManager manager = AlleyPlugin.getInstance().getService(HologramManager.class);
        List<Hologram> holograms = manager.getHolograms();

        int slot = 10;
        for (Hologram holo : holograms) {
            if (slot > 43) break;
            buttons.put(slot++, new HologramButton(holo, manager));
        }

        buttons.put(40, new CreateInfoButton());
        buttons.put(49, new CloseMenuButton());

        this.addGlass(buttons, Material.BLACK_STAINED_GLASS_PANE);

        return buttons;
    }

    @AllArgsConstructor
    private static class HologramButton extends Button {
        private final Hologram hologram;
        private final HologramManager manager;

        @Override
        public ItemStack getButtonItem(Player player) {
            List<String> lore = new ArrayList<>();
            lore.add(CC.MENU_BAR);
            lore.add("&7Status: " + (this.hologram.isEnabled() ? "&aEnabled" : "&cDisabled"));

            if (this.hologram instanceof LeaderboardHologram lb) {
                lore.add("&7Type: &f" + lb.getLeaderboardType().getName());
                lore.add("&7Kit: &f" + (lb.getKitName() != null ? lb.getKitName() : "All/Global"));
                lore.add("&7Show Top: &f" + lb.getShowStat());
            }

            if (this.hologram.getBaseLocation() != null) {
                lore.add("&7World: &f" + this.hologram.getBaseLocation().getWorld().getName());
                lore.add("&7Pos: &f" + this.hologram.getBaseLocation().getBlockX() + ", " + this.hologram.getBaseLocation().getBlockY() + ", " + this.hologram.getBaseLocation().getBlockZ());
            } else {
                lore.add("&7Location: &cNot Set");
            }

            lore.add("");
            lore.add("&eLeft-Click &7to teleport here.");
            lore.add("&eRight-Click &7to toggle enable/disable.");
            lore.add("&eShift-Click &7to delete.");
            lore.add(CC.MENU_BAR);

            return new ItemBuilder(Material.BEACON)
                    .name("&6&l" + this.hologram.getName())
                    .lore(lore)
                    .glow(this.hologram.isEnabled())
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType == ClickType.LEFT) {
                if (this.hologram.getBaseLocation() != null) {
                    player.teleport(this.hologram.getBaseLocation().clone().add(0, 2, 0));
                    player.sendMessage(CC.translate("&aTeleported to hologram &6" + this.hologram.getName() + "&a."));
                    this.playSuccess(player);
                }
            } else if (clickType == ClickType.RIGHT) {
                this.hologram.setEnabled(!this.hologram.isEnabled());
                this.manager.saveHologram(this.hologram);
                if (this.hologram.isEnabled()) {
                    this.hologram.updateContent();
                } else {
                    this.hologram.despawn();
                }
                player.sendMessage(CC.translate("&aHologram &6" + this.hologram.getName() + " &ais now " + (this.hologram.isEnabled() ? "&aenabled" : "&cdisabled") + "&a."));
                new HologramMenu().openMenu(player);
                this.playNeutral(player);
            } else if (clickType == ClickType.SHIFT_LEFT || clickType == ClickType.SHIFT_RIGHT) {
                String holoName = this.hologram.getName();
                HologramManager mgr = AlleyPlugin.getInstance().getService(HologramManager.class);
                mgr.deleteHologram(holoName);
                player.sendMessage(CC.translate("&aHologram '&6" + holoName + "&a' deleted."));
                new HologramMenu().openMenu(player);
                this.playSuccess(player);
            }
        }
    }

    private static class CreateInfoButton extends Button {
        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.EMERALD)
                    .name("&a&lCreate Hologram")
                    .lore(
                            CC.MENU_BAR,
                            "&7Use &e/hologram create <name> <type> [kit]",
                            "&7to create a new hologram.",
                            "",
                            "&7Types: &fRANKED, UNRANKED, WIN_STREAK,",
                            "&fFFA, TOURNAMENT, UNRANKED_MONTHLY",
                            CC.MENU_BAR
                    )
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType == ClickType.LEFT) {
                player.closeInventory();
                player.sendMessage(CC.translate("&eUse &6/hologram create <name> <type> [kit] &eto create."));
            }
        }
    }

    private static class CloseMenuButton extends Button {
        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.BARRIER)
                    .name("&c&lClose")
                    .lore(CC.MENU_BAR, "&7Click to close.", CC.MENU_BAR)
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType == ClickType.LEFT) player.closeInventory();
        }
    }
}
