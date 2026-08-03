package dev.revere.alley.visual.scoreboard.internal.match;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.core.config.ConfigService;
import dev.revere.alley.feature.cps.CPSManager;
import dev.revere.alley.feature.match.Match;
import dev.revere.alley.feature.match.model.internal.MatchGamePlayer;
import dev.revere.alley.feature.match.model.GameParticipant;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.visual.scoreboard.internal.match.MatchScoreboardImpl;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Remi
 * @project Alley
 * @since 26/06/2025
 */
public abstract class BaseMatchScoreboard implements MatchScoreboard {

    protected static final CPSManager cpsManager = MatchScoreboardImpl.cpsMgr;

    /**
     * Gets the path to the solo version of the scoreboard in the config.
     * 获取配置中单人版记分板的路径。
     *
     * @return The configuration path.
     *         配置路径。
     */
    protected abstract String getSoloConfigPath();

    /**
     * Gets the path to the team version of the scoreboard in the config.
     * 获取配置中团队版记分板的路径。
     *
     * @return The configuration path.
     *         配置路径。
     */
    protected abstract String getTeamConfigPath();

    @Override
    public List<String> getLines(Profile profile, Player player, GameParticipant<MatchGamePlayer> you, GameParticipant<MatchGamePlayer> opponent) {
        List<String> scoreboardLines = new ArrayList<>();
        Match match = profile.getMatch();
        String configPath = match.isTeamMatch() ? getTeamConfigPath() : getSoloConfigPath();

        for (String line : AlleyPlugin.getInstance().getService(ConfigService.class).getScoreboardConfig().getStringList(configPath)) {
            scoreboardLines.add(replacePlaceholders(line, profile, player, you, opponent));
        }

        return scoreboardLines;
    }

    /**
     * Replaces all placeholders in a given line of the scoreboard.
     * 替换记分板中给定行的所有占位符。
     * Child classes should override this to add their own specific placeholders.
     * 子类应重写此方法以添加其自己的特定占位符。
     *
     * @param line     The line with placeholders.
     *                 包含占位符的行。
     * @param profile  The player's profile.
     *                 玩家的档案。
     * @param player   The player.
     *                 玩家。
     * @param you      The player's game participant.
     *                 玩家的游戏参与者。
     * @param opponent The opponent's game participant.
     *                 对手的游戏参与者。
     * @return The line with all placeholders replaced.
     *         所有占位符被替换后的行。
     */
    protected String replacePlaceholders(String line, Profile profile, Player player, GameParticipant<MatchGamePlayer> you, GameParticipant<MatchGamePlayer> opponent) {
        Match match = profile.getMatch();

        ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);

        UUID opponentId = opponent.getLeader().getUuid();
        Player opponentPlayer = opponent.getLeader().getTeamPlayer();
        Profile opponentProfile = profileService.getProfile(opponentId);
        String coloredOpponentName = opponentProfile == null
                ? "&f" + opponent.getLeader().getUsername()
                : getColoredName(opponentProfile);
        String opponentName = match.isTeamMatch() ? coloredOpponentName + "' Team" : coloredOpponentName;

        return CC.translate(line)
                .replace("{opponent}", opponentName)
                .replace("{player-ping}", String.valueOf(getPing(player)))
                .replace("{opponent-ping}", String.valueOf(getPing(opponentPlayer)))
                .replace("{duration}", match.getDuration())
                .replace("{arena}", match.getArena().getDisplayName() == null ? "&c&lNULL" : match.getArena().getDisplayName())
                .replace("{kit}", match.getKit().getDisplayName())
                .replace("{your-players}", String.valueOf(you.getPlayerSize()))
                .replace("{opponent-players}", String.valueOf(opponent.getPlayerSize()))
                .replace("{your-alive}", String.valueOf(you.getAlivePlayerSize()))
                .replace("{opponent-alive}", String.valueOf(opponent.getAlivePlayerSize()))
                .replace("{your-cps}", String.valueOf(cpsManager.getCPS(player)))
                .replace("{opponent-cps}", String.valueOf(cpsManager.getCPS(opponentId)))
                .replace("{your-max-cps}", String.valueOf(cpsManager.getMaxCPS(player)))
                .replace("{opponent-max-cps}", String.valueOf(cpsManager.getMaxCPS(opponentId)));
    }
}
