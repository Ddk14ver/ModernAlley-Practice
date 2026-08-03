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
        name = "Hulk",
        description = "HULK SMASH!",
        icon = Material.TNT,
        slot = 13,
        price = 1250
)
public class HulkSuit extends BaseSuit {
    @Override
    public Map<EquipmentSlot, ItemStack> getArmorPieces() {
        Map<EquipmentSlot, ItemStack> armorPieces = new HashMap<>();

        ItemStack hulkHead = new ItemBuilder(Material.PLAYER_HEAD)
                .durability(3)
                .setSkullTexture(TexturesConstant.HULK_SKIN)
                .build();
        armorPieces.put(EquipmentSlot.HEAD, hulkHead);
        armorPieces.put(EquipmentSlot.CHEST, createColoredArmor(Material.LEATHER_CHESTPLATE, Color.GREEN));
        armorPieces.put(EquipmentSlot.LEGS, createColoredArmor(Material.LEATHER_LEGGINGS, Color.PURPLE));
        armorPieces.put(EquipmentSlot.FEET, createColoredArmor(Material.LEATHER_BOOTS, Color.GREEN));

        return armorPieces;
    }

    @Override
    public Map<PotionEffectType, Integer> getPassiveEffects() {
        Map<PotionEffectType, Integer> effects = new HashMap<>();
        effects.put(PotionEffectType.JUMP_BOOST, 6);
        return effects;
    }
}
