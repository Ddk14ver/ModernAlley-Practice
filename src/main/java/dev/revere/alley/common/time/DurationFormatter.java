package dev.revere.alley.common.time;

import org.apache.commons.lang3.time.DurationFormatUtils;

import java.util.concurrent.TimeUnit;

/**
 * Duration formatting utility class.
 * 持续时间格式化工具类。
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 27/06/2026
 */
public class DurationFormatter {

    private static final long MINUTE;
    private static final long HOUR;

    static {
        MINUTE = TimeUnit.MINUTES.toMillis(1L);
        HOUR = TimeUnit.HOURS.toMillis(1L);
    }

    /**
     * Gets the remaining time formatted as a string.
     * 获取剩余时间的格式化字符串。
     *
     * @param millis       the duration in milliseconds.
     *                     以毫秒为单位的持续时间。
     * @param milliseconds whether to include milliseconds in the output.
     *                     是否在输出中包含毫秒。
     * @return the formatted remaining time string.
     *         格式化后的剩余时间字符串。
     */
    public static String getRemaining(long millis, boolean milliseconds) {
        return getRemaining(millis, milliseconds, true);
    }

    /**
     * Gets the remaining time formatted as a string with trailing option.
     * 获取剩余时间的格式化字符串，带有尾随选项。
     *
     * @param duration     the duration in milliseconds.
     *                     以毫秒为单位的持续时间。
     * @param milliseconds whether to include milliseconds in the output.
     *                     是否在输出中包含毫秒。
     * @param trail        whether to use trailing decimal format.
     *                     是否使用尾随小数格式。
     * @return the formatted remaining time string.
     *         格式化后的剩余时间字符串。
     */
    public static String getRemaining(long duration, boolean milliseconds, boolean trail) {
        if (milliseconds && duration < DurationFormatter.MINUTE) {
            return String.valueOf((trail ? DateTimeFormats.REMAINING_SECONDS_TRAILING : DateTimeFormats.REMAINING_SECONDS).get().format(duration * 0.001)) + 's';
        }
        return DurationFormatUtils.formatDuration(duration, ((duration >= DurationFormatter.HOUR) ? "HH:" : "") + "mm:ss");
    }
}
