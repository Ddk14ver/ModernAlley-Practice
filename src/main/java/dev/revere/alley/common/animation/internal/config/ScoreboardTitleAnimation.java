package dev.revere.alley.common.animation.internal.config;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.core.config.ConfigService;

/**
 * @author Emmy
 * @project Alley
 * @since 27/03/2025
 */
public class ScoreboardTitleAnimation extends TextAnimation {
    public ScoreboardTitleAnimation() {
        super(AlleyPlugin.getInstance().getService(ConfigService.class).getScoreboardConfig(), "scoreboard.title");
    }
}