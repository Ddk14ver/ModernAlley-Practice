package dev.revere.alley.feature.kit.setting.types.mode;

import dev.revere.alley.feature.kit.setting.KitSetting;
import dev.revere.alley.feature.kit.setting.annotation.KitSettingData;

@KitSettingData(
        name = "BotQueue",
        description = "Allows this kit to be played against configured bots.",
        enabled = false
)
public class KitSettingBotQueue extends KitSetting {
}
