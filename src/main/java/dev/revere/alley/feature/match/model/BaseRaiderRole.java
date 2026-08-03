package dev.revere.alley.feature.match.model;

import lombok.Getter;

/**
 * @author Emmy
 * @project Alley
 * @since 13/06/2025
 */
@Getter
public enum BaseRaiderRole {
    RAIDER("&cRaider"),
    TRAPPER("&eTrapper");

    private final String displayName;

    /**
     * Constructor for the EnumBaseRaiderRole enum.
     * EnumBaseRaiderRole 枚举的构造函数。
     *
     * @param displayName The display name of the raider role.
     *        掠夺者角色的显示名称。
     */
    BaseRaiderRole(String displayName) {
        this.displayName = displayName;
    }
}