package dev.revere.alley.feature.kit.menu;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.item.ItemBuilder;
import dev.revere.alley.common.text.CC;
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
 * @author Alley
 * @project Alley
 * @since 02/07/2025
 *
 * Main kit management GUI - lists all kits with pagination.
 * 主要工具包管理 GUI - 通过分页列出所有工具包。
 */
public class KitManagementMenu extends PaginatedMenu {

    @Override
    public String getPrePaginatedTitle(Player player) {
        return "&6&lKit Manager";
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

        buttons.put(48, new CreateKitButton());
        buttons.put(49, new CloseMenuButton());
        buttons.put(50, new SaveAllKitsButton());

        return buttons;
    }

    @Override
    public Map<Integer, Button> getAllPagesButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        List<Kit> kits = this.plugin.getService(KitService.class).getKits();
        if (kits.isEmpty()) {
            buttons.put(22, new EmptyInfoButton());
            return buttons;
        }

        int index = 0;
        for (Kit kit : kits) {
            buttons.put(index++, new KitDisplayButton(kit));
        }

        return buttons;
    }

    /**
     * Button displaying a kit entry in the management menu.
     * 在管理菜单中显示工具包条目的按钮。
     */
    @AllArgsConstructor
    private static class KitDisplayButton extends Button {
        private final Kit kit;

        @Override
        public ItemStack getButtonItem(Player player) {
            List<String> lore = new ArrayList<>();
            lore.add(CC.MENU_BAR);
            lore.add("&7ID: &f" + this.kit.getName());
            lore.add("&7Display: &f" + this.kit.getDisplayName());
            lore.add("&7Category: &f" + this.kit.getCategory().getName());
            lore.add("&7Enabled: " + (this.kit.isEnabled() ? "&aYes" : "&cNo"));
            lore.add("&7Editable: " + (this.kit.isEditable() ? "&aYes" : "&cNo"));
            lore.add("&7Settings: &f" + this.kit.getKitSettings().stream().filter(s -> s.isEnabled()).count() + " &7active");
            lore.add("");
            lore.add("&eLeft-Click &7to edit this kit.");
            lore.add("&eRight-Click &7to toggle enabled.");
            lore.add("&eShift-Click &7to delete.");
            lore.add(CC.MENU_BAR);

            return new ItemBuilder(this.kit.getIconItemOrDefault())
                    .name("&6" + this.kit.getDisplayName())
                    .durability(this.kit.getDurability())
                    .lore(lore)
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType == ClickType.LEFT) {
                new KitEditMenu(this.kit).openMenu(player);
                this.playSuccess(player);
            } else if (clickType == ClickType.RIGHT) {
                this.kit.setEnabled(!this.kit.isEnabled());
                AlleyPlugin.getInstance().getService(KitService.class).saveKit(this.kit);
                player.sendMessage(CC.translate("&aKit &6" + this.kit.getName() + " &ais now " + (this.kit.isEnabled() ? "&aenabled" : "&cdisabled") + "&a."));
                new KitManagementMenu().openMenu(player);
                this.playNeutral(player);
            } else if (clickType == ClickType.SHIFT_LEFT || clickType == ClickType.SHIFT_RIGHT) {
                AlleyPlugin.getInstance().getService(KitService.class).deleteKit(this.kit);
                player.sendMessage(CC.translate("&aKit &6" + this.kit.getName() + " &ahas been deleted."));
                new KitManagementMenu().openMenu(player);
                this.playNeutral(player);
            }
        }
    }

    /**
     * Button for creating a new kit.
     * 创建新工具包的按钮。
     */
    private static class CreateKitButton extends Button {
        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.EMERALD)
                    .name("&a&lCreate Kit")
                    .lore(
                            CC.MENU_BAR,
                            "&7Click to create a new kit.",
                            "&7Use &e/kit create <name> &7to",
                            "&7create a kit from your inventory.",
                            CC.MENU_BAR
                    )
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType != ClickType.LEFT) return;
            player.closeInventory();
            player.sendMessage(CC.translate("&eUse &6/kit create <name> &eto create a new kit."));
            this.playNeutral(player);
        }
    }

    /**
     * Button for saving all kits.
     * 保存所有工具包的按钮。
     */
    private static class SaveAllKitsButton extends Button {
        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.BOOK)
                    .name("&e&lSave All Kits")
                    .lore(
                            CC.MENU_BAR,
                            "&7Click to save all kits",
                            "&7to the configuration file.",
                            CC.MENU_BAR
                    )
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType != ClickType.LEFT) return;
            AlleyPlugin.getInstance().getService(KitService.class).saveKits();
            player.sendMessage(CC.translate("&aAll kits have been saved."));
            this.playSuccess(player);
        }
    }

    /**
     * Button displayed when there are no kits.
     * 当没有工具包时显示的按钮。
     */
    private static class EmptyInfoButton extends Button {
        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.BARRIER)
                    .name("&c&lNo Kits Found")
                    .lore(
                            CC.MENU_BAR,
                            "&7There are no kits yet.",
                            "&7Use &e/kit create <name>",
                            "&7to create your first kit.",
                            CC.MENU_BAR
                    )
                    .hideMeta()
                    .build();
        }
    }

    /**
     * Close menu button.
     * 关闭菜单按钮。
     */
    private static class CloseMenuButton extends Button {
        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.BARRIER)
                    .name("&c&lClose Menu")
                    .lore(
                            CC.MENU_BAR,
                            "&7Click to close this menu.",
                            CC.MENU_BAR
                    )
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType != ClickType.LEFT) return;
            player.closeInventory();
            this.playNeutral(player);
        }
    }
}
