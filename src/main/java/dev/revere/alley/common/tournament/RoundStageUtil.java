package dev.revere.alley.common.tournament;

import lombok.experimental.UtilityClass;

/**
 * Utility class for tournament round stage names.
 * 锦标赛回合阶段名称的工具类。
 *
 * @author Remi
 * @project alley-practice
 * @date 8/08/2025
 */
@UtilityClass
public final class RoundStageUtil {
    /**
     * Converts remaining team count into a human-readable stage name.
     * 将剩余队伍数量转换为人类可读的阶段名称。
     *
     * @param teamsLeft Number of teams still in the bracket.
     *                  仍在赛程中的队伍数量。
     * @return Stage name like "Finals", "Semi-Finals", "Quarter-Finals", or "Round of X".
     *         阶段名称，如"Finals"、"Semi-Finals"、"Quarter-Finals"或"Round of X"。
     */
    public String getRoundStageName(int teamsLeft) {
        switch (teamsLeft) {
            case 2:
                return "Finals";
            case 4:
                return "Semi-Finals";
            case 8:
                return "Quarter-Finals";
            case 16:
                return "Round of 16";
            case 32:
                return "Round of 32";
            case 64:
                return "Round of 64";
            default:
                if (teamsLeft <= 1) return "Finals";
                return "Round of " + teamsLeft;
        }
    }
}
