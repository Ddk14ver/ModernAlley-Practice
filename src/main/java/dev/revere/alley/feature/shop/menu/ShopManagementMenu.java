package dev.revere.alley.feature.shop.menu;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.item.ItemBuilder;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.feature.cosmetic.CosmeticService;
import dev.revere.alley.feature.cosmetic.internal.repository.BaseCosmeticRepository;
import dev.revere.alley.feature.cosmetic.model.BaseCosmetic;
import dev.revere.alley.feature.cosmetic.model.Cosmetic;
import dev.revere.alley.feature.cosmetic.model.CosmeticType;
import dev.revere.alley.feature.shop.ShopDataManager;
import dev.revere.alley.feature.title.TitleService;
import dev.revere.alley.feature.title.internal.TitleServiceImpl;
import dev.revere.alley.feature.title.model.TitleRecord;
import dev.revere.alley.library.menu.Button;
import dev.revere.alley.library.menu.pagination.PaginatedMenu;
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
 * Admin GUI for managing all shop items: price, enabled, category.
 * 管理所有商店物品的管理员GUI：价格、启用、分类。
 */
public class ShopManagementMenu extends PaginatedMenu {
    private final List<ShopItemEntry> allItems;

    public ShopManagementMenu() {
        this.allItems = buildItemList();
    }

    @Override
    public String getPrePaginatedTitle(Player player) {
        return "&6&lShop Manager";
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
        return buttons;
    }

    @Override
    public Map<Integer, Button> getAllPagesButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        int index = 0;
        for (ShopItemEntry entry : this.allItems) {
            buttons.put(index++, new ShopManageButton(entry));
        }
        return buttons;
    }

    private List<ShopItemEntry> buildItemList() {
        List<ShopItemEntry> items = new ArrayList<>();
        CosmeticService cosmeticService = AlleyPlugin.getInstance().getService(CosmeticService.class);

        // Load cosmetics
        for (CosmeticType type : CosmeticType.values()) {
            if (type == CosmeticType.TITLE) continue; // Handled separately
            BaseCosmeticRepository<?> repo = cosmeticService.getRepository(type);
            if (repo == null) continue;
            for (Cosmetic cosmetic : repo.getCosmetics()) {
                if (cosmetic.getIcon() == null) continue;
                items.add(new ShopItemEntry(cosmetic, type, cosmetic.getName(), cosmetic.getPrice()));
            }
        }

        // Load titles
        TitleServiceImpl titleService = (TitleServiceImpl) AlleyPlugin.getInstance().getService(TitleService.class);
        for (TitleRecord title : titleService.getSortedTitles()) {
            if (!title.isPurchasable()) continue;
            items.add(new ShopItemEntry(title));
        }

        return items;
    }

    /**
     * Wrapper for a shop item entry (cosmetic or title).
     */
    public static class ShopItemEntry {
        public final Cosmetic cosmetic;       // null for titles
        public final TitleRecord title;       // null for cosmetics
        public final CosmeticType category;
        public final String name;
        public final int defaultPrice;
        public final boolean isTitle;

        public ShopItemEntry(Cosmetic cosmetic, CosmeticType category, String name, int price) {
            this.cosmetic = cosmetic;
            this.title = null;
            this.category = category;
            this.name = name;
            this.defaultPrice = price;
            this.isTitle = false;
        }

        public ShopItemEntry(TitleRecord title) {
            this.cosmetic = null;
            this.title = title;
            this.category = CosmeticType.TITLE;
            this.name = title.getName();
            this.defaultPrice = 1000;
            this.isTitle = true;
        }
    }

    private static class ShopManageButton extends Button {
        private final ShopItemEntry entry;

        public ShopManageButton(ShopItemEntry entry) {
            this.entry = entry;
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            ShopDataManager shopData = AlleyPlugin.getInstance().getService(ShopDataManager.class);
            int price = shopData.getPrice(entry.category.name(), entry.name, entry.defaultPrice);
            boolean enabled = shopData.isEnabled(entry.category.name(), entry.name, true);

            Material icon;
            if (entry.isTitle) {
                icon = entry.title.getKit() != null ? entry.title.getKit().getIcon() : Material.NAME_TAG;
            } else {
                icon = entry.cosmetic.getIcon();
            }

            List<String> lore = new ArrayList<>();
            lore.add(CC.MENU_BAR);
            lore.add("&7Category: &f" + entry.category.name());
            lore.add("&7Price: &6$" + price);
            lore.add("&7Status: " + (enabled ? "&aEnabled" : "&cDisabled"));
            lore.add("");
            lore.add("&eLeft-Click &7to edit price/category.");
            lore.add("&eRight-Click &7to toggle enable/disable.");
            lore.add(CC.MENU_BAR);

            return new ItemBuilder(icon)
                    .name((enabled ? "&a" : "&c") + entry.name)
                    .lore(lore)
                    .glow(enabled)
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType == ClickType.LEFT) {
                new ShopItemEditMenu(entry).openMenu(player);
                this.playSuccess(player);
            } else if (clickType == ClickType.RIGHT) {
                ShopDataManager shopData = AlleyPlugin.getInstance().getService(ShopDataManager.class);
                boolean current = shopData.isEnabled(entry.category.name(), entry.name, true);
                shopData.setEnabled(entry.category.name(), entry.name, !current);
                player.sendMessage(CC.translate("&a" + entry.name + " is now " + (!current ? "&aenabled" : "&cdisabled") + "&a."));
                new ShopManagementMenu().openMenu(player);
                this.playNeutral(player);
            }
        }
    }
}
