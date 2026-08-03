package dev.revere.alley.core.profile.enums;

import lombok.Getter;

/**
 * @author Emmy
 * @project Alley
 * @date 13/10/2024 - 10:17
 * 世界时间枚举，定义世界时间类型。
 * World time enum, defining world time types.
 */
@Getter
public enum WorldTime {
    DAY("DAY"),
    SUNSET("SUNSET"),
    NIGHT("NIGHT"),
    DEFAULT("DEFAULT");

    private final String name;

    /**
     * Constructor for the EnumWorldTime enum.
     * EnumWorldTime 枚举的构造函数。
     *
     * @param name The name of the world time type.
     *             世界时间类型的名称。
     */
    WorldTime(String name) {
        this.name = name;
    }

    /**
     * Get an EnumWorldTime by its name.
     * 根据名称获取 EnumWorldTime。
     *
     * @param name The name of the EnumWorldTime.
     *             EnumWorldTime 的名称。
     * @return The EnumWorldTimeType with the given name.
     *         具有给定名称的 EnumWorldTimeType。
     */
    public static WorldTime getByName(String name) {
        for (WorldTime worldTimeType : values()) {
            if (worldTimeType.getName().equalsIgnoreCase(name)) {
                return worldTimeType;
            }
        }
        return null;
    }
}