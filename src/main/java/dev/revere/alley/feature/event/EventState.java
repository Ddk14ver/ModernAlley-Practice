package dev.revere.alley.feature.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventState {
    QUEUED("Queued"),
    STARTING("Starting"),
    RUNNING("Running"),
    ENDED("Ended");

    private final String displayName;
}
