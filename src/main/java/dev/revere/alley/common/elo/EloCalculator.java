package dev.revere.alley.common.elo;

import dev.revere.alley.bootstrap.lifecycle.Service;

/**
 * @author Remi
 * @project alley-practice
 * @date 2/07/2025
 */
public interface EloCalculator extends Service {
    /**
     * Calculates the new Elo rating for a player based on a simple win/loss result.
     * 根据简单的胜负结果计算玩家的新Elo评分。
     *
     * @param playerElo   The player's current Elo rating.
     *                    玩家当前的Elo评分。
     * @param opponentElo The opponent's Elo rating.
     *                    对手的Elo评分。
     * @param playerWon   True if the player won the match, false otherwise.
     *                    如果玩家赢得比赛则为true，否则为false。
     * @return The player's updated Elo rating.
     *         玩家更新后的Elo评分。
     */
    int determineNewElo(int playerElo, int opponentElo, boolean playerWon);

    /**
     * Calculates the new Elo rating for a player based on a given match score.
     * 根据给定的比赛分数计算玩家的新Elo评分。
     *
     * @param playerElo   The player's current Elo rating.
     *                    玩家当前的Elo评分。
     * @param opponentElo The opponent's Elo rating.
     *                    对手的Elo评分。
     * @param score       The score of the player (typically 1 for a win, 0 for a loss).
     *                    玩家的分数（通常胜利为1，失败为0）。
     * @return The updated Elo rating.
     *         更新后的Elo评分。
     */
    int calculateElo(int playerElo, int opponentElo, int score);
}