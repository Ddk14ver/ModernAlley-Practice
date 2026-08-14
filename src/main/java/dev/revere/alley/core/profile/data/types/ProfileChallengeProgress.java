package dev.revere.alley.core.profile.data.types;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileChallengeProgress {
    private boolean accepted;
    private int progress;
    private boolean completed;
    private boolean rewarded;
}
