package dev.revere.alley.common.text;

import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;

/**
 * A utility class for representing various elements with colors and styles.
 * 一个用于以颜色和样式表示各种元素的工具类。
 *
 * @author Emmy
 * @project alley-practice
 * @since 26/09/2025
 */
@UtilityClass
public class Representer {
    /**
     * Get a colored representation of a stage countdown.
     * 获取阶段倒计时的彩色表示。
     * Assumes stages are from 0 to 5, where 5 is the highest (green) and 0 is the lowest (red).
     * 假设阶段从0到5，其中5是最高的（绿色），0是最低的（红色）。
     *
     * @param stage the stage to represent (0-5)
     *              要表示的阶段（0-5）
     * @param bold  whether the representation should be bold or not.
     *              表示是否应该加粗。
     * @return the colored representation of the stage countdown.
     *         阶段倒计时的彩色表示。
     */
    public String colorizeCountdown(int stage, boolean bold) {
        ChatColor color;

        if (stage >= 5) {
            color = ChatColor.GREEN;
        } else if (stage == 4) {
            color = ChatColor.GREEN;
        } else if (stage == 3) {
            color = ChatColor.YELLOW;
        } else if (stage == 2) {
            color = ChatColor.YELLOW;
        } else {
            color = ChatColor.RED;
        }

        return CC.translate(color + (bold ? "&l" : "") + stage);
    }
}