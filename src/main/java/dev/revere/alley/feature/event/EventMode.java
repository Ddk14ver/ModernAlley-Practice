package dev.revere.alley.feature.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventMode {
    BRACKETS("Brackets"),
    LAST_MAN_STANDING("Last Man Standing");

    private final String displayName;
}
