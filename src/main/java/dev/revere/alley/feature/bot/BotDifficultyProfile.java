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
    private final double maxReach;
    private final double swingRange;
    private final double minReach;
    private final double movementSpeed;
    private final double aimSpeed;
    private final double aimError;
    private final int ping;
    private final boolean wTap;
    private final boolean strafe;
    private final boolean bow;
    private final boolean rod;
    private final boolean lava;
    private final int lavaTicks;
    private final boolean antiFire;
    private final double healHealth;

    private BotDifficultyProfile(String id, String displayName, Material icon, double cps,
                                 double maxReach, double swingRange, double minReach,
                                 double movementSpeed, double aimSpeed, double aimError, int ping,
                                 boolean wTap, boolean strafe, boolean bow, boolean rod,
                                 boolean lava, int lavaTicks, boolean antiFire, double healHealth) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
        this.cps = cps;
        this.maxReach = maxReach;
        this.swingRange = swingRange;
        this.minReach = minReach;
        this.movementSpeed = movementSpeed;
        this.aimSpeed = aimSpeed;
        this.aimError = aimError;
        this.ping = ping;
        this.wTap = wTap;
        this.strafe = strafe;
        this.bow = bow;
        this.rod = rod;
        this.lava = lava;
        this.lavaTicks = lavaTicks;
        this.antiFire = antiFire;
        this.healHealth = healHealth;
    }

    public static BotDifficultyProfile fromConfig(String id, ConfigurationSection section) {
        Material icon = Material.matchMaterial(section.getString("icon", "IRON_SWORD"));
        boolean easy = id.equalsIgnoreCase("easy");
        boolean hard = id.equalsIgnoreCase("hard");
        double legacyReach = section.getDouble("attack-range", 3.0);
        // Difficulty changes combat decisions, not the player's movement physics.
        return new BotDifficultyProfile(
                id,
                section.getString("display-name", id),
                icon == null ? Material.IRON_SWORD : icon,
                clamp(section.getDouble("cps", 10.0), 1.0, 20.0),
                clamp(section.getDouble("maxreach", legacyReach), 1.0, 6.0),
                clamp(section.getDouble("swing-range", 4.2), 1.0, 8.0),
                clamp(section.getDouble("minreach", 1.35), 0.0, 5.0),
                1.0D,
                clamp(section.getDouble("aim-speed", 22.0), 1.0, 180.0),
                clamp(section.getDouble("aim-error", 0.08), 0.0, 1.5),
                Math.max(0, section.getInt("ping", section.getInt("reaction-ticks", 3) * 50)),
                section.getBoolean("w-tap", true),
                section.getBoolean("strafe", true),
                section.getBoolean("bow", !easy),
                section.getBoolean("rod", !easy),
                section.getBoolean("lava", hard),
                Math.max(1, section.getInt("lava-ticks", 12)),
                section.getBoolean("antifire", !easy),
                clamp(section.getDouble("heal-health", 9.0), 0.0, 20.0)
        );
    }

    public double getAttackRange() {
        return maxReach;
    }

    public int getReactionTicks() {
        return Math.max(0, (int) Math.ceil(ping / 50.0D));
    }

    private static double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
