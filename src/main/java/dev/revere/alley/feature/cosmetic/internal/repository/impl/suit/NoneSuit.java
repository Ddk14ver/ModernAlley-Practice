package dev.revere.alley.feature.cosmetic.internal.repository.impl.suit;

import dev.revere.alley.feature.cosmetic.annotation.CosmeticData;
import dev.revere.alley.feature.cosmetic.model.CosmeticType;
import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.Collections;
import java.util.Map;

/**
 * @author Remi
 * @project alley-practice
 * @date 4/08/2025
 */
@CosmeticData(
        type = CosmeticType.SUIT,
        name = "None",
        description = "Remove your suit",
        icon = Material.BARRIER,
        slot = 10
)
public class NoneSuit extends BaseSuit {
    @Override
    public Map<EquipmentSlot, ItemStack> getArmorPieces() {
        return Collections.emptyMap();
    }

    @Override
    public Map<PotionEffectType, Integer> getPassiveEffects() {
        return Collections.emptyMap();
    }
}
