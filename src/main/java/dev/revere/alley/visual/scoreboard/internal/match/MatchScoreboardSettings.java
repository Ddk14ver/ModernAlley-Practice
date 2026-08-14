package dev.revere.alley.visual.scoreboard.internal.match;

import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.data.types.ProfileSettingData;

/**
 * Applies the player's personal match-scoreboard visibility settings to a
 * configured scoreboard line before its placeholders are replaced.
 */
public final class MatchScoreboardSettings {

    private MatchScoreboardSettings() {
    }

    public static boolean shouldDisplay(Profile profile, String line) {
        if (profile == null || line == null) {
            return true;
        }

        ProfileSettingData settings = profile.getProfileData().getSettingData();
        if ((line.contains("{your-cps}") || line.contains("{opponent-cps}"))
                && !settings.isShowMatchCps()) {
            return false;
        }
        if ((line.contains("{ping}") || line.contains("{player-ping}") || line.contains("{opponent-ping}"))
                && !settings.isShowMatchPing()) {
            return false;
        }
        return !line.contains("{opponent}") || settings.isShowMatchOpponent();
    }
}
