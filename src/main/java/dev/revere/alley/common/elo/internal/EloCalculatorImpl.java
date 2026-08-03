package dev.revere.alley.common.elo.internal;

import dev.revere.alley.bootstrap.annotation.Service;
import dev.revere.alley.common.elo.EloCalculator;
import dev.revere.alley.common.elo.EloRangeFactor;
import lombok.Getter;

/**
 * @author Remi
 * @project Alley
 * @date 6/2/2024
 */
@Getter
@Service(provides = EloCalculator.class, priority = 290)
public class EloCalculatorImpl implements EloCalculator {
    private final EloRangeFactor[] ELO_RANGES = {
            new EloRangeFactor(0, 1100, 25),
            new EloRangeFactor(1001, 1400, 20),
            new EloRangeFactor(1401, 1800, 15),
            new EloRangeFactor(1801, 2200, 10)
    };

    private final int DEFAULT_RANGE_FACTOR = 25;

    @Override
    public int determineNewElo(int playerElo, int opponentElo, boolean playerWon) {
        int score = playerWon ? 1 : 0;
        return this.calculateElo(playerElo, opponentElo, score);
    }

    @Override
    public int calculateElo(int playerElo, int opponentElo, int score) {
        double range = this.determineRange(playerElo);
        double expectedScore = this.calculateExpectedScore(playerElo, opponentElo);
        int updatedElo = (int) (playerElo + range * (score - expectedScore));

        if (score == 1 && updatedElo == playerElo) {
            updatedElo++;
        }
        return updatedElo;
    }

    /**
     * Determines the expected outcome for a player against an opponent based on Elo ratings.
     * 根据Elo评分确定玩家对阵对手的预期结果。
     * An expected score of 0.5 means the players are evenly matched.
     * 预期分数为0.5表示双方实力相当。
     *
     * @param playerElo   The player's current Elo rating.
     *                    玩家当前的Elo评分。
     * @param opponentElo The opponent's Elo rating.
     *                    对手的Elo评分。
     * @return The expected score for the player (a value between 0 and 1).
     *         玩家的预期分数（0到1之间的值）。
     */
    private double calculateExpectedScore(int playerElo, int opponentElo) {
        return 1.0 / (1.0 + Math.pow(10, (opponentElo - playerElo) / 400.0));
    }

    /**
     * Determines the K-factor (range factor) for the player's Elo rating.
     * 确定玩家Elo评分的K因子（范围因子）。
     *
     * @param elo The player's Elo rating.
     *            玩家的Elo评分。
     * @return The K-factor (range).
     *         K因子（范围）。
     */
    private double determineRange(int elo) {
        for (EloRangeFactor range : this.ELO_RANGES) {
            if (range.isInRange(elo)) {
                return range.getFactor();
            }
        }
        return this.DEFAULT_RANGE_FACTOR;
    }
}