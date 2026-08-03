package dev.revere.alley.common.item;

import lombok.experimental.UtilityClass;
import org.bukkit.enchantments.Enchantment;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Emmy
 * @project Alley
 * @date 28/05/2024 - 20:53
 */
@UtilityClass
public class EnchantUtil {
    private final Map<String, Enchantment> enchantment = new HashMap<>();

    static {
        enchantment.put("sharpness", Enchantment.SHARPNESS);
        enchantment.put("efficiency", Enchantment.EFFICIENCY);
        enchantment.put("unbreaking", Enchantment.UNBREAKING);
        enchantment.put("fortune", Enchantment.FORTUNE);
        enchantment.put("power", Enchantment.POWER);
        enchantment.put("punch", Enchantment.PUNCH);
        enchantment.put("flame", Enchantment.FLAME);
        enchantment.put("infinity", Enchantment.INFINITY);
        enchantment.put("knockback", Enchantment.KNOCKBACK);
        enchantment.put("protection", Enchantment.PROTECTION);
        enchantment.put("fire_protection", Enchantment.FIRE_PROTECTION);
        enchantment.put("blast_protection", Enchantment.BLAST_PROTECTION);
        enchantment.put("projectile_protection", Enchantment.PROJECTILE_PROTECTION);
        enchantment.put("thorns", Enchantment.THORNS);
        enchantment.put("respiration", Enchantment.RESPIRATION);
        enchantment.put("aqua_affinity", Enchantment.AQUA_AFFINITY);
        enchantment.put("depth_strider", Enchantment.DEPTH_STRIDER);
        enchantment.put("smite", Enchantment.SMITE);
        enchantment.put("bane_of_arthropods", Enchantment.BANE_OF_ARTHROPODS);
        enchantment.put("fire_aspect", Enchantment.FIRE_ASPECT);
        enchantment.put("looting", Enchantment.LOOTING);
        enchantment.put("silk_touch", Enchantment.SILK_TOUCH);
        enchantment.put("luck_of_the_sea", Enchantment.LUCK_OF_THE_SEA);
        enchantment.put("lure", Enchantment.LURE);
    }

    /**
     * Get an enchantment by name.
     * 根据名称获取附魔。
     *
     * @param name The name of the enchantment.
     *             附魔的名称。
     * @return The enchantment.
     *         附魔。
     */
    public Enchantment getEnchantment(String name) {
        return enchantment.get(name);
    }

    /**
     * Get a list of all enchantments.
     * 获取所有附魔的列表。
     *
     * @return The list of enchantments.
     *         附魔列表。
     */
    public String getSortedEnchantments() {
        return String.join(", ", enchantment.keySet());
    }
}