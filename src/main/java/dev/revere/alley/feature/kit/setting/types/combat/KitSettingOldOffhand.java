package dev.revere.alley.feature.kit.setting.types.combat;

import dev.revere.alley.feature.kit.setting.KitSetting;
import dev.revere.alley.feature.kit.setting.annotation.KitSettingData;
/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 13/06/2026
 *
 * Disables the offhand slot and restores 1.8-style attack sounds.
 */
@KitSettingData(
        name = "oldOffhandSounds",
        description = "Disables offhand usage and restores old hit sounds",
        enabled = false
)
public class KitSettingOldOffhand extends KitSetting {
}
