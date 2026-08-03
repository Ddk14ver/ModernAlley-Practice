package dev.revere.alley.feature.level.data;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;

/**
 * @author Emmy
 * @project Alley
 * @since 22/04/2025
 */
@Getter
@Setter
public class LevelData {
    private final String name;
    private String displayName;

    private Material material;
    private int durability;

    private int minElo;
    private int maxElo;

    /**
     * Constructor for the LevelData class.
     * LevelData 类的构造函数。
     *
     * @param name        The name of the level.
     *                    等级的名称。
     * @param displayName The display name of the level.
     *                    等级的显示名称。
     * @param material    The material associated with this level.
     *                    与此等级关联的材质。
     * @param durability  The durability of the material.
     *                    材质的耐久度。
     * @param minElo      The minimum Elo rating for this level.
     *                    此等级的最低 Elo 评分。
     * @param maxElo      The maximum Elo rating for this level.
     *                    此等级的最高 Elo 评分。
     */
    public LevelData(String name, String displayName, Material material, int durability, int minElo, int maxElo) {
        this.name = name;
        this.displayName = displayName;
        this.material = material;
        this.durability = durability;
        this.minElo = minElo;
        this.maxElo = maxElo;
    }
}