package dev.revere.alley.feature.arena.selection;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.item.ItemBuilder;
import lombok.Data;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

/**
 * @author Remi
 * @project Alley
 * @date 5/20/2024
 */
@Data
@Getter
public class ArenaSelection {
    public static final ItemStack SELECTION_TOOL = new ItemBuilder(Material.FEATHER)
            .name("&6Selection Tool")
            .lore("&7Use this tool to select the minimum and maximum locations.")
            .build();

    private static final String SELECTION_METADATA = "alley.selection";

    private Location minimum;
    private Location maximum;

    public ArenaSelection() {

    }

    /**
     * Creates a selection based on the player.
     * 基于玩家创建选区。
     *
     * @param player The player to create the selection for.
     *               要为其创建选区的玩家。
     * @return The selection created.
     *         创建的选区。
     */
    public static ArenaSelection createSelection(Player player) {
        if (player.hasMetadata(SELECTION_METADATA)) {
            return (ArenaSelection) player.getMetadata(SELECTION_METADATA).get(0).value();
        }

        ArenaSelection arenaSelection = new ArenaSelection();

        player.setMetadata(SELECTION_METADATA, new FixedMetadataValue(AlleyPlugin.getInstance(), arenaSelection));
        return arenaSelection;
    }

    /**
     * Gets the selection of the player.
     * 获取玩家的选区。
     *
     * @param player The player to get the selection of.
     *               要获取选区的玩家。
     */
    public static void removeSelection(Player player) {
        if (player.hasMetadata(SELECTION_METADATA)) {
            player.removeMetadata(SELECTION_METADATA, AlleyPlugin.getInstance());
        }
    }

    /**
     * Checks if the player has a selection.
     * 检查玩家是否有选区。
     *
     * @return True if the player has a selection, false otherwise.
     *         如果玩家有选区则返回 true，否则返回 false。
     */
    public boolean hasSelection() {
        return minimum != null && maximum != null;
    }
}