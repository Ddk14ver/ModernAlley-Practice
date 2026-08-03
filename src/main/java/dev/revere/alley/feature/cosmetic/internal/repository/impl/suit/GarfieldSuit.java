package dev.revere.alley.feature.cosmetic.internal.repository.impl.suit;

import dev.revere.alley.common.constants.TexturesConstant;
import dev.revere.alley.common.item.ItemBuilder;
import dev.revere.alley.feature.cosmetic.annotation.CosmeticData;
import dev.revere.alley.feature.cosmetic.model.CosmeticType;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Remi
 * 作者：Remi
 * @project alley-practice
 * 项目：alley-practice
 * @date 4/08/2025
 * 日期：2025年4月8日
 */
@CosmeticData(
        type = CosmeticType.SUIT,
        name = "Garfield",
        description = "I hate Mondays.",
        icon = Material.COD,
        slot = 14,
        price = 900
)
public class GarfieldSuit extends BaseSuit {
    @Override
    public Map<EquipmentSlot, ItemStack> getArmorPieces() {
        Map<EquipmentSlot, ItemStack> armorPieces = new HashMap<>();

        ItemStack garfieldHead = new ItemBuilder(Material.PLAYER_HEAD)
                .durability(3)
                .setSkullTexture(TexturesConstant.GARFIELD_SKIN)
                .build();
        armorPieces.put(EquipmentSlot.HEAD, garfieldHead);

        Color orange = Color.fromRGB(255, 140, 0);
        armorPieces.put(EquipmentSlot.CHEST, createColoredArmor(Material.LEATHER_CHESTPLATE, orange));
        armorPieces.put(EquipmentSlot.LEGS, createColoredArmor(Material.LEATHER_LEGGINGS, orange));
        armorPieces.put(EquipmentSlot.FEET, createColoredArmor(Material.LEATHER_BOOTS, orange));

        return armorPieces;
    }

    @Override
    public Map<PotionEffectType, Integer> getPassiveEffects() {
        Map<PotionEffectType, Integer> effects = new HashMap<>();
        effects.put(PotionEffectType.SLOWNESS, 0);
        return effects;
    }
}
