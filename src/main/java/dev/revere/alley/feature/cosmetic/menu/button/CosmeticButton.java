package dev.revere.alley.feature.cosmetic.menu.button;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.item.ItemBuilder;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.locale.LocaleService;
import dev.revere.alley.core.locale.internal.impl.message.GlobalMessagesLocaleImpl;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.core.profile.data.types.ProfileCosmeticData;
import dev.revere.alley.feature.cosmetic.CosmeticService;
import dev.revere.alley.feature.cosmetic.model.BaseCosmetic;
import dev.revere.alley.library.menu.Button;
import dev.revere.alley.library.menu.Menu;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Remi
 * @project Alley
 * @date 6/23/2025
 */
@AllArgsConstructor
public class CosmeticButton extends Button {
    protected final AlleyPlugin plugin = AlleyPlugin.getInstance();
    private final BaseCosmetic cosmetic;

    @Override
    public ItemStack getButtonItem(Player player) {
        ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);
        Profile profile = profileService.getProfile(player.getUniqueId());
        boolean isSelected = profile.getProfileData().getCosmeticData().isSelected(cosmetic);
        boolean hasPermission = cosmetic.getName().equalsIgnoreCase("None")
                || player.hasPermission(cosmetic.getPermission())
                || profile.getProfileData().getCosmeticData().isPurchased(cosmetic.getName());

        List<String> lore = new ArrayList<>();
        lore.add(CC.MENU_BAR);
        lore.addAll(cosmetic.getDisplayLore());
        lore.add("");
        if (hasPermission) {
            lore.add(isSelected ? "&eSelected." : "&aClick to select.");
        } else {
            lore.add("&cYou do not own this cosmetic.");
        }
        lore.add(CC.MENU_BAR);

        return new ItemBuilder(cosmetic.getIcon())
                .name("&6&l" + cosmetic.getName())
                .lore(lore)
                .glow(isSelected)
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

        if (clickType == ClickType.MIDDLE || clickType == ClickType.NUMBER_KEY || clickType == ClickType.DROP || clickType == ClickType.SHIFT_LEFT) {
            return;
        }

        LocaleService localeService = AlleyPlugin.getInstance().getService(LocaleService.class);
        ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);
        Profile profile = profileService.getProfile(player.getUniqueId());
        ProfileCosmeticData cosmeticData = profile.getProfileData().getCosmeticData();

        if (cosmeticData.isSelected(cosmetic)) {
            player.sendMessage(localeService.getString(GlobalMessagesLocaleImpl.COSMETIC_ALREADY_SELECTED).replace("{cosmetic-name}", cosmetic.getName()));
            this.playFail(player);
            return;
        }

        boolean hasPermission = cosmetic.getName().equalsIgnoreCase("None")
                || player.hasPermission(cosmetic.getPermission())
                || cosmeticData.isPurchased(cosmetic.getName());
        if (!hasPermission) {
            player.sendMessage(localeService.getString(GlobalMessagesLocaleImpl.COSMETIC_NOT_OWNED).replace("{cosmetic-name}", cosmetic.getName()));
            this.playFail(player);
            return;
        }

        cosmetic.getType().handleSelection(cosmetic, player);

        cosmeticData.setSelected(cosmetic);

        this.playSuccess(player);
        player.sendMessage(localeService.getString(GlobalMessagesLocaleImpl.COSMETIC_SELECTED).replace("{cosmetic-name}", cosmetic.getName()));
    }
}
