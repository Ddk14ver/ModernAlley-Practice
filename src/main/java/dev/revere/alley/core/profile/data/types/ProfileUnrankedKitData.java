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
    private int bestWinstreak;
    private String monthlyPeriodKey;
    private int monthlyWins;

    public ProfileUnrankedKitData() {
        this.determineDivision(0);
        this.wins = 0;
        this.losses = 0;
        this.winstreak = 0;
        this.bestWinstreak = 0;
        this.monthlyPeriodKey = "";
        this.monthlyWins = 0;
    }

    public void incrementWins() {
        this.winstreak++;
        if (this.winstreak > this.bestWinstreak) {
            this.bestWinstreak = this.winstreak;
        }
        this.wins++;
    }

    public void incrementLosses() {
        this.winstreak = 0;
        this.losses++;
    }

    public void incrementMonthlyWins(String periodKey) {
        this.ensureMonthlyPeriod(periodKey);
        this.monthlyWins++;
    }

    public int getMonthlyWins(String periodKey) {
        this.ensureMonthlyPeriod(periodKey);
        return this.monthlyWins;
    }

    private void ensureMonthlyPeriod(String periodKey) {
        String currentPeriod = periodKey == null ? "" : periodKey;
        if (!currentPeriod.equals(this.monthlyPeriodKey)) {
            this.monthlyPeriodKey = currentPeriod;
            this.monthlyWins = 0;
        }
    }

    /**
     * Determines the division/tier this kit data belongs to based on the given total wins.
     * The total includes wins from every mode (unranked solo/duo, ranked, tournament) so that
     * all modes advance the same division progression.
     * 根据传入的总胜场数确定该套件所属的段位/层级。
     * 总胜场包含所有模式（非排位单打/双打、排位、锦标赛）的胜场，
     * 使所有模式的胜利都推进同一套段位进度。
     *
     * @param totalWins The sum of wins across all modes for this kit.
     *                  该套件在所有模式下的胜场之和。
     */
    public void determineDivision(int totalWins) {
        DivisionService divisionService = AlleyPlugin.getInstance().getService(DivisionService.class);
        for (Division division : divisionService.getDivisions()) {
            for (DivisionTier tier : division.getTiers()) {
                if (totalWins >= tier.getRequiredWins() && (this.division == null || !this.division.equals(division.getName()) || !Objects.equals(this.tier, tier.getName()))) {
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
