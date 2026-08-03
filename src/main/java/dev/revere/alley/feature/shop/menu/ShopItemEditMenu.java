package dev.revere.alley.feature.shop.menu;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.item.ItemBuilder;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.feature.cosmetic.model.CosmeticType;
import dev.revere.alley.feature.shop.ShopDataManager;
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
 * Edit menu for a single shop item: adjust price, toggle enabled.
 * 单个商店物品的编辑菜单：调整价格、切换启用。
 */
public class ShopItemEditMenu extends Menu {
    private final ShopManagementMenu.ShopItemEntry entry;

    public ShopItemEditMenu(ShopManagementMenu.ShopItemEntry entry) {
        this.entry = entry;
        setPlaceholder(true);
    }

    @Override
    public String getTitle(Player player) {
        return "&6&lEdit: " + this.entry.name;
    }

    @Override
    public int getSize() {
        return 9 * 3;
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        buttons.put(4, new InfoButton(this.entry));
        buttons.put(11, new PriceDownButton(this.entry));
        buttons.put(13, new PriceDisplayButton(this.entry));
        buttons.put(15, new PriceUpButton(this.entry));
        buttons.put(22, new BackButton());

        this.addBorder(buttons, Material.BLACK_STAINED_GLASS_PANE, 3);

        return buttons;
    }

    private static class InfoButton extends Button {
        private final ShopManagementMenu.ShopItemEntry entry;

        public InfoButton(ShopManagementMenu.ShopItemEntry entry) {
            this.entry = entry;
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            Material icon;
            if (entry.isTitle) {
                icon = entry.title.getKit() != null ? entry.title.getKit().getIcon() : Material.NAME_TAG;
            } else {
                icon = entry.cosmetic != null ? entry.cosmetic.getIcon() : Material.BARRIER;
            }

            ShopDataManager shopData = AlleyPlugin.getInstance().getService(ShopDataManager.class);
            int price = shopData.getPrice(entry.category.name(), entry.name, entry.defaultPrice);
            boolean enabled = shopData.isEnabled(entry.category.name(), entry.name, true);

            return new ItemBuilder(icon)
                    .name((enabled ? "&a" : "&c") + entry.name)
                    .lore(
                            CC.MENU_BAR,
                            "&7Category: &f" + entry.category.name(),
                            "&7Price: &6$" + price,
                            "&7Status: " + (enabled ? "&aEnabled" : "&cDisabled"),
                            CC.MENU_BAR
                    )
                    .glow(enabled)
                    .hideMeta()
                    .build();
        }
    }

    private static class PriceDownButton extends Button {
        private final ShopManagementMenu.ShopItemEntry entry;

        public PriceDownButton(ShopManagementMenu.ShopItemEntry entry) {
            this.entry = entry;
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.RED_DYE)
                    .name("&c&lDecrease Price")
                    .lore(
                            CC.MENU_BAR,
                            "&7Left: -10",
                            "&7Right: -100",
                            "&7Shift: -1000",
                            CC.MENU_BAR
                    )
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            ShopDataManager shopData = AlleyPlugin.getInstance().getService(ShopDataManager.class);
            int current = shopData.getPrice(entry.category.name(), entry.name, entry.defaultPrice);
            int delta = clickType == ClickType.SHIFT_LEFT || clickType == ClickType.SHIFT_RIGHT ? 1000
                    : clickType == ClickType.RIGHT ? 100 : 10;
            int newPrice = Math.max(0, current - delta);
            shopData.setPrice(entry.category.name(), entry.name, newPrice);
            new ShopItemEditMenu(entry).openMenu(player);
            this.playNeutral(player);
        }
    }

    private static class PriceDisplayButton extends Button {
        private final ShopManagementMenu.ShopItemEntry entry;

        public PriceDisplayButton(ShopManagementMenu.ShopItemEntry entry) {
            this.entry = entry;
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            ShopDataManager shopData = AlleyPlugin.getInstance().getService(ShopDataManager.class);
            int price = shopData.getPrice(entry.category.name(), entry.name, entry.defaultPrice);
            boolean enabled = shopData.isEnabled(entry.category.name(), entry.name, true);

            return new ItemBuilder(Material.GOLD_NUGGET)
                    .name("&6&lPrice: $" + price)
                    .lore(
                            CC.MENU_BAR,
                            "&7Status: " + (enabled ? "&aEnabled" : "&cDisabled"),
                            "",
                            "&eClick &7to toggle enable/disable.",
                            "&7Use left/right buttons to adjust price.",
                            CC.MENU_BAR
                    )
                    .glow(enabled)
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType != ClickType.LEFT) return;
            ShopDataManager shopData = AlleyPlugin.getInstance().getService(ShopDataManager.class);
            boolean current = shopData.isEnabled(entry.category.name(), entry.name, true);
            shopData.setEnabled(entry.category.name(), entry.name, !current);
            new ShopItemEditMenu(entry).openMenu(player);
            this.playSuccess(player);
        }
    }

    private static class PriceUpButton extends Button {
        private final ShopManagementMenu.ShopItemEntry entry;

        public PriceUpButton(ShopManagementMenu.ShopItemEntry entry) {
            this.entry = entry;
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.LIME_DYE)
                    .name("&a&lIncrease Price")
                    .lore(
                            CC.MENU_BAR,
                            "&7Left: +10",
                            "&7Right: +100",
                            "&7Shift: +1000",
                            CC.MENU_BAR
                    )
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            ShopDataManager shopData = AlleyPlugin.getInstance().getService(ShopDataManager.class);
            int current = shopData.getPrice(entry.category.name(), entry.name, entry.defaultPrice);
            int delta = clickType == ClickType.SHIFT_LEFT || clickType == ClickType.SHIFT_RIGHT ? 1000
                    : clickType == ClickType.RIGHT ? 100 : 10;
            shopData.setPrice(entry.category.name(), entry.name, current + delta);
            new ShopItemEditMenu(entry).openMenu(player);
            this.playNeutral(player);
        }
    }

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
            new ShopManagementMenu().openMenu(player);
            this.playNeutral(player);
        }
    }
}
