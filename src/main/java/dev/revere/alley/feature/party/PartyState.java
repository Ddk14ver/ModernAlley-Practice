package dev.revere.alley.feature.party;

import lombok.Getter;

/**
 * 队伍状态枚举，定义队伍的公开/私有状态。
 * @author Emmy
 * @project Alley
 * @date 24/05/2024 - 22:57
 */
@Getter
public enum PartyState {
    PRIVATE("Private", "Only invited players can join"),
    PUBLIC("Public", "Everyone can join");

    private final String name;
    private final String description;

    /**
     * Constructor for the EnumPartyState
     * EnumPartyState 的构造函数
     *
     * @param name        The name of the party state
     *                    队伍状态的名称
     * @param description The description of the party state
     *                    队伍状态的描述
     */
    PartyState(String name, String description) {
        this.name = name;
        this.description = description;
    }
}