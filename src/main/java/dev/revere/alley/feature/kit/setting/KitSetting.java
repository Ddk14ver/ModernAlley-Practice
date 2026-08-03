package dev.revere.alley.feature.kit.setting;

import dev.revere.alley.feature.kit.setting.annotation.KitSettingData;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * @author Remi
 * @project Alley
 * @date 5/21/2024
 */
@Getter
@Setter
public class KitSetting {
    private final String name;
    private final String description;
    private boolean enabled;
    private int value;

    public KitSetting() {
        KitSettingData data = getClass().getAnnotation(KitSettingData.class);
        this.name = Objects.requireNonNull(data).name();
        this.description = data.description();
        this.enabled = data.enabled();
        this.value = 0;
    }
}