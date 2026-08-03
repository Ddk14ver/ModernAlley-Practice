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
        name = "Odie",
        description = "Become Odie, the lovable dog from Garfield.",
        icon = Material.BONE,
        slot = 15,
        price = 850
)
public class OdieSuit extends BaseSuit {
    @Override
    public Map<EquipmentSlot, ItemStack> getArmorPieces() {
        Map<EquipmentSlot, ItemStack> armorPieces = new HashMap<>();

        ItemStack odieHead = new ItemBuilder(Material.PLAYER_HEAD)
                .durability(3)
                .setSkullTexture(TexturesConstant.ODIE_SKIN)
                .build();
        armorPieces.put(EquipmentSlot.HEAD, odieHead);

        Color yellow = Color.fromRGB(255, 255, 0);
        armorPieces.put(EquipmentSlot.CHEST, createColoredArmor(Material.LEATHER_CHESTPLATE, yellow));
        armorPieces.put(EquipmentSlot.LEGS, createColoredArmor(Material.LEATHER_LEGGINGS, yellow));
        armorPieces.put(EquipmentSlot.FEET, createColoredArmor(Material.LEATHER_BOOTS, yellow));

        return armorPieces;
    }

    @Override
    public Map<PotionEffectType, Integer> getPassiveEffects() {
        Map<PotionEffectType, Integer> effects = new HashMap<>();
        effects.put(PotionEffectType.SPEED, 1);
        effects.put(PotionEffectType.JUMP_BOOST, 1);
        return effects;
    }
}
