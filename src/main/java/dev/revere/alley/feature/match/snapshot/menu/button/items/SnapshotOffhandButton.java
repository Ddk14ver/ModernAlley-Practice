package dev.revere.alley.feature.match.snapshot.menu.button.items;

import dev.revere.alley.common.item.ItemBuilder;
import dev.revere.alley.library.menu.Button;
import dev.revere.alley.feature.match.snapshot.Snapshot;
import lombok.AllArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 16/08/2026
 * Displays the player's offhand item in the post-match snapshot.
 * 在赛后快照中显示玩家的副手物品。
 */
@AllArgsConstructor
public class SnapshotOffhandButton extends Button {
    private final Snapshot snapshot;

    @Override
    public ItemStack getButtonItem(Player player) {
        ItemStack offhand = this.snapshot.getOffhand();
        if (offhand == null || offhand.getType() == Material.AIR) {
            return new ItemBuilder(Material.BARRIER)
                    .name("&7Offhand")
                    .lore("&7Empty")
                    .hideMeta()
                    .build();
        }
        return offhand.clone();
    }
}
