package dev.revere.alley.common.time;

import lombok.Getter;
import org.bukkit.ChatColor;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Date formatting utility class.
 * 日期格式化工具类。
 *
 * @author Emmy
 * @project Alley
 * @since 05/04/2025
 */
@Getter
public class DateFormatter {
    private final SimpleDateFormat dateFormat;
    private final Date date;

    /**
     * Constructor for the DateFormatter class.
     * DateFormatter类的构造函数。
     * <p> </p>
     * Constructs a new {@link DateFormatter} using the specified {@link DateFormat}
     * 使用指定的{@link DateFormat}和提供的毫秒时间构造一个新的{@link DateFormatter}
     * and a provided time in milliseconds.
     * <p>
     * This class is useful for formatting timestamps into readable or styled formats,
     * 此类对于将时间戳格式化为可读或样式化的格式非常有用，
     * including custom formats for Discord or in-game display.
     * 包括用于Discord或游戏内显示的自定义格式。
     * </p>
     *
     * <h3>Example Usage:</h3>
     * <h3>使用示例：</h3>
     * <pre>{@code
     * // Create a formatter using the current time and a predefined format
     * // 使用当前时间和预定义格式创建格式化器
     * DateFormatter currentTime = new DateFormatter(EnumDateFormat.TIME, System.currentTimeMillis());
     *
     * // Format the date using the underlying SimpleDateFormat
     * // 使用底层的SimpleDateFormat格式化日期
     * String formattedTime = currentTime.getDateFormat().format(currentTime.getDate());
     * }</pre>
     *
     * @param dateFormat The date format pattern to use, defined by {@link DateFormat}.
     *                   要使用的日期格式模式，由{@link DateFormat}定义。
     * @param time       The timestamp in milliseconds since epoch.
     *                   自纪元以来的毫秒时间戳。
     */

    public DateFormatter(DateFormat dateFormat, long time) {
        this.date = new Date(time);
        this.dateFormat = new SimpleDateFormat(dateFormat.getFormat());
        this.dateFormat.format(this.date);
    }

    /**
     * Get the formatted date in a fancy and readable format (e.g., "1st of December, 2024").
     * 以精美可读的格式获取格式化后的日期（例如"1st of December, 2024"）。
     *
     * @param primaryColor   The primary color for the date.
     *                       日期的主要颜色。
     * @param secondaryColor The secondary color for the date.
     *                       日期的次要颜色。
     * @return The formatted readable date.
     *         格式化后的可读日期。
     */
    public String setFancy(ChatColor primaryColor, ChatColor secondaryColor) {
        this.dateFormat.applyPattern("dd MMMM yyyy");
        String formattedDate = this.dateFormat.format(this.date);

        String[] parts = formattedDate.split(" ");
        String day = parts[0];
        String month = parts[1];
        String year = parts[2];

        String dayWithSuffix = this.addOrdinalSuffix(Integer.parseInt(day));

        return primaryColor + dayWithSuffix + secondaryColor + " of " + primaryColor + month + secondaryColor + ", " + primaryColor + year;
    }

    /**
     * Add the appropriate ordinal suffix (st, nd, rd, th) to the given day.
     * 为给定的日期添加适当的序数后缀（st, nd, rd, th）。
     *
     * @param day The day to which the suffix should be added.
     *            要添加后缀的日期。
     * @return The day with the ordinal suffix.
     *         带有序数后缀的日期。
     */
    private String addOrdinalSuffix(int day) {
        if (day >= 11 && day <= 13) {
            return day + "th";
        }
        switch (day % 10) {
            case 1:
                return day + "st";
            case 2:
                return day + "nd";
            case 3:
                return day + "rd";
            default:
                return day + "th";
        }
    }
}
