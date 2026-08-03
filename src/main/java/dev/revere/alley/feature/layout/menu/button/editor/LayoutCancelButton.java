package dev.revere.alley.feature.layout.menu.button.editor;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.library.menu.Button;
import dev.revere.alley.feature.layout.LayoutService;
import dev.revere.alley.common.item.ItemBuilder;
import dev.revere.alley.common.text.CC;
import lombok.AllArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

/**
 * Button to cancel layout editing and return to the main menu.
 * 取消布局编辑并返回主菜单的按钮。
 * @author Emmy
 * @project Alley
 * @since 03/05/2025
 */
@AllArgsConstructor
public class LayoutCancelButton extends Button {
    @Override
    public ItemStack getButtonItem(Player player) {
        return new ItemBuilder(Material.RED_WOOL)
                .name("&6&lCancel")
                .lore(
                        CC.MENU_BAR,
                        "&7Cancel changes &",
                        "&7return to main menu.",
                        "",
                        "&aClick to cancel.",
                        CC.MENU_BAR
                )
                .hideMeta()
                .build();
    }

    @Override
    public void clicked(Player player, ClickType clickType) {
        if (clickType != ClickType.LEFT) return;

        AlleyPlugin.getInstance().getService(LayoutService.class).getLayoutMenu().openMenu(player);
    }
}