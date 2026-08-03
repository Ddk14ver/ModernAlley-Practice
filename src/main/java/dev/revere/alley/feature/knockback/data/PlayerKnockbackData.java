package dev.revere.alley.feature.knockback.data;

import org.bukkit.util.Vector;
/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 04/07/2026
 *
 * Per-player knockback state.
 */
public class PlayerKnockbackData {
    private Vector velocity;
    private long lastDamageTick;
    private double lastGroundY;
    private boolean onGround;
    private String profileName;
    private int configuredHitDelay = -1;
    private long lastAcceptedCombatHitTick = Long.MIN_VALUE;
    private long lastHazardDamageTick = Long.MIN_VALUE;
    private long lastFireHazardPreparationTick = Long.MIN_VALUE;
    private long lastPoisonHazardPreparationTick = Long.MIN_VALUE;
    private boolean serverSideHit;
    private Vector legacyResidualVelocity;
    private long legacyResidualTick;
    private boolean pendingLegacyResidual;
    private long pendingNativeProjectileVelocityTick = Long.MIN_VALUE;

    public Vector getVelocity() { return velocity; }
    public void setVelocity(Vector v) { this.velocity = v; }

    public long getLastDamageTick() { return lastDamageTick; }
    public void setLastDamageTick(long t) { this.lastDamageTick = t; }

    public double getLastGroundY() { return lastGroundY; }
    public void setLastGroundY(double y) { this.lastGroundY = y; }

    public boolean isOnGround() { return onGround; }
    public void setOnGround(boolean g) { this.onGround = g; }

    public String getProfileName() { return profileName; }
    public void setProfileName(String n) { this.profileName = n; }

    public int getConfiguredHitDelay() { return configuredHitDelay; }
    public void setConfiguredHitDelay(int hitDelay) { this.configuredHitDelay = hitDelay; }

    public long getLastAcceptedCombatHitTick() { return lastAcceptedCombatHitTick; }
    public void setLastAcceptedCombatHitTick(long tick) { this.lastAcceptedCombatHitTick = tick; }

    public long getLastHazardDamageTick() { return lastHazardDamageTick; }
    public void setLastHazardDamageTick(long tick) { this.lastHazardDamageTick = tick; }

    public long getLastFireHazardPreparationTick() { return lastFireHazardPreparationTick; }
    public void setLastFireHazardPreparationTick(long tick) { this.lastFireHazardPreparationTick = tick; }

    public long getLastPoisonHazardPreparationTick() { return lastPoisonHazardPreparationTick; }
    public void setLastPoisonHazardPreparationTick(long tick) { this.lastPoisonHazardPreparationTick = tick; }

    public boolean isServerSideHit() { return serverSideHit; }
    public void setServerSideHit(boolean s) { this.serverSideHit = s; }

    public Vector getLegacyResidualVelocity() { return legacyResidualVelocity; }
    public void setLegacyResidualVelocity(Vector velocity) { this.legacyResidualVelocity = velocity; }

    public long getLegacyResidualTick() { return legacyResidualTick; }
    public void setLegacyResidualTick(long tick) { this.legacyResidualTick = tick; }

    public boolean isPendingLegacyResidual() { return pendingLegacyResidual; }
    public void setPendingLegacyResidual(boolean pending) { this.pendingLegacyResidual = pending; }

    public long getPendingNativeProjectileVelocityTick() { return pendingNativeProjectileVelocityTick; }
    public void setPendingNativeProjectileVelocityTick(long tick) { this.pendingNativeProjectileVelocityTick = tick; }
    public void clearPendingNativeProjectileVelocity() { this.pendingNativeProjectileVelocityTick = Long.MIN_VALUE; }

    public void clearLegacyResidual() {
        this.legacyResidualVelocity = null;
        this.legacyResidualTick = 0L;
        this.pendingLegacyResidual = false;
        this.pendingNativeProjectileVelocityTick = Long.MIN_VALUE;
    }
}
