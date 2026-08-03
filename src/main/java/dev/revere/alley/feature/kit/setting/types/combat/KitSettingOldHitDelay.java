package dev.revere.alley.feature.kit.setting.types.combat;

import dev.revere.alley.feature.kit.setting.KitSetting;
import dev.revere.alley.feature.kit.setting.annotation.KitSettingData;
/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 04/07/2026
 *
 * Per-kit hit delay in ticks for all damage and knockback sources.
 * 14 ticks ≈ 0.7s (1.8 default).
 */
@KitSettingData(
        name = "oldHitDelay",
        description = "Sets this kit's hit delay in ticks. 0 disables hurt frames.",
        enabled = true
)
public class KitSettingOldHitDelay extends KitSetting {
    public static final int DEFAULT_DELAY = 10;

    public KitSettingOldHitDelay() {
        super();
        setValue(DEFAULT_DELAY);
    }
}
