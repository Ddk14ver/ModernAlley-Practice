package dev.revere.alley.feature.kit;

import lombok.Getter;

/**
 * @author Emmy
 * @project Alley
 * @since 01/05/2025
 */
@Getter
public enum KitCategory {
    NORMAL("Normal", "Regularly-accessible modes."),
    EXTRA("Extra", "Less-popular modes."),

    ;

    private final String name;
    private final String description;

    /**
     * Constructor for the EnumKitCategory enum.
     * EnumKitCategory枚举的构造函数。
     *
     * @param name        The name of the kit category.
     *                   工具包分类的名称。
     * @param description The description of the kit category.
     *                   工具包分类的描述。
     */
    KitCategory(String name, String description) {
        this.name = name;
        this.description = description;
    }
}