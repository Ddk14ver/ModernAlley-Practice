package dev.revere.alley.feature.event.skywars;

import dev.revere.alley.feature.arena.Arena;
import dev.revere.alley.feature.kit.Kit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Populates every chest in a dedicated SkyWars arena from an administrator-selected resource kit.
 * Bukkit block and inventory access in this class must run on the server thread.
 */
public final class SkyWarsLoot {
    public static final int MINIMUM_ITEMS_PER_CHEST = 7;
    public static final int MAXIMUM_ITEMS_PER_CHEST = 14;

    private SkyWarsLoot() {
    }

    public static boolean isUsableResourceKit(Kit kit) {
        return getResourceItems(kit).size() >= MINIMUM_ITEMS_PER_CHEST;
    }

    public static List<ItemStack> getResourceItems(Kit kit) {
        List<ItemStack> resourceItems = new ArrayList<>();
        if (kit == null || kit.getItems() == null) return resourceItems;

        for (ItemStack item : kit.getItems()) {
            if (item != null && item.getType() != Material.AIR && item.getAmount() > 0) {
                resourceItems.add(item.clone());
            }
        }
        return resourceItems;
    }

    public static int populateAllChests(Arena arena, Kit resourceKit) {
        if (arena == null || arena.getMinimum() == null || arena.getMaximum() == null) return 0;
        if (!isUsableResourceKit(resourceKit)) return 0;

        Location first = arena.getMinimum();
        Location second = arena.getMaximum();
        if (first.getWorld() == null || first.getWorld() != second.getWorld()) return 0;

        int minX = Math.min(first.getBlockX(), second.getBlockX());
        int maxX = Math.max(first.getBlockX(), second.getBlockX());
        int minY = Math.min(first.getBlockY(), second.getBlockY());
        int maxY = Math.max(first.getBlockY(), second.getBlockY());
        int minZ = Math.min(first.getBlockZ(), second.getBlockZ());
        int maxZ = Math.max(first.getBlockZ(), second.getBlockZ());

        Set<String> populatedChests = new HashSet<>();
        int chestCount = 0;
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = first.getWorld().getBlockAt(x, y, z);
                    if (!(block.getState() instanceof Chest chest)) continue;
                    if (!populatedChests.add(getChestKey(chest))) continue;

                    populateChest(chest, resourceKit);
                    chestCount++;
                }
            }
        }
        return chestCount;
    }

    public static int countChests(Arena arena) {
        if (arena == null || arena.getMinimum() == null || arena.getMaximum() == null) return 0;
        Location first = arena.getMinimum();
        Location second = arena.getMaximum();
        if (first.getWorld() == null || first.getWorld() != second.getWorld()) return 0;

        int minX = Math.min(first.getBlockX(), second.getBlockX());
        int maxX = Math.max(first.getBlockX(), second.getBlockX());
        int minY = Math.min(first.getBlockY(), second.getBlockY());
        int maxY = Math.max(first.getBlockY(), second.getBlockY());
        int minZ = Math.min(first.getBlockZ(), second.getBlockZ());
        int maxZ = Math.max(first.getBlockZ(), second.getBlockZ());

        Set<String> chests = new HashSet<>();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = first.getWorld().getBlockAt(x, y, z);
                    if (block.getState() instanceof Chest chest) {
                        chests.add(getChestKey(chest));
                    }
                }
            }
        }
        return chests.size();
    }

    private static void populateChest(Chest chest, Kit resourceKit) {
        Inventory inventory = chest.getInventory();
        chest.setLootTable(null);
        inventory.clear();

        List<ItemStack> contents = selectContents(resourceKit);
        List<Integer> slots = new ArrayList<>();
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            slots.add(slot);
        }
        Collections.shuffle(slots);

        for (int index = 0; index < contents.size(); index++) {
            inventory.setItem(slots.get(index), contents.get(index));
        }
        chest.update(true, false);
    }

    private static List<ItemStack> selectContents(Kit resourceKit) {
        List<ItemStack> candidates = getResourceItems(resourceKit);
        List<ItemStack> selected = new ArrayList<>();
        List<ItemStack> rejected = new ArrayList<>();

        for (ItemStack candidate : candidates) {
            if (ThreadLocalRandom.current().nextBoolean()) {
                selected.add(candidate);
            } else {
                rejected.add(candidate);
            }
        }

        int maximum = Math.min(MAXIMUM_ITEMS_PER_CHEST, candidates.size());
        if (selected.size() < MINIMUM_ITEMS_PER_CHEST) {
            Collections.shuffle(rejected);
            while (selected.size() < MINIMUM_ITEMS_PER_CHEST && !rejected.isEmpty()) {
                selected.add(rejected.remove(rejected.size() - 1));
            }
        }
        if (selected.size() > maximum) {
            Collections.shuffle(selected);
            selected = new ArrayList<>(selected.subList(0, maximum));
        }

        Collections.shuffle(selected);
        return selected;
    }

    private static String getChestKey(Chest chest) {
        InventoryHolder holder = chest.getInventory().getHolder();
        if (holder instanceof DoubleChest doubleChest
                && doubleChest.getLeftSide() instanceof Chest left
                && doubleChest.getRightSide() instanceof Chest right) {
            String leftKey = getLocationKey(left.getLocation());
            String rightKey = getLocationKey(right.getLocation());
            return leftKey.compareTo(rightKey) <= 0 ? leftKey + ':' + rightKey : rightKey + ':' + leftKey;
        }
        return getLocationKey(chest.getLocation());
    }

    private static String getLocationKey(Location location) {
        return location.getWorld().getUID() + ":" + location.getBlockX() + ':'
                + location.getBlockY() + ':' + location.getBlockZ();
    }
}
