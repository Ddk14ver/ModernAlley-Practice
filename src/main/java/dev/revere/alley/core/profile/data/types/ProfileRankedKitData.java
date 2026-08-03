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

    public void incrementWins() {
        this.wins++;
    }

    public void incrementLosses() {
        this.losses++;
    }
}
