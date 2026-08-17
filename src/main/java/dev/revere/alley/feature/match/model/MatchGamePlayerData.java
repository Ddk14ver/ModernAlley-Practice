package dev.revere.alley.feature.match.model;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.core.locale.LocaleService;
import dev.revere.alley.core.locale.internal.impl.SettingsLocaleImpl;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Remi
 * @project Alley
 * @date 5/27/2024
 */
@Getter
@Setter
public class MatchGamePlayerData {
    /**
     * Max milliseconds between releasing W (sprint-stop) and landing a melee hit for it to
     * still count as a W-tap attempt. W-taps are executed a tick or two before the hit.
     */
    private static final long WTAP_WINDOW_MS = 350L;

    private int lives;
    private int score;
    private int kills;
    private int deaths;

    private int longestCombo;
    private int combo;

    private int hits;
    private int criticalHits;
    private int blockedHits;

    private int missedPotions;
    private int thrownPotions;

    private int wTapAttempts;
    private int wTapSuccesses;
    private long lastSprintStopMillis;

    /** Amount of health naturally regenerated during the match (RegainReason.REGEN). */
    private double regen;

    private BaseRaiderRole role;

    public MatchGamePlayerData() {
        this.lives = AlleyPlugin.getInstance().getService(LocaleService.class).getInt(SettingsLocaleImpl.GAME_LIVES_PER_MATCH);
        this.score = 0;
    }

    /**
     * Method to handle an attack.
     * 处理攻击的方法。
     */
    public void handleAttack() {
        this.hits++;
        this.combo++;

        if (this.combo > this.longestCombo) {
            this.longestCombo = this.combo;
        }
    }

    /**
     * Records a sprint-toggle event (PlayerToggleSprintEvent).
     * When the player stops sprinting (releases W) we timestamp it — the start of a potential
     * W-tap. Called with the resulting sprint state, so {@code false} means "just stopped sprinting".
     */
    public void onSprintToggle(boolean sprintingNow) {
        if (!sprintingNow) {
            this.lastSprintStopMillis = System.currentTimeMillis();
        }
    }

    /**
     * Called on each melee hit. A W-tap attempt is a sprint-stop that happened shortly before
     * the hit; it succeeds when the hit lands while still not sprinting (sprint fully reset),
     * matching the reference PotPvP wTapAllHits/wTapSuccessHits model.
     */
    public void handleWTap(boolean sprintingNow) {
        long msSinceSprintStop = System.currentTimeMillis() - this.lastSprintStopMillis;
        if (msSinceSprintStop < 0 || msSinceSprintStop > WTAP_WINDOW_MS) {
            return;
        }
        this.wTapAttempts++;
        if (!sprintingNow) {
            this.wTapSuccesses++;
        }
    }

    /** Records the packet-ordered STOP -> START -> accepted-hit Legacy cycle. */
    public void handleLegacyWTap(boolean attempt, boolean success) {
        if (!attempt) return;
        this.wTapAttempts++;
        if (success) this.wTapSuccesses++;
    }

    /**
     * Method to reset the combo.
     * 重置连击的方法。
     */
    public void resetCombo() {
        this.combo = 0;
    }

    public void incrementScore() {
        this.score++;
    }

    public void incrementKills() {
        this.kills++;
    }

    public void incrementDeaths() {
        this.deaths++;
    }

    public void incrementMissedPotions() {
        this.missedPotions++;
    }

    public void incrementThrownPotions() {
        this.thrownPotions++;
    }

    public void incrementCriticalHits() {
        this.criticalHits++;
    }

    public void incrementBlockedHits() {
        this.blockedHits++;
    }

    /** Adds naturally regenerated health (in health points). */
    public void addRegen(double amount) {
        this.regen += amount;
    }

    /**
     * W-tap rate as a percentage of all W-tap attempts (successful / attempts), matching the
     * reference PotPvP calcWTap calculation. Returns 0 when there were no attempts.
     * W-tap 命中率 = 成功 W-tap 次数 / W-tap 尝试次数；无尝试时返回 0。
     */
    public int getWTapPercentage() {
        if (this.wTapAttempts <= 0) return 0;
        return Math.round(100.0F * this.wTapSuccesses / this.wTapAttempts);
    }
}
