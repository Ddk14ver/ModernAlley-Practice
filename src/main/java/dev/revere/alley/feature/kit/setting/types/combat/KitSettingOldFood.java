package dev.revere.alley.feature.kit.setting.types.combat;

import dev.revere.alley.feature.kit.setting.KitSetting;
import dev.revere.alley.feature.kit.setting.annotation.KitSettingData;
/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 13/06/2026
 * Enables 1.8-style food consumption: instant heal/saturation, old golden apple effects.
 */
@KitSettingData(
        name = "oldFood",
        description = "Restores 1.8 food mechanics — instant eat heal and old gapple effects",
        enabled = false
)
public class KitSettingOldFood extends KitSetting {
}
