package dev.revere.alley.feature.arena.menu;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.item.ItemBuilder;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.feature.arena.Arena;
import dev.revere.alley.feature.arena.ArenaService;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.kit.KitService;
import dev.revere.alley.library.menu.Button;
import dev.revere.alley.library.menu.pagination.PaginatedMenu;
import lombok.AllArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 16/06/2026
 * Arena kit assignment menu - toggle which kits are available for an arena.
 * 竞技场套件分配菜单 - 切换哪些套件可用于竞技场。
 */
public class ArenaKitMenu extends PaginatedMenu {
    private final Arena arena;

    public ArenaKitMenu(Arena arena) {
        this.arena = arena;
    }

    @Override
    public String getPrePaginatedTitle(Player player) {
        return "&6&lKits for " + this.arena.getDisplayName();
    }

    @Override
    public int getMaxItemsPerPage() {
        return 28;
    }

    @Override
    public int getSize() {
        return 9 * 6;
    }

    @Override
    public Map<Integer, Button> getGlobalButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        this.addGlassHeader(buttons, Material.BLACK_STAINED_GLASS_PANE);

        buttons.put(48, new AddAllKitsButton(this.arena));
        buttons.put(49, new RemoveAllKitsButton(this.arena));
        buttons.put(53, new BackButton(this.arena));

        return buttons;
    }

    @Override
    public Map<Integer, Button> getAllPagesButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        List<Kit> allKits = this.plugin.getService(KitService.class).getKits();
        if (allKits.isEmpty()) {
            buttons.put(22, new NoKitsButton());
            return buttons;
        }

        int index = 0;
        for (Kit kit : allKits) {
            buttons.put(index++, new KitToggleButton(this.arena, kit));
        }

        return buttons;
    }

    /**
     * Button to toggle a kit's assignment for this arena.
     * 切换此竞技场套件分配的按钮。
     */
    @AllArgsConstructor
    private static class KitToggleButton extends Button {
        private final Arena arena;
        private final Kit kit;

        @Override
        public ItemStack getButtonItem(Player player) {
            boolean assigned = this.arena.getKits().contains(this.kit.getName());

            List<String> lore = new ArrayList<>();
            lore.add(CC.MENU_BAR);
            lore.add("&7Status: " + (assigned ? "&aAssigned" : "&cNot Assigned"));
            lore.add("&7Display: &f" + this.kit.getDisplayName());
            lore.add("&7Category: &f" + this.kit.getCategory().getName());
            lore.add("&7Enabled: " + (this.kit.isEnabled() ? "&aYes" : "&cNo"));
            lore.add("");
            lore.add("&eClick &7to " + (assigned ? "&cremove" : "&aadd") + " &7this kit.");
            lore.add(CC.MENU_BAR);

            return new ItemBuilder(assigned ? Material.LIME_DYE : Material.GRAY_DYE)
                    .name((assigned ? "&a" : "&c") + this.kit.getDisplayName())
                    .lore(lore)
                    .glow(assigned)
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType != ClickType.LEFT) return;

            ArenaService arenaService = AlleyPlugin.getInstance().getService(ArenaService.class);
            List<String> kits = this.arena.getKits();

            if (kits.contains(this.kit.getName())) {
                kits.remove(this.kit.getName());
                player.sendMessage(CC.translate("&cRemoved &6" + this.kit.getName() + " &cfrom arena &6" + this.arena.getName() + "&c."));
            } else {
                kits.add(this.kit.getName());
                player.sendMessage(CC.translate("&aAdded &6" + this.kit.getName() + " &ato arena &6" + this.arena.getName() + "&a."));
            }

            arenaService.saveArena(this.arena);
            new ArenaKitMenu(this.arena).openMenu(player);
            this.playSuccess(player);
        }
    }

    /**
     * Button to add all kits at once.
     * 一次性添加所有套件的按钮。
     */
    @AllArgsConstructor
    private static class AddAllKitsButton extends Button {
        private final Arena arena;

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.LIME_DYE)
                    .name("&a&lAdd All Kits")
                    .lore(
                            CC.MENU_BAR,
                            "&7Click to assign",
                            "&7all kits to this arena.",
                            CC.MENU_BAR
                    )
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType != ClickType.LEFT) return;

            List<Kit> allKits = AlleyPlugin.getInstance().getService(KitService.class).getKits();
            List<String> assigned = this.arena.getKits();

            int count = 0;
            for (Kit kit : allKits) {
                if (!assigned.contains(kit.getName())) {
                    assigned.add(kit.getName());
                    count++;
                }
            }

            AlleyPlugin.getInstance().getService(ArenaService.class).saveArena(this.arena);
            player.sendMessage(CC.translate("&aAdded &6" + count + " &akits to arena &6" + this.arena.getName() + "&a."));
            new ArenaKitMenu(this.arena).openMenu(player);
            this.playSuccess(player);
        }
    }

    /**
     * Button to remove all kits at once.
     * 一次性移除所有套件的按钮。
     */
    @AllArgsConstructor
    private static class RemoveAllKitsButton extends Button {
        private final Arena arena;

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.GRAY_DYE)
                    .name("&c&lRemove All Kits")
                    .lore(
                            CC.MENU_BAR,
                            "&7Click to remove all",
                            "&7assigned kits from this arena.",
                            CC.MENU_BAR
                    )
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType != ClickType.LEFT) return;

            int count = this.arena.getKits().size();
            this.arena.getKits().clear();

            AlleyPlugin.getInstance().getService(ArenaService.class).saveArena(this.arena);
            player.sendMessage(CC.translate("&cRemoved &6" + count + " &ckits from arena &6" + this.arena.getName() + "&c."));
            new ArenaKitMenu(this.arena).openMenu(player);
            this.playSuccess(player);
        }
    }

    /**
     * Return button to arena edit menu.
     * 返回竞技场编辑菜单的按钮。
     */
    @AllArgsConstructor
    private static class BackButton extends Button {
        private final Arena arena;

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.ARROW)
                    .name("&e&lBack")
                    .lore(
                            CC.MENU_BAR,
                            "&7Click to return to",
                            "&7the arena editor.",
                            CC.MENU_BAR
                    )
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType != ClickType.LEFT) return;
            new ArenaEditMenu(this.arena).openMenu(player);
            this.playNeutral(player);
        }
    }

    /**
     * Button shown when no kits exist.
     * 当没有套件存在时显示的按钮。
     */
    private static class NoKitsButton extends Button {
        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.BARRIER)
                    .name("&c&lNo Kits Available")
                    .lore(
                            CC.MENU_BAR,
                            "&7There are no kits yet.",
                            "&7Create kits first before",
                            "&7assigning them to arenas.",
                            CC.MENU_BAR
                    )
                    .hideMeta()
                    .build();
        }
    }
}
