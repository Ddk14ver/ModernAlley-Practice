package dev.revere.alley.core.profile.progress;

import dev.revere.alley.common.text.ProgressBarUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author Remi
 * @project alley-practice
 * @date 2/07/2025
 * 玩家进度数据类，存储当前进度信息并提供进度条和百分比显示。
 * Player progress data class, storing current progress info and providing progress bar and percentage display.
 *
 * The bar and percentage are relative to the player's CURRENT tier floor: they measure how far the
 * player is between the current tier's required wins and the next tier's required wins, rather than
 * from zero absolute wins.
 * 进度条和百分比以玩家当前层级的门槛为起点：衡量玩家从当前层级所需胜场推进到下一层级所需胜场的进度，
 * 而不是从绝对 0 胜场开始。
 */
@Getter
@RequiredArgsConstructor
public class PlayerProgress {
    private final int currentWins;
    private final int currentTierWins;
    private final int winsForNextTier;
    private final String nextRankName;
    private final boolean isMaxRank;

    /**
     * Generates a visual progress bar string relative to the current tier floor.
     * 生成相对于当前层级门槛的可视化进度条字符串。
     *
     * @param length The desired length of the bar.
     *               进度条所需的长度。
     * @param symbol The character to use for the bar.
     *               进度条使用的字符。
     * @return A formatted progress bar.
     *         格式化后的进度条。
     */
    public String getProgressBar(int length, String symbol) {
        if (isMaxRank || winsForNextTier <= currentTierWins) {
            return ProgressBarUtil.generate(1, 1, length, symbol);
        }
        int progress = Math.max(0, currentWins - currentTierWins);
        int span = winsForNextTier - currentTierWins;
        return ProgressBarUtil.generate(progress, span, length, symbol);
    }

    /**
     * Gets the progress as a formatted percentage string, relative to the current tier floor.
     * 获取格式化的百分比进度字符串，相对于当前层级门槛。
     *
     * @return The progress as a formatted percentage string (e.g., "75%").
     *         格式化的百分比进度字符串（例如 "75%"）。
     */
    public String getProgressPercentage() {
        if (isMaxRank || winsForNextTier <= currentTierWins) {
            return "100%";
        }
        int progress = Math.max(0, currentWins - currentTierWins);
        int span = winsForNextTier - currentTierWins;
        return Math.round((float) progress / span * 100) + "%";
    }

    /**
     * Gets the number of additional wins required to reach the next tier.
     * 获取达到下一等级所需的额外胜场数。
     *
     * @return The number of additional wins required to reach the next tier.
     *         达到下一等级所需的额外胜场数。
     */
    public int getWinsRequired() {
        if (isMaxRank) return 0;
        return Math.max(0, winsForNextTier - currentWins);
    }

    /**
     * Gets the correct singular or plural form of "win" based on required wins.
     * 根据所需胜场数获取 "win" 或 "wins" 的正确单复数形式。
     *
     * @return The word "win" or "wins" based on the number of required wins.
     *         根据所需胜场数返回 "win" 或 "wins"。
     */
    public String getWinOrWins() {
        return getWinsRequired() == 1 ? "win" : "wins";
    }
}
