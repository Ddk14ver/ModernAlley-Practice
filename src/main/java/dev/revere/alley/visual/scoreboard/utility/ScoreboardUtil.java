package dev.revere.alley.visual.scoreboard.utility;

import dev.revere.alley.common.text.CC;
import dev.revere.alley.common.text.Symbol;
import lombok.experimental.UtilityClass;

/**
 * @author Emmy
 * @project Alley
 * @date 05/10/2024 - 11:05
 *
 * 计分板工具类，提供目标可视化和床状态可视化等辅助方法。
 */
@UtilityClass
public class ScoreboardUtil {
    /**
     * Visualizes the goals in a scoreboard format.
     * 以计分板格式可视化目标得分。
     *
     * @param currentGoals The current number of goals achieved.
     *                     当前已获得的目标数量。
     * @param maxGoals     The maximum number of goals to visualize.
     *                     要可视化的最大目标数量。
     * @return A string representation of the goals, with filled and empty indicators.
     *         目标得分的字符串表示，包含已填充和空的指示器。
     */
    public String visualizeGoals(int currentGoals, int maxGoals) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < maxGoals; i++) {
            if (i < currentGoals) {
                stringBuilder.append(CC.translate("&a■"));
            } else {
                stringBuilder.append(CC.translate("&7■"));
            }
        }

        return stringBuilder.toString();
    }

    /**
     * Visualizes the bed status in a scoreboard format.
     * 以计分板格式可视化床的状态。
     *
     * @param isBroken Whether the bed is broken or not.
     *                 床是否已被破坏。
     * @return A string representation of the bed status, with a tick for intact and an X for broken.
     *         床状态的字符串表示，完好时显示勾号，破损时显示叉号。
     */
    public String visualizeBed(boolean isBroken) {
        return CC.translate(isBroken ? "&c" + Symbol.X : "&a" + Symbol.TICK);
    }
}