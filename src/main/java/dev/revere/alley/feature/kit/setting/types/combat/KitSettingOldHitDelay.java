package dev.revere.alley.feature.kit.setting.types.combat;

import dev.revere.alley.feature.kit.setting.KitSetting;
import dev.revere.alley.feature.kit.setting.annotation.KitSettingData;
/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 04/07/2026
 *
 * Per-kit NMS hurt-resistance window for all damage and knockback sources.
 * A window of 20 admits the next full hit after approximately 10 server ticks.
 */
@KitSettingData(
        name = "oldHitDelay",
        description = "Uses the profile hit-delay window. 20 is about 10 server ticks.",
        enabled = true
)
public class KitSettingOldHitDelay extends KitSetting {
    public static final int DEFAULT_DELAY = 20;

    public KitSettingOldHitDelay() {
        super();
        setValue(DEFAULT_DELAY);
    }
}
