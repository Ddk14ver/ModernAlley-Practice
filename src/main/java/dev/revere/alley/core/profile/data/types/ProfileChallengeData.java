package dev.revere.alley.core.profile.data.types;

import dev.revere.alley.feature.challenge.ChallengePeriod;
import dev.revere.alley.feature.challenge.ChallengeType;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class ProfileChallengeData {
    private String dailyPeriodKey = "";
    private String weeklyPeriodKey = "";
    private Map<String, ProfileChallengeProgress> dailyStates = new HashMap<>();
    private Map<String, ProfileChallengeProgress> weeklyStates = new HashMap<>();

    public ProfileChallengeProgress getOrCreate(ChallengePeriod period, ChallengeType type) {
        return getStates(period).computeIfAbsent(type.getConfigKey(), ignored -> new ProfileChallengeProgress());
    }

    public Map<String, ProfileChallengeProgress> getStates(ChallengePeriod period) {
        return period == ChallengePeriod.DAILY ? this.dailyStates : this.weeklyStates;
    }

    public void clear(ChallengePeriod period) {
        getStates(period).clear();
    }
}
