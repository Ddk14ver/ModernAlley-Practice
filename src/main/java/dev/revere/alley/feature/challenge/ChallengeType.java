package dev.revere.alley.feature.challenge;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChallengeType {
    KILLS("kills", "Kills"),
    WINS("wins", "Wins"),
    ELO("elo", "Elo");

    private final String configKey;
    private final String unitName;
}
