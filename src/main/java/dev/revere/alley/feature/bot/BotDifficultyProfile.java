package dev.revere.alley.feature.bot;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

@Getter
public final class BotDifficultyProfile {
    private final String id;
    private final String displayName;
    private final Material icon;
    private final double cps;
    private final double attackRange;
    private final double movementSpeed;
    private final int reactionTicks;
    private final double aimError;
    private final boolean wTap;
    private final boolean strafe;
    private final boolean bow;
    private final boolean rod;
    private final boolean lava;
    private final double healHealth;

    private BotDifficultyProfile(String id, String displayName, Material icon, double cps,
                                 double attackRange, double movementSpeed, int reactionTicks,
                                 double aimError, boolean wTap, boolean strafe, boolean bow,
                                 boolean rod, boolean lava, double healHealth) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
        this.cps = cps;
        this.attackRange = attackRange;
        this.movementSpeed = movementSpeed;
        this.reactionTicks = reactionTicks;
        this.aimError = aimError;
        this.wTap = wTap;
        this.strafe = strafe;
        this.bow = bow;
        this.rod = rod;
        this.lava = lava;
        this.healHealth = healHealth;
    }

    public static BotDifficultyProfile fromConfig(String id, ConfigurationSection section) {
        Material icon = Material.matchMaterial(section.getString("icon", "IRON_SWORD"));
        boolean easy = id.equalsIgnoreCase("easy");
        boolean hard = id.equalsIgnoreCase("hard");
        return new BotDifficultyProfile(
                id,
                section.getString("display-name", id),
                icon == null ? Material.IRON_SWORD : icon,
                clamp(section.getDouble("cps", 10.0), 1.0, 20.0),
                clamp(section.getDouble("attack-range", 3.0), 1.0, 4.5),
                clamp(section.getDouble("movement-speed", 1.0), 0.2, 2.0),
                Math.max(0, section.getInt("reaction-ticks", 3)),
                clamp(section.getDouble("aim-error", 0.08), 0.0, 1.5),
                section.getBoolean("w-tap", true),
                section.getBoolean("strafe", true),
                section.getBoolean("bow", !easy),
                section.getBoolean("rod", !easy),
                section.getBoolean("lava", hard),
                clamp(section.getDouble("heal-health", 9.0), 0.0, 20.0)
        );
    }

    private static double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
