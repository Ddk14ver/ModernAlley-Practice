package dev.revere.alley.feature.title.menu;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.item.ItemBuilder;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.feature.division.Division;
import dev.revere.alley.feature.division.DivisionService;
import dev.revere.alley.feature.title.internal.TitleServiceImpl;
import dev.revere.alley.feature.title.model.TitleRecord;
import dev.revere.alley.library.menu.Button;
import dev.revere.alley.library.menu.Menu;
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
 * Edit menu for a single title: prefix, required division, slot, enabled.
 * 单个头衔的编辑菜单：前缀、所需段位、槽位、启用状态。
 */
public class TitleEditMenu extends Menu {
    private final TitleRecord title;
    private final TitleServiceImpl titleService;

    public TitleEditMenu(TitleRecord title, TitleServiceImpl titleService) {
        this.title = title;
        this.titleService = titleService;
        setPlaceholder(true);
    }

    @Override
    public String getTitle(Player player) {
        return "&6&lEditing: " + this.title.getName();
    }

    @Override
    public int getSize() {
        return 9 * 4;
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        // Row 1: Title display
        buttons.put(4, new TitlePreviewButton(this.title));

        // Row 2: Edit properties
        buttons.put(19, new PrefixButton(this.title, this.titleService));
        buttons.put(22, new DivisionButton(this.title, this.titleService));
        buttons.put(25, new SlotButton(this.title, this.titleService));

        // Row 3: Toggles
        buttons.put(28, new ToggleEnabledButton(this.title, this.titleService));
        buttons.put(30, new TogglePurchasableButton(this.title, this.titleService));

        // Row 4: Navigation
        buttons.put(31, new SaveAndBackButton(this.title, this.titleService));
        buttons.put(34, new BackButton());

        this.addBorder(buttons, Material.BLACK_STAINED_GLASS_PANE, 4);

        return buttons;
    }

    /**
     * Shows the title's kit icon and current prefix.
     */
    private static class TitlePreviewButton extends Button {
        private final TitleRecord title;

        public TitlePreviewButton(TitleRecord title) {
            this.title = title;
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            Material icon = this.title.getKit() != null ? this.title.getKit().getIcon() : Material.NAME_TAG;
            int dur = this.title.getKit() != null ? this.title.getKit().getDurability() : 0;

            return new ItemBuilder(icon)
                    .name("&6&l" + this.title.getName())
                    .lore(
                            CC.MENU_BAR,
                            "&7Prefix: &f" + this.title.getPrefix(),
                            "&7Required: &f" + this.title.getRequiredDivision().getName(),
                            "&7Status: " + (this.title.isEnabled() ? "&aEnabled" : "&cDisabled"),
                            "&7Slot: &f" + this.title.getSlot(),
                            CC.MENU_BAR
                    )
                    .durability(dur)
                    .glow(this.title.isEnabled())
                    .hideMeta()
                    .build();
        }
    }

    /**
     * Edit the prefix via chat command hint.
     */
    private static class PrefixButton extends Button {
        private final TitleRecord title;
        private final TitleServiceImpl titleService;

