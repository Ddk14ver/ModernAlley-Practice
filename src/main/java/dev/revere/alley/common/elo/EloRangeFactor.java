package dev.revere.alley.common.elo;

import lombok.Getter;

/**
 * @author Remi
 * @project Alley
 * @date 6/2/2024
 */
@Getter
public class EloRangeFactor {
    private final int lowerBound;
    private final int upperBound;
    private final int factor;

    /**
     * Constructor for the EloRangeFactor class.
     * EloRangeFactor类的构造函数。
     *
     * @param lowerBound The lower bound of the range.
     *                   范围的下限。
     * @param upperBound The upper bound of the range.
     *                   范围的上限。
     * @param factor     The factor for the range.
     *                   范围的因子。
     */
    public EloRangeFactor(int lowerBound, int upperBound, int factor) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.factor = factor;
    }

    /**
     * Method to check if the elo is in range.
     * 检查elo是否在范围内的方法。
     *
     * @param elo The elo to check.
     *            要检查的elo值。
     * @return If the elo is in range.
     *         elo是否在范围内。
     */
    public boolean isInRange(int elo) {
        return elo >= this.lowerBound && elo <= this.upperBound;
    }
}