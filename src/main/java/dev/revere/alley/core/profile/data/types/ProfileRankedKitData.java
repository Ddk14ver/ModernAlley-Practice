package dev.revere.alley.core.profile.data.types;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Remi
 * @project Alley
 * @date 5/26/2024
 */
@Getter
@Setter
public class ProfileRankedKitData {
    private int elo = 1000;
    private int wins = 0;
    private int losses = 0;
    private int winstreak = 0;
    private int bestWinstreak = 0;

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
}
