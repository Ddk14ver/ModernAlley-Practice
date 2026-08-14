package dev.revere.alley.visual.scoreboard.internal.match.types.state;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.core.config.ConfigService;
import dev.revere.alley.feature.match.model.internal.MatchGamePlayer;
import dev.revere.alley.feature.match.model.GameParticipant;
import dev.revere.alley.feature.match.Match;
import dev.revere.alley.feature.match.task.MatchTask;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.visual.scoreboard.internal.match.MatchScoreboard;
import dev.revere.alley.visual.scoreboard.internal.match.MatchScoreboardSettings;
import dev.revere.alley.common.animation.internal.types.DotAnimation;
import dev.revere.alley.common.text.CC;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Emmy
 * @project Alley
 * @since 30/04/2025
 */
public class MatchScoreboardStartingImpl implements MatchScoreboard {
    private final DotAnimation dotAnimation;

    /**
     * Constructor for the MatchScoreboardStartingImpl class.
     * MatchScoreboardStartingImpl 类的构造函数。
     */
    public MatchScoreboardStartingImpl() {
        this.dotAnimation = new DotAnimation();
    }

    @Override
    public List<String> getLines(Profile profile, Player player, GameParticipant<MatchGamePlayer> you, GameParticipant<MatchGamePlayer> opponent) {
        ConfigService configService = AlleyPlugin.getInstance().getService(ConfigService.class);
        ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);
        Match match = profile.getMatch();
        if (match == null) return List.of();
        MatchTask matchTask = match.getRunnable();
        int countdown = matchTask == null ? 0 : matchTask.getStage();

        List<String> scoreboardLines = new ArrayList<>();
        List<String> template = configService.getScoreboardConfig().getStringList("scoreboard.lines.starting");

        for (String line : template) {
            if (!MatchScoreboardSettings.shouldDisplay(profile, line)) {
                continue;
            }
            scoreboardLines.add(CC.translate(line)
                    .replace("{opponent}", this.getColoredName(profileService.getProfile(opponent.getLeader().getUuid())))
                    .replace("{opponent-ping}", String.valueOf(this.getPing(opponent.getLeader().getTeamPlayer())))
                    .replace("{player-ping}", String.valueOf(this.getPing(player)))
                    .replace("{duration}", match.getDuration())
                    .replace("{arena}", match.getArena().getDisplayName() == null ? "&c&lNULL" : match.getArena().getDisplayName())
                    .replace("{dot-animation}", this.dotAnimation.getCurrentFrame())
                    .replace("{countdown}", String.valueOf(countdown))
                    .replace("{kit}", match.getKit().getDisplayName()));
        }

        return scoreboardLines;
    }
}
