package dev.revere.alley.feature.division;

import dev.revere.alley.feature.division.model.DivisionTier;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;

import java.util.List;

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
public class Division {
    private final List<DivisionTier> tiers;
    private final String name;

    private String displayName;
    private String description;

    private int durability;

    private Material icon;

    /**
     * Constructor for the Division class.
     * Division类的构造函数。
     *
     * @param name        The name of the division.
     *                    部门的名称。
     * @param displayName The display name of the division.
     *                    部门的显示名称。
     * @param description The description of the division.
     *                    部门的描述。
     * @param durability  The durability of the division.
     *                    部门的耐久度。
     * @param icon        The icon of the division.
     *                    部门的图标。
     * @param tiers       The tiers of the division.
     *                    部门的等级列表。
     */
    public Division(String name, String displayName, String description, int durability, Material icon, List<DivisionTier> tiers) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.durability = durability;
        this.icon = icon;
        this.tiers = tiers;
    }

    /**
     * Gets the division tier by the name.
     * 根据名称获取部门等级。
     *
     * @param tier The name of the division tier.
     *             部门等级的名称。
     * @return The division tier.
     *         部门等级。
     */
    public String getTier(int tier) {
        if (tier < 0 || tier >= this.tiers.size()) {
            return null;
        }

        DivisionTier divisionTier = this.tiers.get(tier);
        return divisionTier.getName();
    }

    /**
     * Gets the division tier by the name.
     * 根据名称获取部门等级。
     *
     * @param tier The name of the division tier.
     *             部门等级的名称。
     * @return The division tier.
     *         部门等级。
     */
    public DivisionTier getTier(String tier) {
        for (DivisionTier divisionTier : this.tiers) {
            if (divisionTier.getName().equalsIgnoreCase(tier)) {
                return divisionTier;
            }
        }
        return null;
    }

    /**
     * Gets the wins of the last tier in the division.
     * 获取部门中最后一个等级所需的胜场数。
     *
     * @return The wins of the last tier.
     *         最后一个等级所需的胜场数。
     */
    public int getTotalWins() {
        return this.tiers.get(this.tiers.size() - 1).getRequiredWins();
    }
}