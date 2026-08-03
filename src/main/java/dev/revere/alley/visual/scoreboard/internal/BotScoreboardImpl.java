package dev.revere.alley.visual.scoreboard.internal;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.config.ConfigService;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.feature.bot.BotService;
import dev.revere.alley.feature.bot.match.BotMatchSession;
import dev.revere.alley.visual.scoreboard.Scoreboard;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BotScoreboardImpl implements Scoreboard {
    @Override
    public List<String> getLines(Profile profile) {
        return Collections.emptyList();
    }

    @Override
    public List<String> getLines(Profile profile, Player player) {
        BotMatchSession session = AlleyPlugin.getInstance().getService(BotService.class).getSession(player);
        if (session == null) return Collections.emptyList();

        int elapsedSeconds = Math.max(0, session.getTicks() - session.getCountdownTicks()) / 20;
        String duration = String.format("%02d:%02d", elapsedSeconds / 60, elapsedSeconds % 60);
        List<String> lines = new ArrayList<>();
        List<String> configuredLines = AlleyPlugin.getInstance().getService(ConfigService.class)
                .getScoreboardConfig().getStringList("scoreboard.lines.bot-match");
        if (configuredLines.isEmpty()) {
            configuredLines = List.of(
                    "{sidebar}",
                    "&6&lBot Duel",
                    "&6&l| &rKit: &6{kit}",
                    "&6&l| &rAI: &6{bot-ai-mode}",
                    "&6&l| &rDifficulty: &6{bot-difficulty}",
                    "&6&l| &rDuration: &6{duration}"
            );
        }
        for (String line : configuredLines) {
            lines.add(CC.translate(line)
                    .replace("{kit}", session.getKit().getDisplayName())
                    .replace("{bot-ai-mode}", session.getAiMode().getDisplayName())
                    .replace("{bot-difficulty}", session.getDifficulty().getDisplayName())
                    .replace("{duration}", duration));
        }
        return lines;
    }
}
