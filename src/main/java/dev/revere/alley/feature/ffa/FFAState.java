package dev.revere.alley.feature.ffa;

import lombok.Getter;

/**
 * @author Emmy
 * @project Alley
 * @date 25/05/2024 - 14:25
 */
@Getter
public enum FFAState {
    SPAWN("Spawn", "The player is in the safezone."),
    FIGHTING("Fighting", "The player is fighting outside safezone.");

    private final String name;
    private final String description;

    /**
     * Constructor for the EnumFFAState enum.
     * 枚举 FFAState 的构造方法。
     *
     * @param name        The name of the state.
     *                     状态名称。
     * @param description The description of the state.
     *                     状态的描述。
     */
    FFAState(String name, String description) {
        this.name = name;
        this.description = description;
    }
}