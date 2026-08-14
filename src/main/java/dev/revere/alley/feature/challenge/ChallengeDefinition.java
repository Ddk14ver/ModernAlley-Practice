package dev.revere.alley.feature.challenge;

import lombok.Value;

@Value
public class ChallengeDefinition {
    ChallengePeriod period;
    ChallengeType type;
    String displayName;
    int requirement;
    int rewardCoins;
}
