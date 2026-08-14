package dev.revere.alley.feature.challenge;

import dev.revere.alley.bootstrap.lifecycle.Service;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.data.types.ProfileChallengeProgress;

public interface ChallengeService extends Service {
    boolean accept(Profile profile, ChallengePeriod period, ChallengeType type);

    void recordProgress(Profile profile, ChallengeType type, int amount);

    void deferCompletionMessages(Profile profile);

    void flushCompletionMessages(Profile profile);

    void synchronizePeriod(Profile profile);

    ChallengeDefinition getDefinition(ChallengePeriod period, ChallengeType type);

    ProfileChallengeProgress getProgress(Profile profile, ChallengePeriod period, ChallengeType type);

    long getSecondsUntilReset(ChallengePeriod period);

    void reloadDefinitions();
}
