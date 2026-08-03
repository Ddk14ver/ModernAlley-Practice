package dev.revere.alley.common.time;

import lombok.Getter;

/**
 * Enumeration of date format patterns.
 * 日期格式模式的枚举。
 *
 * @author Emmy
 * @project Alley
 * @since 05/04/2025
 */
@Getter
public enum DateFormat {
    TIME_PLUS_DATE("HH:mm:ss dd/MM/yyyy"),
    DATE_PLUS_TIME("dd/MM/yyyy HH:mm:ss"),
    TIME("HH:mm:ss"),
    DATE("dd/MM/yyyy"),

    ;

    private final String format;

    /**
     * Constructor for the EnumDateFormat enum.
     * DateFormat枚举的构造函数。
     *
     * @param format The date format string.
     *               日期格式字符串。
     */
    DateFormat(String format) {
        this.format = format;
    }
}
