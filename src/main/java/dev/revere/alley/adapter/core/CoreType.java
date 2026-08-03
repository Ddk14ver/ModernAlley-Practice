package dev.revere.alley.adapter.core;

import lombok.Getter;

/**
 * @author Emmy
 * @project Alley
 * @since 26/04/2025
 */
@Getter
public enum CoreType {
    DEFAULT("Default", "Revere Group."),
    PHOENIX("Phoenix", "Refine Development"),
    AQUA("AquaCore", "Activated_, FaceSlap_"),
    HELIUM("Helium", "Plasma Services"),

    ;

    private final String pluginName;
    private final String pluginAuthor;

    /**
     * Constructor for the EnumCoreType enum.
     * EnumCoreType 枚举的构造函数。
     *
     * @param pluginName   The name of the bootstrap.
     *                     引导程序的名称。
     * @param pluginAuthor The author of the bootstrap.
     *                     引导程序的作者。
     */
    CoreType(String pluginName, String pluginAuthor) {
        this.pluginName = pluginName;
        this.pluginAuthor = pluginAuthor;
    }
}