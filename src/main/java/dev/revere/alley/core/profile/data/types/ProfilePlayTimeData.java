package dev.revere.alley.core.profile.data.types;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Emmy
 * @project Alley
 * @since 05/04/2025
 */
@Getter
@Setter
public class ProfilePlayTimeData {
    private long total;
    private long lastLogin;

    public ProfilePlayTimeData() {
        this.total = 0;
        this.lastLogin = System.currentTimeMillis();
    }
}
