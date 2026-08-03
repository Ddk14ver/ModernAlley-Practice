package dev.revere.alley.feature.kit.setting.types.combat;

import dev.revere.alley.feature.kit.setting.KitSetting;
import dev.revere.alley.feature.kit.setting.annotation.KitSettingData;
/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 13/06/2026
 * Restores 1.8 sharpness/protection enchantment formulas and removes armor toughness.
 */
@KitSettingData(
        name = "oldEnchantsArmor",
        description = "Restores 1.8 enchantment formulas and removes armor toughness",
        enabled = false
)
public class KitSettingOldEnchantments extends KitSetting {
}
