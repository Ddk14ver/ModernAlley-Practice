package dev.revere.alley.feature.cosmetic.internal.repository.impl.suit;

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
 * @project alley-practice
 * @date 4/08/2025
 */
@CosmeticData(
        type = CosmeticType.SUIT,
        name = "Spaceman",
        description = "Wear a cool spacesuit to explore the universe.",
        icon = Material.GLASS,
        slot = 11
)
public class SpacemanSuit extends BaseSuit {
    @Override
    public Map<EquipmentSlot, ItemStack> getArmorPieces() {
        Map<EquipmentSlot, ItemStack> armorPieces = new HashMap<>();

        armorPieces.put(EquipmentSlot.HEAD, new ItemStack(Material.GLASS));
        armorPieces.put(EquipmentSlot.CHEST, createColoredArmor(Material.LEATHER_CHESTPLATE, Color.WHITE));
        armorPieces.put(EquipmentSlot.LEGS, createColoredArmor(Material.LEATHER_LEGGINGS, Color.WHITE));
        armorPieces.put(EquipmentSlot.FEET, createColoredArmor(Material.LEATHER_BOOTS, Color.WHITE));

        return armorPieces;
    }

    @Override
    public Map<PotionEffectType, Integer> getPassiveEffects() {
        Map<PotionEffectType, Integer> effects = new HashMap<>();
        effects.put(PotionEffectType.SPEED, 2);
        effects.put(PotionEffectType.JUMP_BOOST, 2);
        return effects;
    }
}
