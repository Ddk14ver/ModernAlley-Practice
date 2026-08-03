package dev.revere.alley.feature.division.model;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Emmy
 * 作者：Emmy
 * @project Alley
 * 项目：Alley
 * @since 25/01/2025
 * 自：25/01/2025
 */
@Getter
@Setter
public class DivisionTier {
    private final String name;
    private int requiredWins;

    /**
     * Constructor for the DivisionTier class.
     * DivisionTier类的构造函数。
     *
     * @param name         The level of the division tier.
     *                     部门等级的级别。
     * @param requiredWins The required wins of the division tier.
     *                     部门等级所需的胜场数。
     */
    public DivisionTier(String name, int requiredWins) {
        this.name = name;
        this.requiredWins = requiredWins;
    }
}