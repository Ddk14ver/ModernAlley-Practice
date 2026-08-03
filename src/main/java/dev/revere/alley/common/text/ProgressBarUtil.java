package dev.revere.alley.common.text;

import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;

/**
 * Progress bar generation utility class.
 * 进度条生成工具类。
 *
 * @author Emmy
 * @project Alley
 * @since 27/03/2025
 */
@UtilityClass
public class ProgressBarUtil {
    /**
     * Generates a progress bar based on the given values.
     * 根据给定的值生成进度条。
     *
     * @param current the current value
     *                当前值
     * @param maximum the max value
     *                最大值
     * @param length  the total number of bars (usually 20 or 40)
     *                进度条的总长度（通常为20或40）
     * @param symbol  the symbol to use for the progress bar (Default: ▎)
     *                进度条使用的符号（默认：▎）
     * @return the progress bar string with the given values.
     *         带有给定值的进度条字符串。
     */
    public String generate(int current, int maximum, int length, String symbol) {
        if (maximum <= 0) throw new IllegalArgumentException("Max value must be greater than zero.");

        ChatColor progressColor = ChatColor.GREEN;
        ChatColor pendingColor = ChatColor.GRAY;

        int progressBars = Math.round(length * ((float) current / maximum));
        StringBuilder bar = new StringBuilder();

        for (int i = 0; i < length; i++) {
            bar.append(i < progressBars ? progressColor : pendingColor).append(symbol);
        }

        return bar.toString();
    }
}