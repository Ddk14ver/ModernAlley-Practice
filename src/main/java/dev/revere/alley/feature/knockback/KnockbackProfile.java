package dev.revere.alley.feature.knockback;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 05/07/2026
 *
 * KB profile model loaded from knockback/*.yml
 */
@Getter
public class KnockbackProfile {
    private final String name;
    private double horizontalGround, horizontalAir, horizontalSprintExtra;
    private double verticalGround, verticalAir, verticalSprintExtra;
    private int hitDelay;
    private double yLimit;
    private boolean stopSprint;
    private boolean projectileEnabled;
    private double projectileHorizontal, projectileVertical;
    private double projectileHorizontalMult, projectileVerticalMult;
    private boolean projectileDirectionOverride;
    private boolean disableDownwardKb;
    private boolean potionEnabled;
    private double potionHorizontalMult, potionVerticalMult;
    private double potionCompensationMultiplier;
    private double potionSpeedCompensation;
    private double potionHorizontalCompensation;
    private double potionHorizontalOffset, potionVerticalOffset;
    private boolean cooldownAffectsKb;
    private double entityInteractionRange;
    private boolean packetMisplaceEnabled;
    private double packetMisplaceDistance;
    private boolean packetDelayEnabled;
    private int packetDelayTicks;
    private double hitboxLength, hitboxHeight;
    private boolean legacyComboResidual;

    public KnockbackProfile(String name, FileConfiguration config) {
        this.name = name;
        reload(config);
    }

    public void reload(FileConfiguration config) {
        horizontalGround = config.getDouble("horizontal.ground", 0.35);
        horizontalAir = config.getDouble("horizontal.air", 0.35);
        horizontalSprintExtra = config.getDouble("horizontal.sprint_extra", 0.2);
        verticalGround = config.getDouble("vertical.ground", 0.36);
        verticalAir = config.getDouble("vertical.air", 0.36);
        verticalSprintExtra = config.getDouble("vertical.sprint_extra", 0.1);
        hitDelay = config.getInt("hit_delay", 20);
        yLimit = config.getDouble("y_limit", 3.0);
        disableDownwardKb = config.getBoolean("disable_downward_kb", false);
        stopSprint = config.getBoolean("stop_sprint", false);
        projectileEnabled = config.getBoolean("projectile.enabled", true);
        projectileHorizontal = config.getDouble("projectile.horizontal", 0.15);
        projectileVertical = config.getDouble("projectile.vertical", 0.15);
        projectileHorizontalMult = config.getDouble("projectile.horizontal_multiplier", 1.0);
        projectileVerticalMult = config.getDouble("projectile.vertical_multiplier", 1.0);
        projectileDirectionOverride = config.getBoolean("projectile.direction_override", false);
        potionEnabled = config.getBoolean("potion.enabled", false);
        potionHorizontalMult = config.getDouble("potion.horizontal_multiplier", 1.0);
        potionVerticalMult = config.getDouble("potion.vertical_multiplier", 1.0);
        potionCompensationMultiplier = config.getDouble("potion.compensation_multiplier", 1.0);
        double defaultPotionSpeedCompensation = name.equalsIgnoreCase("1_7")
                || name.equalsIgnoreCase("1_8") ? 0.0 : 1.0;
        potionSpeedCompensation = config.getDouble(
                "potion.speed_compensation", defaultPotionSpeedCompensation);
        potionHorizontalCompensation = config.getDouble("potion.horizontal_compensation", 0.0);
        potionHorizontalOffset = config.getDouble("potion.horizontal_offset", 0.0);
        potionVerticalOffset = config.getDouble("potion.vertical_offset", 0.0);
        cooldownAffectsKb = config.getBoolean("modern.cooldown_affects_kb", false);
        entityInteractionRange = config.getDouble("modern.entity_interaction_range", 3.0);
        packetMisplaceEnabled = config.getBoolean("packet.misplace.enabled", false);
        packetMisplaceDistance = config.getDouble("packet.misplace.distance", 0.1);
        packetDelayEnabled = config.getBoolean("packet.delay.enabled", false);
        packetDelayTicks = config.getInt("packet.delay.ticks", 2);
        hitboxLength = config.getDouble("hitbox.length", 0.6);
        hitboxHeight = config.getDouble("hitbox.height", 1.8);
        legacyComboResidual = config.getBoolean("legacy.combo_residual", false);
    }
}