        public PrefixButton(TitleRecord title, TitleServiceImpl titleService) {
            this.title = title;
            this.titleService = titleService;
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.OAK_SIGN)
                    .name("&6&lPrefix")
                    .lore(
                            CC.MENU_BAR,
                            "&7Current: " + this.title.getPrefix(),
                            "",
                            "&eClick &7to change via command.",
                            "&7Use &e/titlemanager prefix " + this.title.getName() + " <prefix>",
                            CC.MENU_BAR
                    )
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType != ClickType.LEFT) return;
            this.playNeutral(player);
        }
    }

    /**
     * Cycle through available divisions.
     */
    private static class DivisionButton extends Button {
        private final TitleRecord title;
        private final TitleServiceImpl titleService;

        public DivisionButton(TitleRecord title, TitleServiceImpl titleService) {
            this.title = title;
            this.titleService = titleService;
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.DIAMOND)
                    .name("&6&lRequired Division")
                    .lore(
                            CC.MENU_BAR,
                            "&7Current: &f" + this.title.getRequiredDivision().getName(),
                            "",
                            "&eLeft-Click &7to cycle forward.",
                            "&eRight-Click &7to cycle backward.",
                            CC.MENU_BAR
                    )
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            List<Division> divisions = AlleyPlugin.getInstance().getService(DivisionService.class).getDivisions();
            if (divisions.isEmpty()) return;

            int currentIdx = divisions.indexOf(this.title.getRequiredDivision());
            if (currentIdx < 0) currentIdx = 0;

            if (clickType == ClickType.LEFT) {
                currentIdx = (currentIdx + 1) % divisions.size();
            } else if (clickType == ClickType.RIGHT) {
                currentIdx = (currentIdx - 1 + divisions.size()) % divisions.size();
            } else {
                return;
            }

            this.title.setRequiredDivision(divisions.get(currentIdx));
            this.titleService.saveTitle(this.title);
            new TitleEditMenu(this.title, this.titleService).openMenu(player);
            this.playSuccess(player);
        }
    }

    /**
     * Adjust slot position up/down.
     */
    private static class SlotButton extends Button {
        private final TitleRecord title;
        private final TitleServiceImpl titleService;

        public SlotButton(TitleRecord title, TitleServiceImpl titleService) {
            this.title = title;
            this.titleService = titleService;
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.HOPPER)
                    .name("&6&lSlot Position: &e" + this.title.getSlot())
                    .lore(
                            CC.MENU_BAR,
                            "&7Current position: &f" + this.title.getSlot(),
                            "",
                            "&eLeft-Click &7to increase (+1).",
                            "&eRight-Click &7to decrease (-1).",
                            "&eShift-Left &7to jump up (+5).",
                            "&eShift-Right &7to jump down (-5).",
                            CC.MENU_BAR
                    )
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            int slot = this.title.getSlot();

            if (clickType == ClickType.LEFT) {
                slot++;
            } else if (clickType == ClickType.RIGHT) {
                slot = Math.max(0, slot - 1);
            } else if (clickType == ClickType.SHIFT_LEFT) {
                slot += 5;
            } else if (clickType == ClickType.SHIFT_RIGHT) {
                slot = Math.max(0, slot - 5);
            } else {
                return;
            }

            this.title.setSlot(slot);
            this.titleService.saveTitle(this.title);
            new TitleEditMenu(this.title, this.titleService).openMenu(player);
            this.playNeutral(player);
        }
    }

    /**
     * Toggle enabled state.
     */
    private static class ToggleEnabledButton extends Button {
        private final TitleRecord title;
        private final TitleServiceImpl titleService;

        public ToggleEnabledButton(TitleRecord title, TitleServiceImpl titleService) {
            this.title = title;
            this.titleService = titleService;
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            boolean enabled = this.title.isEnabled();
            return new ItemBuilder(enabled ? Material.LIME_DYE : Material.GRAY_DYE)
                    .name((enabled ? "&a" : "&c") + "&lEnabled: " + (enabled ? "YES" : "NO"))
                    .lore(
                            CC.MENU_BAR,
                            "&7Click to " + (enabled ? "disable" : "enable") + ".",
                            CC.MENU_BAR
                    )
                    .glow(enabled)
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType != ClickType.LEFT) return;
            this.title.setEnabled(!this.title.isEnabled());
            this.titleService.saveTitle(this.title);
            new TitleEditMenu(this.title, this.titleService).openMenu(player);
            this.playSuccess(player);
        }
    }

    /**
     * Toggle whether the title can be purchased in the shop.
     */
    private static class TogglePurchasableButton extends Button {
        private final TitleRecord title;
        private final TitleServiceImpl titleService;

        public TogglePurchasableButton(TitleRecord title, TitleServiceImpl titleService) {
            this.title = title;
            this.titleService = titleService;
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            boolean purchasable = this.title.isPurchasable();
            return new ItemBuilder(purchasable ? Material.GOLD_INGOT : Material.IRON_INGOT)
                    .name((purchasable ? "&6" : "&7") + "&lShop: " + (purchasable ? "Purchasable" : "Not for Sale"))
                    .lore(
                            CC.MENU_BAR,
                            "&7Title can be bought in shop: " + (purchasable ? "&aYes" : "&cNo"),
                            "",
                            "&eClick &7to toggle.",
                            purchasable ? "&7Use &e/shopmanager &7to set price." : "",
                            CC.MENU_BAR
                    )
                    .glow(purchasable)
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType != ClickType.LEFT) return;
            this.title.setPurchasable(!this.title.isPurchasable());
            this.titleService.saveTitle(this.title);
            new TitleEditMenu(this.title, this.titleService).openMenu(player);
            this.playSuccess(player);
        }
    }

    /**
     * Save and return to management menu.
     */
    private static class SaveAndBackButton extends Button {
        private final TitleRecord title;
        private final TitleServiceImpl titleService;

        public SaveAndBackButton(TitleRecord title, TitleServiceImpl titleService) {
            this.title = title;
            this.titleService = titleService;
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.WRITABLE_BOOK)
                    .name("&a&lSave & Back")
                    .lore(CC.MENU_BAR, "&7Click to save and return.", CC.MENU_BAR)
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType != ClickType.LEFT) return;
            this.titleService.saveTitle(this.title);
            player.sendMessage(CC.translate("&aTitle &6" + this.title.getName() + " &asaved!"));
            new TitleManagementMenu().openMenu(player);
            this.playSuccess(player);
        }
    }

    /**
     * Return without saving (changes already saved on each action).
     */
    private static class BackButton extends Button {
        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.ARROW)
                    .name("&e&lBack")
                    .lore(CC.MENU_BAR, "&7Click to return.", CC.MENU_BAR)
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType != ClickType.LEFT) return;
            new TitleManagementMenu().openMenu(player);
            this.playNeutral(player);
        }
    }
}
