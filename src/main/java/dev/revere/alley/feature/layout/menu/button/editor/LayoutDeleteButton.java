package dev.revere.alley.feature.layout.menu.button.editor;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.item.ItemBuilder;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.feature.layout.data.LayoutData;
import dev.revere.alley.library.menu.Button;
import lombok.AllArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * @author Ddk1 dsv4pro
 * @project Alley
 * @since 13/07/2026
 */

@AllArgsConstructor
public class LayoutDeleteButton extends Button {
    private final LayoutData layout;

    @Override
    public ItemStack getButtonItem(Player player) {
        return new ItemBuilder(Material.RED_DYE)
                .name("&c&lDelete Layout")
                .lore(CC.MENU_BAR,
                        "&7Warning: Permanent!",
                        "",
                        "&aClick to delete.",
                        CC.MENU_BAR)
                .hideMeta().build();
    }

    @Override
    public void clicked(Player player, ClickType clickType) {
        if (clickType != ClickType.LEFT) return;

        // Find which kit this layout belongs to
        Profile profile = AlleyPlugin.getInstance().getService(ProfileService.class).getProfile(player.getUniqueId());
        String kitName = null;
        for (var entry : profile.getProfileData().getLayoutData().getLayouts().entrySet()) {
            if (entry.getValue().contains(this.layout)) {
                kitName = entry.getKey();
                break;
            }
        }

        if (kitName != null) {
            List<LayoutData> layouts = profile.getProfileData().getLayoutData().getLayouts().get(kitName);
            layouts.remove(this.layout);
            player.sendMessage(CC.translate("&cLayout &e" + this.layout.getDisplayName() + " &cdeleted."));
        }

        player.closeInventory();
    }
}
