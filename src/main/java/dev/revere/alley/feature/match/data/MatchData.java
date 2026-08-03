package dev.revere.alley.feature.match.data;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Emmy
 * @project Alley
 * @since 29/05/2025
 */
@Getter
@Setter
public abstract class MatchData {
    private final String kit;
    private final String arena;

    private final long creationTime;
    private boolean ranked;

    /**
     * Constructor for the AbstractMatchData class.
     * 抽象MatchData类的构造函数。
     *
     * @param kit   The kit used in the match.
     *              比赛中使用的工具包。
     * @param arena The arena where the match took place.
     *              比赛发生的竞技场。
     */
    public MatchData(String kit, String arena) {
        this.kit = kit;
        this.arena = arena;
        this.creationTime = System.currentTimeMillis();
        this.ranked = false;
    }
}