package dev.revere.alley.feature.kit.setting.types.mechanic;

import dev.revere.alley.feature.kit.setting.KitSetting;
import dev.revere.alley.feature.kit.setting.annotation.KitSettingData;

/**
 * @author Remi
 * @project alley-practice
 * @date 13/08/2025
 */
@KitSettingData(name = "PearlCooldown", description = "Sets the ender pearl cooldown in seconds. 0 disables it.", enabled = true)
public class KitSettingPearlCooldownImpl extends KitSetting {
    public static final int DEFAULT_SECONDS = 15;

    public KitSettingPearlCooldownImpl() {
        super();
        setValue(DEFAULT_SECONDS);
    }

    @Override
    public void setValue(int value) {
        int seconds = Math.max(0, value);
        super.setValue(seconds);
        super.setEnabled(seconds > 0);
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (enabled && getValue() <= 0) {
            super.setValue(DEFAULT_SECONDS);
        } else if (!enabled) {
            super.setValue(0);
        }
        super.setEnabled(enabled);
    }
}
