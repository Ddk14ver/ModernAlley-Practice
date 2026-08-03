package dev.revere.alley.core.profile.data.types;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.feature.division.Division;
import dev.revere.alley.feature.division.DivisionService;
import dev.revere.alley.feature.division.model.DivisionTier;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * @author Emmy
 * @project Alley
 * @since 25/01/2025
 */
@Getter
@Setter
public class ProfileUnrankedKitData {
    private String division;
    private String tier;
    private int wins;
    private int losses;
    private int winstreak;

    public ProfileUnrankedKitData() {
        this.determineDivision();
        this.wins = 0;
        this.losses = 0;
        this.winstreak = 0;
    }

    public void incrementWins() {
        this.winstreak++;
        this.wins++;
        this.determineDivision();
    }

    public void incrementLosses() {
        this.winstreak = 0;
        this.losses++;
    }

    public void determineDivision() {
        DivisionService divisionService = AlleyPlugin.getInstance().getService(DivisionService.class);
        for (Division division : divisionService.getDivisions()) {
            for (DivisionTier tier : division.getTiers()) {
                if (this.wins >= tier.getRequiredWins() && (this.division == null || !this.division.equals(division.getName()) || !Objects.equals(this.tier, tier.getName()))) {
                    this.division = division.getName();
                    this.tier = tier.getName();
                }
            }
        }
    }

    /**
     * Gets the division.
     * 获取段位。
     *
     * @return The division.
     *         段位。
     */
    public Division getDivision() {
        DivisionService divisionService = AlleyPlugin.getInstance().getService(DivisionService.class);
        return divisionService.getDivision(this.division);
    }

    /**
     * Gets the division tier.
     * 获取段位等级。
     *
     * @return The division tier.
     *         段位等级。
     */
    public DivisionTier getTier() {
        Division division = this.getDivision();
        return division.getTiers().stream()
                .filter(tier -> tier.getName().equals(this.tier))
                .findFirst()
                .orElse(null);
    }
}
