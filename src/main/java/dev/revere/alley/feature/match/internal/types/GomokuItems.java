package dev.revere.alley.feature.match.internal.types;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.item.ItemBuilder;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;

public final class GomokuItems {
    private static final NamespacedKey SURRENDER_POTION =
            new NamespacedKey(AlleyPlugin.getInstance(), "gomoku_surrender_potion");

    private GomokuItems() {
    }

    public static ItemStack createSurrenderPotion() {
        ItemStack item = new ItemBuilder(Material.POTION)
                .name("&c&lSurrender Potion")
                .lore("&7Drink to surrender immediately.")
                .build();
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        meta.setColor(Color.RED);
        meta.getPersistentDataContainer().set(SURRENDER_POTION, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isSurrenderPotion(ItemStack item) {
        if (item == null || item.getType() != Material.POTION || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer()
                .has(SURRENDER_POTION, PersistentDataType.BYTE);
    }
}
