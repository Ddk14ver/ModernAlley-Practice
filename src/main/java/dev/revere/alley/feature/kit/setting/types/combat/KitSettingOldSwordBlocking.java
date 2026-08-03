package dev.revere.alley.feature.kit.setting.types.combat;

import dev.revere.alley.feature.kit.setting.KitSetting;
import dev.revere.alley.feature.kit.setting.annotation.KitSettingData;
/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 13/06/2026
 *
 * Enables 1.8-style sword blocking, instant attack speed, fishing rod/snowball/egg knockback.
 */
@KitSettingData(
        name = "oldSwordBlockKB",
        description = "Restores 1.8 sword blocking, attack speed, and projectile knockback",
        enabled = false
)
public class KitSettingOldSwordBlocking extends KitSetting {
}
