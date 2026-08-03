package dev.revere.alley.core.profile.menu.shop.button;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.item.ItemBuilder;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.locale.LocaleService;
import dev.revere.alley.core.locale.internal.impl.SettingsLocaleImpl;
import dev.revere.alley.core.locale.internal.impl.message.GlobalMessagesLocaleImpl;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.feature.cosmetic.CosmeticService;
import dev.revere.alley.feature.cosmetic.model.BaseCosmetic;
import dev.revere.alley.library.menu.Button;
import dev.revere.alley.library.menu.Menu;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Remi
 * @project Alley
 * @date 6/23/2025
 */
@AllArgsConstructor
public class ShopItemButton extends Button {
    private final BaseCosmetic cosmetic;
    private static final Map<UUID, PermissionAttachment> attachments = new ConcurrentHashMap<>();

    @Override
    public ItemStack getButtonItem(Player player) {
        Profile profile = AlleyPlugin.getInstance().getService(ProfileService.class).getProfile(player.getUniqueId());
        boolean hasPermission = cosmetic.getName().equalsIgnoreCase("None")
                || player.hasPermission(cosmetic.getPermission())
                || (profile != null && profile.getProfileData().getCosmeticData().isPurchased(cosmetic.getName()));

        List<String> lore = new ArrayList<>();
        lore.add(CC.MENU_BAR);
        lore.addAll(cosmetic.getDisplayLore());
        lore.add("");

        if (hasPermission) {
            lore.add("&aYou already own this item.");
        } else {
            lore.add(" &fPrice: &6$" + cosmetic.getPrice());
            lore.add("");
            lore.add("&aClick to purchase.");
        }
        lore.add(CC.MENU_BAR);

        return new ItemBuilder(cosmetic.getIcon())
                .name("&6&l" + cosmetic.getName())
                .lore(lore)
                .hideMeta()
                .build();
    }

    @Override
    public void clicked(Player player, ClickType clickType) {
        if (clickType == ClickType.RIGHT || clickType == ClickType.SHIFT_RIGHT) {
            CosmeticService service = AlleyPlugin.getInstance().getService(CosmeticService.class);
            service.getPreviewManager().preview(player, cosmetic, Menu.currentlyOpenedMenus.get(player.getName()));
            return;
        }
        if (clickType != ClickType.LEFT) return;

        Profile profile = AlleyPlugin.getInstance().getService(ProfileService.class).getProfile(player.getUniqueId());
        LocaleService localeService = AlleyPlugin.getInstance().getService(LocaleService.class);

        boolean alreadyOwned = cosmetic.getName().equalsIgnoreCase("None")
                || player.hasPermission(cosmetic.getPermission())
                || profile.getProfileData().getCosmeticData().isPurchased(cosmetic.getName());
        if (alreadyOwned) {
            player.sendMessage(localeService.getString(GlobalMessagesLocaleImpl.COSMETIC_ALREADY_OWNED));
            this.playFail(player);
            return;
        }

        if (profile.getProfileData().getCoins() < cosmetic.getPrice()) {
            player.sendMessage(localeService.getString(GlobalMessagesLocaleImpl.COSMETIC_PURCHASE_INSUFFICIENT_FUNDS));
            this.playFail(player);
            return;
        }

        profile.getProfileData().setCoins(profile.getProfileData().getCoins() - cosmetic.getPrice());

        // Grant permission via managed attachment
        PermissionAttachment attachment = attachments.computeIfAbsent(player.getUniqueId(),
                k -> player.addAttachment(AlleyPlugin.getInstance()));
        attachment.setPermission(cosmetic.getPermission(), true);

        // Save purchase to profile data for persistence across restarts
        profile.getProfileData().getCosmeticData().addPurchased(cosmetic.getName());

        // Also run external command for persistence
        String command = localeService.getString(SettingsLocaleImpl.GRANT_COSMETIC_PERMISSION_COMMAND);
        if (command != null && !command.isEmpty() && !command.equalsIgnoreCase("none")) {
            command = command.replace("{player}", player.getName())
                    .replace("{permission}", cosmetic.getPermission());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }

        profile.save();

        player.sendMessage(localeService.getString(GlobalMessagesLocaleImpl.COSMETIC_PURCHASE_SUCCESS).replace("{cosmetic}", cosmetic.getName()));
        this.playSuccess(player);
    }
}
