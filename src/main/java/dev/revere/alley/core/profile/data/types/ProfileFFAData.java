package dev.revere.alley.core.profile.data.types;

import lombok.Getter;
import lombok.Setter;

import java.text.DecimalFormat;

/**
 * @author Remi
 * @project Alley
 * @date 5/27/2024
 */
@Getter
@Setter
public class ProfileFFAData {
    private int kills = 0;
    private int deaths = 0;

    private int killstreak = 0;
    private int highestKillstreak = 0;

    public void incrementKills() {
        this.kills++;
    }

    public void incrementDeaths() {
        this.deaths++;
    }

    public void incrementKillstreak() {
        this.killstreak++;
        if (this.killstreak > this.highestKillstreak) {
            this.highestKillstreak = this.killstreak;
        }
    }

    public void resetKillstreak() {
        this.killstreak = 0;
    }

    /**
     * Get the kill/death ratio.
     * 获取击杀/死亡比率。
     *
     * @return the kill/death ratio
     *         击杀/死亡比率。
     */
    public String getKillDeathRatio() {
        if (this.deaths == 0) {
            return this.kills == 0 ? "0.0x" : String.format("%.1f", (double) this.kills) + "x";
        }

        double ratio = (double) this.kills / this.deaths;

        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        return decimalFormat.format(ratio) + "x";
    }
}
