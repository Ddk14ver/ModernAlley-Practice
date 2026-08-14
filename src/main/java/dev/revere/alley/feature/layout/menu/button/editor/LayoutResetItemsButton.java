package dev.revere.alley.feature.layout.menu.button.editor;

import dev.revere.alley.library.menu.Button;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.kit.setting.types.combat.KitSettingOldOffhand;
import dev.revere.alley.common.item.ItemBuilder;
import dev.revere.alley.common.text.CC;
import lombok.AllArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

/**
 * Button to reset inventory items to the default kit items.
 * 将背包物品重置为默认套件物品的按钮。
 * @author Emmy
 * @project Alley
 * @since 03/05/2025
 */
@AllArgsConstructor
public class LayoutResetItemsButton extends Button {
    private final Kit kit;

    @Override
    public ItemStack getButtonItem(Player player) {
        return new ItemBuilder(Material.YELLOW_WOOL)
                .name("&6&lReset Items")
                .lore(
                        CC.MENU_BAR,
                        "&7Reset items to default.",
                        "",
                        "&aClick to reset.",
                        CC.MENU_BAR
                )
                .hideMeta()
                .build();
    }

    @Override
    public void clicked(Player player, ClickType clickType) {
        if (clickType != ClickType.LEFT) return;

        player.getInventory().clear();
        player.getInventory().setContents(this.kit.getItems());
        if (!this.kit.isSettingEnabled(KitSettingOldOffhand.class)) {
            player.getInventory().setItemInOffHand(this.kit.getOffhand());
            player.getOpenInventory().getTopInventory().setItem(35, this.kit.getOffhand());
        }
        player.updateInventory();
    }
}