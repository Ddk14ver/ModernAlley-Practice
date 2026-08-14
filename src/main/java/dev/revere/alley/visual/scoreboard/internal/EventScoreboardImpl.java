package dev.revere.alley.visual.scoreboard.internal;

import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.feature.event.EventState;
import dev.revere.alley.feature.event.HostedEvent;
import dev.revere.alley.visual.scoreboard.Scoreboard;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EventScoreboardImpl implements Scoreboard {
    @Override
    public List<String> getLines(Profile profile) {
        HostedEvent event = profile.getGameEvent();
        if (event == null) {
            return Collections.emptyList();
        }

        List<String> lines = new ArrayList<>();
        boolean showScoreboardLines = profile.getProfileData().getSettingData().isShowScoreboardLines();
        if (showScoreboardLines) {
            lines.add(CC.translate("&7&m--------------------"));
        }
        lines.add(CC.translate("&6&lEvent"));
        lines.add(CC.translate(" &fType: &6" + event.getDisplayName()));
        lines.add(CC.translate(" &fKit: &6" + event.getKit().getDisplayName()));
        lines.add(CC.translate(" &fHost: &6" + event.getHostName()));
        lines.add("");
        lines.add(CC.translate(" &fState: &6" + event.getState().getDisplayName()));

        if (event.getState() == EventState.STARTING) {
            lines.add(CC.translate(" &fPlayers: &6" + event.getParticipants().size() + "/" + event.getMaxPlayers()));
            lines.add(CC.translate(" &fStarting in: &6" + Math.max(0, event.getCountdown()) + "s"));
        } else if (event.getState() == EventState.RUNNING) {
            lines.add(CC.translate(" &fRound: &6" + Math.max(1, event.getRound())));
            lines.add(CC.translate(" &fRemaining: &6" + event.getRemainingPlayers().size()));
        }

        if (showScoreboardLines) {
            lines.add(CC.translate("&7&m--------------------"));
        }
        return lines;
    }

    @Override
    public List<String> getLines(Profile profile, Player player) {
        return getLines(profile);
    }
}
