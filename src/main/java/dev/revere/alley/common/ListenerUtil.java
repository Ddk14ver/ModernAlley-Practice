package dev.revere.alley.common;

import dev.revere.alley.AlleyPlugin;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Emmy
 * @project Alley
 * @since 08/02/2025
 */
@UtilityClass
public class ListenerUtil {
    /**
     * After 5 seconds, clears the dropped items on death via a BukkitRunnable.
     * 5秒后，通过BukkitRunnable清除死亡时掉落的物品。
     *
     * @param event      The event.
     *                   事件。
     * @param deadPlayer The dead player.
     *                   死亡的玩家。
     */
    public void clearDroppedItemsOnDeath(PlayerDeathEvent event, Player deadPlayer) {
        List<Item> droppedItems = new ArrayList<>();
        for (ItemStack drop : event.getDrops()) {
            if (drop != null && drop.getType() != Material.AIR) {
                droppedItems.add(deadPlayer.getWorld().dropItemNaturally(deadPlayer.getLocation(), drop));
            }
        }
        event.getDrops().clear();

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Item item : droppedItems) {
                    if (item != null && item.isValid()) {
                        item.remove();
                    }
                }
            }
        }.runTaskLater(AlleyPlugin.getInstance(), 100L);
    }

    /**
     * After 5 seconds, clears the dropped items on regular item drop via a BukkitRunnable.
     * 5秒后，通过BukkitRunnable清除常规掉落的物品。
     *
     * @param item The dropped item.
     *             掉落的物品。
     */
    public void clearDroppedItemsOnRegularItemDrop(Item item) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (item != null && item.isValid()) {
                    item.remove();
                }
            }
        }.runTaskLater(AlleyPlugin.getInstance(), 100L);
    }

    /**
     * Checks if the player is not stepping on a pressure plate.
     * 检查玩家是否没有踩在压力板上。
     *
     * @param block The block you are standing on.
     *              你站立在其上的方块。
     * @return true if the player is stepping on a pressure plate, false otherwise.
     *         如果玩家踩在压力板上返回true，否则返回false。
     */
    public boolean notSteppingOnPlate(Block block) {
        if (block == null) {
            return false;
        }

        Material type = block.getType();
        return !pressurePlates.contains(type);
    }

    public boolean checkSteppingOnGoldPressurePlate(Block block) {
        if (block == null) {
            return false;
        }

        Material type = block.getType();
        return type == Material.LIGHT_WEIGHTED_PRESSURE_PLATE;
    }

    public boolean checkSteppingOnIronPressurePlate(Block block) {
        if (block == null) {
            return false;
        }

        Material type = block.getType();
        return type == Material.HEAVY_WEIGHTED_PRESSURE_PLATE;
    }

    public void teleportAndClearSpawn(Player player, Location spawnLocation) {
        for (int i = 0; i <= 2; i++) {
            Block block = spawnLocation.clone().add(0, i, 0).getBlock();
            if (block.getType() != Material.AIR) {
                block.setType(Material.AIR);
            }
        }
        player.teleportAsync(spawnLocation);
    }

    /**
     * List of pressure plate materials.
     * 压力板材质列表。
     */
    private final List<Material> pressurePlates = Arrays.asList(
            Material.OAK_PRESSURE_PLATE,
            Material.STONE_PRESSURE_PLATE,
            Material.HEAVY_WEIGHTED_PRESSURE_PLATE,
            Material.LIGHT_WEIGHTED_PRESSURE_PLATE
    );

    /**
     * Checks if the material is a door or gate.
     * 检查材质是否为门或栅栏门。
     *
     * @param material The material to check.
     *                 要检查的材质。
     * @return true if the material is a door or gate, false otherwise.
     *         如果材质是门或栅栏门返回true，否则返回false。
     */
    public boolean isInteractiveBlock(Material material) {
        return interactiveBlocks.contains(material);
    }

    /**
     * List of door and gate materials.
     * 门和栅栏门材质列表。
     */
    private final List<Material> interactiveBlocks = Arrays.asList(
            Material.OAK_DOOR,
            Material.SPRUCE_DOOR,
            Material.BIRCH_DOOR,
            Material.JUNGLE_DOOR,
            Material.ACACIA_DOOR,
            Material.DARK_OAK_DOOR,

            Material.OAK_FENCE_GATE,
            Material.SPRUCE_FENCE_GATE,
            Material.BIRCH_FENCE_GATE,
            Material.JUNGLE_FENCE_GATE,
            Material.ACACIA_FENCE_GATE,
            Material.DARK_OAK_FENCE_GATE,

            Material.OAK_TRAPDOOR,
            Material.IRON_TRAPDOOR,

            Material.CHEST,
            Material.ENDER_CHEST,
            Material.TRAPPED_CHEST,

            Material.HOPPER,
            Material.HOPPER_MINECART
    );

    /**
     * Checks if the material is a bed fight protected block.
     * 检查材质是否为床战保护方块。
     *
     * @param material The material to check.
     *                 要检查的材质。
     * @return true if the material is a bed fight protected block, false otherwise.
     *         如果材质是床战保护方块返回true，否则返回false。
     */
    public boolean isBedFightProtectedBlock(Material material) {
        return bedFightProtectedBlocks.contains(material)
            || material.name().endsWith("_WOOL")
            || material.name().endsWith("_BED");
    }

    /**
     * List of bed fight protected block materials.
     * 床战保护方块材质列表。
     */
    private final List<Material> bedFightProtectedBlocks = Arrays.asList(
            Material.END_STONE,
            Material.OAK_PLANKS
    );

    /**
     * Checks if the material is a bed fight protected block.
     * 检查材质是否为床战保护方块。
     *
     * @param material The material to check.
     *                 要检查的材质。
     * @return true if the material is a bed fight protected block, false otherwise.
     *         如果材质是床战保护方块返回true，否则返回false。
     */
    public boolean isSword(Material material) {
        return swords.contains(material);
    }

    /**
     * List of bed fight protected block materials.
     * 床战保护方块材质列表。
     */
    private final List<Material> swords = Arrays.asList(
            Material.DIAMOND_SWORD,
            Material.GOLDEN_SWORD,
            Material.IRON_SWORD,
            Material.STONE_SWORD,
            Material.WOODEN_SWORD,
            Material.NETHERITE_SWORD
    );
}
