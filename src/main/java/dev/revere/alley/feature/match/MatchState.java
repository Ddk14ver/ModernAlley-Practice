package dev.revere.alley.feature.match;

import lombok.Getter;

/**
 * Enum representing the various states a match can be in.
 * 表示比赛可能处于的各种状态的枚举。
 * @author Remi
 * @project Alley
 * @date 5/21/2024
 */
@Getter
public enum MatchState {
    STARTING("Starting", "Starting"),
    RUNNING("Running", "In-Game"),
    ENDING_ROUND("Ending Round", "Ending"),
    RESTARTING_ROUND("Restarting Round", "Restarting"),
    ENDING_MATCH("Ending Match", "Ending");

    private final String name;
    private final String description;

    /**
     * Constructor for the EnumMatchState enum.
     * 比赛状态枚举的构造方法。
     * @param name        The name of the match state.
     *                      比赛状态的名称。
     * @param description The description of the match state.
     *                      比赛状态的描述。
     */
    MatchState(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
