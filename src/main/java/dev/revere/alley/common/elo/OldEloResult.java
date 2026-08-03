package dev.revere.alley.common.elo;

import lombok.Getter;

/**
 * @author Emmy
 * @project Alley
 * @date 09/11/2024 - 15:23
 */
@Getter
public class OldEloResult {
    private final int oldWinnerElo;
    private final int oldLoserElo;

    /**
     * Constructor for the OldEloResult class.
     * OldEloResult类的构造函数。
     *
     * @param oldWinnerElo The old elo of the winner.
     *                     获胜者的旧elo值。
     * @param oldLoserElo  The old elo of the loser.
     *                     失败者的旧elo值。
     */
    public OldEloResult(int oldWinnerElo, int oldLoserElo) {
        this.oldWinnerElo = oldWinnerElo;
        this.oldLoserElo = oldLoserElo;
    }
}