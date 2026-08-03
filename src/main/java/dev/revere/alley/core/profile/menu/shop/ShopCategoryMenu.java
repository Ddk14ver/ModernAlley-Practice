package dev.revere.alley.core.profile.menu.shop;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.item.ItemBuilder;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.common.text.StringUtil;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.core.profile.menu.shop.button.ShopItemButton;
import dev.revere.alley.feature.cosmetic.CosmeticService;
import dev.revere.alley.feature.cosmetic.internal.repository.BaseCosmeticRepository;
import dev.revere.alley.feature.cosmetic.model.BaseCosmetic;
import dev.revere.alley.feature.cosmetic.model.CosmeticType;
import dev.revere.alley.feature.shop.ShopDataManager;
import dev.revere.alley.feature.title.TitleService;
import dev.revere.alley.feature.title.internal.TitleServiceImpl;
import dev.revere.alley.feature.title.model.TitleRecord;
import dev.revere.alley.library.menu.Button;
import dev.revere.alley.library.menu.Menu;
import dev.revere.alley.library.menu.impl.BackButton;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 23/06/2026
 */

@RequiredArgsConstructor
public class ShopCategoryMenu extends Menu {

    private final CosmeticType cosmeticType;

    @Override
    public String getTitle(Player player) {
        return "&6&lShop - " + StringUtil.formatEnumName(cosmeticType) + "s";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        buttons.put(0, new BackButton(new ShopMenu()));

        if (cosmeticType == CosmeticType.TITLE) {
            loadTitleItems(buttons, player);
        } else {
            BaseCosmeticRepository<?> repository = AlleyPlugin.getInstance().getService(CosmeticService.class).getRepository(cosmeticType);
            if (repository != null) {
                ShopDataManager shopData = AlleyPlugin.getInstance().getService(ShopDataManager.class);
                repository.getCosmetics().stream()
                        .filter(cosmetic -> cosmetic.getIcon() != null && cosmetic.getPrice() >= 0)
                        .filter(cosmetic -> shopData.isEnabled(cosmeticType.name(), cosmetic.getName(), true))
                        .forEach(cosmetic -> buttons.put(cosmetic.getSlot(), new ShopItemButton(cosmetic)));
            }
        }

        this.addBorder(buttons, Material.BLACK_STAINED_GLASS_PANE, 5);

        return buttons;
    }

    /**
     * Loads purchasable titles as shop items.
     */
    private void loadTitleItems(Map<Integer, Button> buttons, Player player) {
        TitleServiceImpl titleService = (TitleServiceImpl) AlleyPlugin.getInstance().getService(TitleService.class);
        ShopDataManager shopData = AlleyPlugin.getInstance().getService(ShopDataManager.class);
        Profile profile = AlleyPlugin.getInstance().getService(ProfileService.class).getProfile(player.getUniqueId());

        int slot = 10;
        for (TitleRecord title : titleService.getSortedTitles()) {
            if (!title.isPurchasable()) continue;

            int price = shopData.getPrice("TITLE", title.getName(), 1000);
            boolean enabled = shopData.isEnabled("TITLE", title.getName(), true);

            if (!enabled) continue;

            buttons.put(slot++, new TitleShopButton(profile, title, price));
            if (slot == 17 || slot == 26 || slot == 35 || slot == 44) slot += 2;
        }
    }

    /**
     * Purchase button for title items in the shop.
     */
    private static class TitleShopButton extends Button {
        private final Profile profile;
        private final TitleRecord title;
        private final int price;

        public TitleShopButton(Profile profile, TitleRecord title, int price) {
            this.profile = profile;
            this.title = title;
            this.price = price;
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            boolean owned = this.profile.getProfileData().getUnlockedTitles().contains(this.title.getName());

            Material icon = this.title.getKit() != null ? this.title.getKit().getIcon() : Material.NAME_TAG;

            return new ItemBuilder(icon)
                    .name(this.title.getName())
                    .lore(
                            CC.MENU_BAR,
                            "&7" + this.title.getPrefix(),
                            "",
                            owned ? "&a&lOWNED" : "&7Price: &6$" + this.price,
                            "",
                            owned ? "&7You already own this title." : "&aClick to purchase!",
                            CC.MENU_BAR
                    )
                    .durability(this.title.getKit() != null ? this.title.getKit().getDurability() : 0)
                    .glow(owned)
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType != ClickType.LEFT) return;

            if (this.profile.getProfileData().getUnlockedTitles().contains(this.title.getName())) {
                player.sendMessage(CC.translate("&cYou already own this title."));
                this.playFail(player);
                return;
            }

            if (this.profile.getProfileData().getCoins() < this.price) {
                player.sendMessage(CC.translate("&cYou don't have enough coins! You need &6$" + this.price + "&c."));
                this.playFail(player);
                return;
            }

            this.profile.getProfileData().setCoins(this.profile.getProfileData().getCoins() - this.price);
            this.profile.getProfileData().getUnlockedTitles().add(this.title.getName());
            this.profile.save();

            player.sendMessage(CC.translate("&aYou purchased the &6" + this.title.getName() + " &atitle for &6$" + this.price + "&a!"));
            new ShopCategoryMenu(CosmeticType.TITLE).openMenu(player);
            this.playSuccess(player);
        }
    }
}
