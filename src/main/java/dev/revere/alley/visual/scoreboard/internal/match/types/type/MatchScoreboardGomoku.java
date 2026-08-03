package dev.revere.alley.visual.scoreboard.internal.match.types.type;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.core.config.ConfigService;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.feature.bot.BotService;
import dev.revere.alley.feature.bot.match.BotMatchSession;
import dev.revere.alley.feature.kit.setting.types.mode.KitSettingGomoku;
import dev.revere.alley.feature.match.internal.types.GomokuPlayable;
import dev.revere.alley.feature.match.model.GameParticipant;
import dev.revere.alley.feature.match.model.internal.MatchGamePlayer;
import dev.revere.alley.visual.scoreboard.internal.match.BaseMatchScoreboard;
import dev.revere.alley.visual.scoreboard.internal.match.annotation.ScoreboardData;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@ScoreboardData(kit = KitSettingGomoku.class)
public class MatchScoreboardGomoku extends BaseMatchScoreboard {
    @Override
    protected String getSoloConfigPath() {
        return "scoreboard.lines.playing.solo.gomoku-match";
    }

    @Override
    protected String getTeamConfigPath() {
        return "scoreboard.lines.playing.team.gomoku-match";
    }

    @Override
    public List<String> getLines(Profile profile, Player player,
                                 GameParticipant<MatchGamePlayer> you,
                                 GameParticipant<MatchGamePlayer> opponent) {
        String path = profile.getMatch().isTeamMatch() ? getTeamConfigPath() : getSoloConfigPath();
        List<String> template = AlleyPlugin.getInstance().getService(ConfigService.class)
                .getScoreboardConfig().getStringList(path);
        if (template.isEmpty()) {
            template = List.of(
                    "{sidebar}",
                    "&6&lɢᴏᴍᴏᴋᴜ",
                    "&6&l│ &rColor: {gomoku-color}",
                    "&6&l│ &rTurn: &e{gomoku-current}",
                    "&6&l│ &rTime: &e{gomoku-time}s",
                    "&6&l│ &rStones: &e{gomoku-stones}/225",
                    "&6&l│ &rDuration: &6{duration}",
                    "&6&l│ &rPing: &a{player-ping}ms&f ┃ &c{opponent-ping}ms"
            );
        }

        List<String> lines = new ArrayList<>(template.size());
        for (String line : template) {
            if (line.isEmpty()) continue;
            String normalized = line.equals("&6&lGomoku") ? "&6&lɢᴏᴍᴏᴋᴜ" : line;
            lines.add(replacePlaceholders(normalized, profile, player, you, opponent));
        }
        return lines;
    }

    @Override
    protected String replacePlaceholders(String line, Profile profile, Player player,
                                         GameParticipant<MatchGamePlayer> you,
                                         GameParticipant<MatchGamePlayer> opponent) {
        String replaced = super.replacePlaceholders(line, profile, player, you, opponent);
        if (profile.getMatch() instanceof GomokuPlayable gomoku) {
            return replaced
                    .replace("{gomoku-color}", gomoku.getPlayerColorName(player.getUniqueId()))
                    .replace("{gomoku-current}", gomoku.getCurrentPlayerName())
                    .replace("{gomoku-time}", String.valueOf(gomoku.getRemainingTurnSeconds()))
                    .replace("{gomoku-stones}", String.valueOf(gomoku.getPlacedStones()));
        }

        BotMatchSession session = AlleyPlugin.getInstance().getService(BotService.class).getSession(player);
        if (session == null) return replaced;
        return replaced
                .replace("{gomoku-color}", session.getGomokuPlayerColorName())
                .replace("{gomoku-current}", session.getGomokuCurrentPlayerName())
                .replace("{gomoku-time}", String.valueOf(session.getGomokuRemainingTurnSeconds()))
                .replace("{gomoku-stones}", String.valueOf(session.getGomokuPlacedStones()));
    }
}
