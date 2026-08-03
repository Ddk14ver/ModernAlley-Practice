package dev.revere.alley.common.elo;

import lombok.Getter;

/**
 * @author Emmy
 * @project Alley
 * @date 09/11/2024 - 15:24
 */
@Getter
public class EloResult {
    private final int newWinnerElo;
    private final int newLoserElo;

    /**
     * Constructor for the EloResult class.
     * EloResult类的构造函数。
     *
     * @param newWinnerElo The new elo of the winner.
     *                     获胜者的新elo值。
     * @param newLoserElo  The new elo of the loser.
     *                     失败者的新elo值。
     */
    public EloResult(int newWinnerElo, int newLoserElo) {
        this.newWinnerElo = newWinnerElo;
        this.newLoserElo = newLoserElo;
    }
}