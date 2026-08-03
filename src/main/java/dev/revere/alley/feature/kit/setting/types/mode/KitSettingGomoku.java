package dev.revere.alley.feature.kit.setting.types.mode;

import dev.revere.alley.feature.kit.setting.KitSetting;
import dev.revere.alley.feature.kit.setting.annotation.KitSettingData;

@KitSettingData(
        name = "Gomoku",
        description = "Turns the kit into a turn-based five-in-a-row match.",
        enabled = false
)
public class KitSettingGomoku extends KitSetting {
}
