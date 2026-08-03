package dev.revere.alley.common.time;

import lombok.experimental.UtilityClass;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Time utility class for time conversion and formatting.
 * 用于时间转换和格式化的时间工具类。
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 01/07/2026
 */
@UtilityClass
public final class TimeUtil {
    private final String HOUR_FORMAT = "%02d:%02d:%02d";
    private final String MINUTE_FORMAT = "%02d:%02d";

    /**
     * Converts milliseconds to a timer format.
     * 将毫秒转换为计时器格式。
     *
     * @param millis the milliseconds to convert.
     *               要转换的毫秒数。
     * @return the formatted time.
     *         格式化后的时间。
     */
    public String millisToTimer(long millis) {
        long seconds = millis / 1000L;

        if (seconds > 3600L) {
            return String.format(HOUR_FORMAT, seconds / 3600L, seconds % 3600L / 60L, seconds % 60L);
        } else {
            return String.format(MINUTE_FORMAT, seconds / 60L, seconds % 60L);
        }
    }

    /**
     * Converts milliseconds to a four digit seconds format. (00:00)
     * 将毫秒转换为四位秒格式。(00:00)
     *
     * @param millis the milliseconds to convert.
     *               要转换的毫秒数。
     * @return the formatted time.
     *         格式化后的时间。
     */
    public String millisToFourDigitSecondsTimer(long millis) {
        long minutes = (millis / 1000) / 60;
        long seconds = (millis / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * Formats a long time value to mm:ss format.
     * 将长整型时间值格式化为mm:ss格式。
     *
     * @param time the time in milliseconds.
     *             以毫秒为单位的时间。
     * @return the formatted time string.
     *         格式化后的时间字符串。
     */
    public static String formatLongMin(final long time) {
        final long totalSecs = time / 1000L;
        return String.format("%02d:%02d", totalSecs / 60L, totalSecs % 60L);
    }

    /**
     * Converts milliseconds to a seconds format. (00)
     * 将毫秒转换为秒格式。(00)
     *
     * @param millis the milliseconds to convert.
     *               要转换的毫秒数。
     * @return the formatted time.
     *         格式化后的时间。
     */
    public String millisToSecondsTimer(long millis) {
        return String.valueOf(millis / 1000);
    }

    /**
     * Converts elapsed milliseconds to a formatted time string in "mm:ss" format.
     * 将经过的毫秒数转换为"mm:ss"格式的格式化时间字符串。
     *
     * @param elapsedMillis the elapsed time in milliseconds.
     *                      以毫秒为单位的经过时间。
     * @return the formatted time string.
     *         格式化后的时间字符串。
     */
    public String getFormattedElapsedTime(long elapsedMillis) {
        long elapsedSeconds = elapsedMillis / 1000;
        return String.format("%02d:%02d", elapsedSeconds / 60, elapsedSeconds % 60);
    }

    /**
     * Converts a total number of seconds into a formatted time string in "mm:ss" format.
     * 将总秒数转换为"mm:ss"格式的格式化时间字符串。
     *
     * @param totalSeconds the total number of seconds to convert.
     *                     要转换的总秒数。
     * @return the formatted time string in "mm:ss" format.
     *         "mm:ss"格式的格式化时间字符串。
     */
    public static String formatTimeFromSeconds(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * Converts a date to a string.
     * 将日期转换为字符串。
     *
     * @param date the date to convert.
     *             要转换的日期。
     * @param secondaryColor the secondary color for formatting.
     *                       用于格式化的次要颜色。
     * @return the formatted date.
     *         格式化后的日期。
     */
    public String dateToString(Date date, String secondaryColor) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        return new SimpleDateFormat("MMM dd yyyy " + (secondaryColor == null ? "" : secondaryColor) +
                "(hh:mm aa zz)").format(date);
    }

    /**
     * Adds a duration to the current time.
     * 将持续时间添加到当前时间。
     *
     * @param duration the duration to add.
     *                 要添加的持续时间。
     * @return the new timestamp.
     *         新的时间戳。
     */
    public Timestamp addDuration(long duration) {
        return truncateTimestamp(new Timestamp(System.currentTimeMillis() + duration));
    }

    /**
     * Truncates a timestamp to the year 2037.
     * 将时间戳截断到2037年。
     *
     * @param timestamp the timestamp to truncate.
     *                  要截断的时间戳。
     * @return the truncated timestamp.
     *         截断后的时间戳。
     */
    public Timestamp truncateTimestamp(Timestamp timestamp) {
        if (timestamp.toLocalDateTime().getYear() > 2037) {
            timestamp.setYear(2037);
        }

        return timestamp;
    }

    /**
     * Adds a duration to the current time.
     * 将持续时间添加到当前时间。
     *
     * @param timestamp the duration to add.
     *                  要添加的持续时间。
     * @return the new timestamp.
     *         新的时间戳。
     */
    public Timestamp addDuration(Timestamp timestamp) {
        return truncateTimestamp(new Timestamp(System.currentTimeMillis() + timestamp.getTime()));
    }

    /**
     * Converts milliseconds to a timestamp.
     * 将毫秒转换为时间戳。
     *
     * @param millis the milliseconds to convert.
     *               要转换的毫秒数。
     * @return the timestamp.
     *         时间戳。
     */
    public Timestamp fromMillis(long millis) {
        return new Timestamp(millis);
    }

    /**
     * Gets the current timestamp.
     * 获取当前时间戳。
     *
     * @return the current timestamp.
     *         当前时间戳。
     */
    public Timestamp getCurrentTimestamp() {
        return new Timestamp(System.currentTimeMillis());
    }

    /**
     * Converts milliseconds to a human-readable rounded time string.
     * 将毫秒转换为人类可读的舍入时间字符串。
     *
     * @param millis the milliseconds to convert.
     *               要转换的毫秒数。
     * @return the human-readable rounded time string.
     *         人类可读的舍入时间字符串。
     */
    public String millisToRoundedTime(long millis) {
        millis += 1L;

        long seconds = millis / 1000L;
        long minutes = seconds / 60L;
        long hours = minutes / 60L;
        long days = hours / 24L;
        long weeks = days / 7L;
        long months = weeks / 4L;
        long years = months / 12L;

        if (years > 0) {
            return years + " year" + (years == 1 ? "" : "s");
        } else if (months > 0) {
            return months + " month" + (months == 1 ? "" : "s");
        } else if (weeks > 0) {
            return weeks + " week" + (weeks == 1 ? "" : "s");
        } else if (days > 0) {
            return days + " day" + (days == 1 ? "" : "s");
        } else if (hours > 0) {
            return hours + " hour" + (hours == 1 ? "" : "s");
        } else if (minutes > 0) {
            return minutes + " minute" + (minutes == 1 ? "" : "s");
        } else {
            return seconds + " second" + (seconds == 1 ? "" : "s");
        }
    }

    /**
     * Parses a time string to milliseconds.
     * 将时间字符串解析为毫秒。
     *
     * @param time the time string to parse.
     *             要解析的时间字符串。
     * @return the parsed time in milliseconds.
     *         解析后的时间（毫秒）。
     */
    public long parseTime(String time) {
        long totalTime = 0L;
        boolean found = false;
        Matcher matcher = Pattern.compile("\\d+\\D+").matcher(time);

        while (matcher.find()) {
            String s = matcher.group();
            Long value = Long.parseLong(s.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)")[0]);
            String type = s.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)")[1];

            switch (type) {
                case "s":
                    totalTime += value;
                    found = true;
                    break;
                case "m":
                    totalTime += value * 60;
                    found = true;
                    break;
                case "h":
                    totalTime += value * 60 * 60;
                    found = true;
                    break;
                case "d":
                    totalTime += value * 60 * 60 * 24;
                    found = true;
                    break;
                case "w":
                    totalTime += value * 60 * 60 * 24 * 7;
                    found = true;
                    break;
                case "M":
                    totalTime += value * 60 * 60 * 24 * 30;
                    found = true;
                    break;
                case "y":
                    totalTime += value * 60 * 60 * 24 * 365;
                    found = true;
                    break;
            }
        }

        return !found ? -1 : totalTime * 1000;
    }
}
