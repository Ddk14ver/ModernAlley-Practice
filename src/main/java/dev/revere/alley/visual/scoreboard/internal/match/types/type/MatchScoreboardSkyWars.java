package dev.revere.alley.visual.scoreboard.internal.match.types.type;

import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.feature.event.skywars.SkyWarsMatch;
import dev.revere.alley.feature.match.model.GameParticipant;
import dev.revere.alley.feature.match.model.internal.MatchGamePlayer;
import dev.revere.alley.visual.scoreboard.internal.match.annotation.ScoreboardData;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/** Adds SkyWars-only state to the normal FFA sidebar without changing the shared FFA layout. */
@ScoreboardData(match = SkyWarsMatch.class)
public class MatchScoreboardSkyWars extends MatchScoreboardFFA {
    @Override
    public List<String> getLines(Profile profile, Player player, GameParticipant<MatchGamePlayer> you,
                                 GameParticipant<MatchGamePlayer> opponent) {
        List<String> lines = new ArrayList<>(super.getLines(profile, player, you, opponent));
        SkyWarsMatch match = (SkyWarsMatch) profile.getMatch();
        String status = match.isProtectionActive()
                ? "&a" + match.getProtectionSecondsRemaining() + "s"
                : "&cEnded";
        int insertionIndex = Math.min(5, lines.size());
        lines.add(insertionIndex, CC.translate("&b| &fProtection: " + status));
        return lines;
    }
}
