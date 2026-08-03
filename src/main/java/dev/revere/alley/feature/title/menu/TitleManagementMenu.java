package dev.revere.alley.feature.title.menu;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.item.ItemBuilder;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.feature.title.TitleService;
import dev.revere.alley.feature.title.internal.TitleServiceImpl;
import dev.revere.alley.feature.title.model.TitleRecord;
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
 * @since 03/07/2025
 *
 * Admin GUI for managing all titles: enable/disable, reorder, edit.
 * 管理所有头衔的管理员GUI：启用/禁用、重新排序、编辑。
 */
public class TitleManagementMenu extends PaginatedMenu {

    @Override
    public String getPrePaginatedTitle(Player player) {
        return "&6&lTitle Manager";
    }

    @Override
    public int getSize() {
        return 9 * 6;
    }

    @Override
    public int getMaxItemsPerPage() {
        return 28;
    }

    @Override
    public Map<Integer, Button> getGlobalButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        this.addGlassHeader(buttons, Material.BLACK_STAINED_GLASS_PANE);
        buttons.put(49, new SaveAllButton());
        buttons.put(53, new CloseMenuButton());
        return buttons;
    }

    @Override
    public Map<Integer, Button> getAllPagesButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        TitleServiceImpl titleService = (TitleServiceImpl) AlleyPlugin.getInstance().getService(TitleService.class);
        List<TitleRecord> titles = titleService.getSortedTitles();

        int index = 0;
        for (TitleRecord title : titles) {
            buttons.put(index++, new TitleManageButton(title, titleService));
        }

        return buttons;
    }

    @AllArgsConstructor
    private static class TitleManageButton extends Button {
        private final TitleRecord title;
        private final TitleServiceImpl titleService;

        @Override
        public ItemStack getButtonItem(Player player) {
            List<String> lore = new ArrayList<>();
            lore.add(CC.MENU_BAR);
            lore.add("&7Name: &f" + this.title.getName());
            lore.add("&7Prefix: &f" + this.title.getPrefix());
            lore.add("&7Required: &f" + this.title.getRequiredDivision().getName());
            lore.add("&7Status: " + (this.title.isEnabled() ? "&aEnabled" : "&cDisabled"));
            lore.add("&7Slot: &f" + this.title.getSlot());
            lore.add("");
            lore.add("&eLeft-Click &7to edit this title.");
            lore.add("&eRight-Click &7to toggle enabled/disabled.");
            lore.add(CC.MENU_BAR);

            Material icon = this.title.getKit() != null ? this.title.getKit().getIcon() : Material.NAME_TAG;
            int dur = this.title.getKit() != null ? this.title.getKit().getDurability() : 0;

            return new ItemBuilder(icon)
                    .name((this.title.isEnabled() ? "&a" : "&c") + this.title.getName())
                    .lore(lore)
                    .durability(dur)
                    .glow(this.title.isEnabled())
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType == ClickType.LEFT) {
                new TitleEditMenu(this.title, this.titleService).openMenu(player);
                this.playSuccess(player);
            } else if (clickType == ClickType.RIGHT) {
                this.title.setEnabled(!this.title.isEnabled());
                this.titleService.saveTitle(this.title);
                player.sendMessage(CC.translate("&aTitle &6" + this.title.getName() + " &ais now " + (this.title.isEnabled() ? "&aenabled" : "&cdisabled") + "&a."));
                new TitleManagementMenu().openMenu(player);
                this.playNeutral(player);
            }
        }
    }

    private static class SaveAllButton extends Button {
        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.WRITABLE_BOOK)
                    .name("&a&lSave All")
                    .lore(CC.MENU_BAR, "&7Click to save all titles.", CC.MENU_BAR)
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType != ClickType.LEFT) return;
            TitleServiceImpl titleService = (TitleServiceImpl) AlleyPlugin.getInstance().getService(TitleService.class);
            titleService.saveAllTitles();
            player.sendMessage(CC.translate("&aAll titles saved!"));
            this.playSuccess(player);
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
