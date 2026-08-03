package dev.revere.alley.common;

import lombok.experimental.UtilityClass;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.List;

/**
 * @author Emmy
 * @project Alley
 * @date 03/11/2024 - 20:56
 */
@UtilityClass
public class PotionUtil {
    /**
     * Get the potion effect type of item stack.
     * 获取物品堆的药水效果类型。
     *
     * @param item The item stack.
     *             物品堆。
     * @return The potion effect type.
     *         药水效果类型。
     */
    public PotionEffectType getPotionEffectType(ItemStack item) {
        return getPotionEffects(item).stream().findFirst().map(PotionEffect::getType).orElse(null);
    }

    /**
     * Get the potion effect amplifier of an item stack.
     * 获取物品堆的药水效果等级。
     *
     * @param item The item stack.
     *             物品堆。
     * @return The potion effect amplifier.
     *         药水效果等级。
     */
    public int getPotionEffectAmplifier(ItemStack item) {
        return getPotionEffects(item).stream().findFirst().map(PotionEffect::getAmplifier).orElse(0);
    }

    public List<PotionEffect> getPotionEffects(ItemStack item) {
        if (item == null || !(item.getItemMeta() instanceof PotionMeta potionMeta)) {
            return List.of();
        }
        return potionMeta.getAllEffects();
    }

    /**
     * Creates a Splash Potion of Instant Health II (replaces old 1.8 data value 16421).
     * 创建一个喷溅型瞬间治疗 II 药水（替代旧的 1.8 数据值 16421）。
     *
     * @return A Splash Potion of Instant Health II.
     *         喷溅型瞬间治疗 II 药水。
     */
    public ItemStack createSplashHealthPotion() {
        ItemStack potion = new ItemStack(Material.SPLASH_POTION);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        meta.setBasePotionType(PotionType.STRONG_HEALING);
        potion.setItemMeta(meta);
        return potion;
    }

    /**
     * Checks if an ItemStack is a splash potion of healing.
     * 检查物品堆是否是喷溅型治疗药水。
     *
     * @param item The item to check.
     *             要检查的物品。
     * @return True if the item is a splash potion of healing.
     *         如果物品是喷溅型治疗药水则返回 true。
     */
    public boolean isSplashHealthPotion(ItemStack item) {
        if (item == null || item.getType() != Material.SPLASH_POTION) {
            return false;
        }
        if (item.getItemMeta() instanceof PotionMeta meta) {
            return meta.getBasePotionType() == PotionType.STRONG_HEALING
                    || meta.getBasePotionType() == PotionType.HEALING;
        }
        return false;
    }
}
