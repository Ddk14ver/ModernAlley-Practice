package dev.revere.alley.common.text;

import lombok.experimental.UtilityClass;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * Text formatting utility class.
 * 文本格式化工具类。
 *
 * @author Emmy
 * @project Alley
 * @since 21/04/2025
 */
@UtilityClass
public class TextFormatter {
    /**
     * Formats a Location object into a string representation.
     * 将Location对象格式化为字符串表示。
     *
     * @param location The Location object to format.
     *                 要格式化的Location对象。
     * @return A string in the format "X, Y, Z" or "Not set" if the location is null.
     *         格式为"X, Y, Z"的字符串，如果位置为null则返回"Not set"。
     */
    public String formatLocation(Location location) {
        if (location == null) return CC.translate("&cNot set");
        return location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ();
    }

    /**
     * Automatically centers a list of strings within the specified width,
     * 在指定宽度内自动将字符串列表居中，
     * taking into account visible characters only (color codes are ignored).
     * 仅考虑可见字符（颜色代码被忽略）。
     *
     * @param lines The list of strings to center.
     *              要居中的字符串列表。
     * @param width The width to center within.
     *              居中的宽度。
     * @return A list of centered strings.
     *         居中后的字符串列表。
     */
    public List<String> centerText(List<String> lines, int width) {
        List<String> centeredLines = new ArrayList<>();

        for (String line : lines) {
            line = line.trim();

            int visibleLength = stripColorCodes(line).length();
            int spaces = Math.max(0, (width - visibleLength) / 2);

            String centeredLine = repeat(spaces) + line + repeat(spaces);

            if (stripColorCodes(centeredLine).length() < width) {
                centeredLine += " ";
            }

            centeredLines.add(centeredLine);
        }

        return centeredLines;
    }

    /**
     * Repeats a space character a specified number of times.
     * 将空格字符重复指定的次数。
     *
     * @param times The number of times to repeat.
     *              重复的次数。
     * @return The resulting string.
     *         结果字符串。
     */
    private String repeat(int times) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < times; i++) {
            stringBuilder.append(" ");
        }
        return stringBuilder.toString();
    }

    /**
     * Removes color codes from a string for accurate length measurement.
     * 从字符串中移除颜色代码以便准确测量长度。
     *
     * @param text The input string.
     *             输入字符串。
     * @return The string without color codes.
     *         去除颜色代码后的字符串。
     */
    private String stripColorCodes(String text) {
        return text.replaceAll("(?i)§[0-9A-FK-OR]", "")
                .replaceAll("(?i)&[0-9A-FK-OR]", "");
    }
}
