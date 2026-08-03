package dev.revere.alley.feature.layout.menu.button.editor;

import dev.revere.alley.common.text.CC;
import dev.revere.alley.library.menu.Button;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * Adds a configured optional item to the layout currently being edited.
 */
@AllArgsConstructor
public class LayoutEditorExtraItemButton extends Button {
    private final ItemStack item;

    @Override
    public ItemStack getButtonItem(Player player) {
        return this.item.clone();
    }

    @Override
    public void clicked(Player player, ClickType clickType) {
        if (clickType != ClickType.LEFT && clickType != ClickType.RIGHT) {
            return;
        }

        Map<Integer, ItemStack> leftover = player.getInventory().addItem(this.item.clone());
        if (!leftover.isEmpty()) {
            player.sendMessage(CC.translate("&cInventory full!"));
        }
    }
}
