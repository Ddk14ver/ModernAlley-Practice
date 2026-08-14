package dev.revere.alley.feature.challenge;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChallengePeriod {
    DAILY("daily", "Daily"),
    WEEKLY("weekly", "Weekly");

    private final String configKey;
    private final String displayName;
}
