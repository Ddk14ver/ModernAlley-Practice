package dev.revere.alley.visual.scoreboard.internal.match;

import dev.revere.alley.feature.kit.setting.KitSetting;
import dev.revere.alley.feature.match.Match;
import dev.revere.alley.feature.event.skywars.SkyWarsMatch;
import dev.revere.alley.visual.scoreboard.internal.match.annotation.ScoreboardData;
import dev.revere.alley.common.logger.Logger;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Remi
 * @project Alley
 * @since 26/06/2025
 */
public class MatchScoreboardRegistry {
    private final Map<Class<? extends KitSetting>, MatchScoreboard> kitSettingScoreboards = new HashMap<>();
    private final Map<Class<? extends Match>, MatchScoreboard> matchTypeScoreboards = new HashMap<>();
    private MatchScoreboard defaultScoreboard;

    /**
     * Scans the classpath to discover and register all annotated scoreboard providers.
     * 扫描类路径以发现并注册所有带注解的计分板提供者。
     * This should be called once by the object that creates the registry.
     * 此方法应由创建注册表的对象调用一次。
     */
    public void initialize() {
        String searchPackage = "dev.revere.alley.visual.scoreboard";

        try (ScanResult scanResult = new ClassGraph().enableAllInfo().acceptPackages(searchPackage).scan()) {
            for (ClassInfo classInfo : scanResult.getClassesWithAnnotation(ScoreboardData.class.getName())) {
                if (classInfo.isAbstract() || classInfo.isInterface()) {
                    continue;
                }

                try {
                    Class<?> clazz = classInfo.loadClass();
                    if (!MatchScoreboard.class.isAssignableFrom(clazz)) {
                        continue;
                    }

                    Constructor<?> constructor = clazz.getConstructor();

                    MatchScoreboard scoreboard = (MatchScoreboard) constructor.newInstance();
                    ScoreboardData annotation = clazz.getAnnotation(ScoreboardData.class);

                    assert annotation != null;
                    if (annotation.isDefault()) {
                        this.defaultScoreboard = scoreboard;
                    } else if (annotation.kit() != KitSetting.class) {
                        kitSettingScoreboards.put(annotation.kit(), scoreboard);
                    } else if (annotation.match() != Match.class) {
                        matchTypeScoreboards.put(annotation.match(), scoreboard);
                    }
                } catch (Exception e) {
                    Logger.logException("Failed to instantiate scoreboard visual: " + classInfo.getName(), e);
                }
            }
        }
    }

    /**
     * Resolves the appropriate scoreboard for the given match.
     * 根据给定的比赛解析对应的计分板。
     *
     * @param match The match to resolve.
     *              要解析的比赛。
     * @return The resolved IMatchScoreboard.
     *         解析后的 IMatchScoreboard。
     */
    public MatchScoreboard getScoreboard(Match match) {
        // SkyWars must always expose its opening-protection countdown, even if
        // its gameplay kit also has a generic setting-specific scoreboard.
        if (match instanceof SkyWarsMatch) {
            MatchScoreboard skyWarsScoreboard = matchTypeScoreboards.get(SkyWarsMatch.class);
            if (skyWarsScoreboard != null) {
                return skyWarsScoreboard;
            }
        }

        for (Class<? extends KitSetting> settingClass : kitSettingScoreboards.keySet()) {
            if (match.getKit().isSettingEnabled(settingClass)) {
                return kitSettingScoreboards.get(settingClass);
            }
        }

        MatchScoreboard scoreboard = matchTypeScoreboards.get(match.getClass());
        if (scoreboard != null) {
            return scoreboard;
        }

        return defaultScoreboard;
    }
}
