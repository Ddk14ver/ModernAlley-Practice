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
    private final double combatDistance;
    private final double movementSpeed;
    private final double aimSpeed;
    private final double aimError;
    private final int ping;
    private final boolean tryhard;
    private final boolean wTap;
    private final double wTapRate;
    private final int wTapReactionTimeMs;
    private final boolean blockHit;
    private final boolean strafe;
    private final boolean bow;
    private final boolean rod;
    private final boolean lava;
    private final int lavaTicks;
    private final boolean antiFire;
    private final double healHealth;

    private BotDifficultyProfile(String id, String displayName, Material icon, double cps,
                                 double maxReach, double swingRange, double minReach, double combatDistance,
                                 double movementSpeed, double aimSpeed, double aimError, int ping,
                                 boolean tryhard, boolean wTap, double wTapRate, int wTapReactionTimeMs,
                                 boolean blockHit, boolean strafe, boolean bow, boolean rod,
                                 boolean lava, int lavaTicks, boolean antiFire, double healHealth) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
        this.cps = cps;
        this.maxReach = maxReach;
        this.swingRange = swingRange;
        this.minReach = minReach;
        this.combatDistance = combatDistance;
        this.movementSpeed = movementSpeed;
        this.aimSpeed = aimSpeed;
        this.aimError = aimError;
        this.ping = ping;
        this.tryhard = tryhard;
        this.wTap = wTap;
        this.wTapRate = wTapRate;
        this.wTapReactionTimeMs = wTapReactionTimeMs;
        this.blockHit = blockHit;
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
        double configuredMaxReach = clamp(section.getDouble("maxreach", legacyReach), 1.0, 6.0);
        double configuredMinReach = clamp(section.getDouble("minreach", 1.35), 0.0, 5.0);
        double minimumAttackReach = Math.min(configuredMinReach, configuredMaxReach);
        double maximumAttackReach = Math.max(configuredMinReach, configuredMaxReach);
        return new BotDifficultyProfile(
                id,
                section.getString("display-name", id),
                icon == null ? Material.IRON_SWORD : icon,
                clamp(section.getDouble("cps", 10.0), 1.0, 20.0),
                maximumAttackReach,
                clamp(section.getDouble("swing-range", 4.2), 1.0, 8.0),
                minimumAttackReach,
                clamp(section.getDouble("combat-distance", minimumAttackReach), 0.0, 5.0),
                clamp(section.getDouble("movement-speed", 1.0D), 0.1D, 2.0D),
                clamp(section.getDouble("aim-speed", 22.0), 1.0, 180.0),
                clamp(section.getDouble("aim-error", 0.08), 0.0, 1.5),
                Math.max(0, section.getInt("ping", section.getInt("reaction-ticks", 3) * 50)),
                section.getBoolean("tryhard", true),
                section.getBoolean("w-tap", true),
                clamp(section.getDouble("w-tap-rate", 0.65D), 0.0D, 1.0D),
                Math.max(0, Math.min(1000, section.getInt("w-tap-reaction-time-ms", 120))),
                section.getBoolean("blockhit", false),
                section.getBoolean("strafe", true),
                section.getBoolean("bow", !easy),
                section.getBoolean("rod", !easy),
                section.getBoolean("lava", hard),
                Math.max(1, section.getInt("lava-ticks", 12)),
                section.getBoolean("antifire", !easy),
                clamp(section.getDouble("heal-health", 9.0), 0.0, 20.0)
        );
    }

    public static BotDifficultyProfile fromCustom(CustomBotProfile custom) {
        double configuredMaxReach = clamp(custom.getMaxReach(), 1.0D, 6.0D);
        double configuredMinReach = clamp(custom.getMinReach(), 0.0D, 5.0D);
        return new BotDifficultyProfile(
                "custom",
                custom.getName(),
                Material.PLAYER_HEAD,
                clamp(custom.getCps(), 1.0D, 20.0D),
                Math.max(configuredMinReach, configuredMaxReach),
                clamp(custom.getSwingRange(), 1.0D, 8.0D),
                Math.min(configuredMinReach, configuredMaxReach),
                clamp(custom.getCombatDistance(), 0.0D, 5.0D),
                clamp(custom.getMovementSpeed(), 0.1D, 2.0D),
                clamp(custom.getAimSpeed(), 1.0D, 180.0D),
                clamp(custom.getAimError(), 0.0D, 1.5D),
                Math.max(0, custom.getPing()),
                custom.isTryhard(),
                custom.isWTap(),
                clamp(custom.getWTapRate(), 0.0D, 1.0D),
                Math.max(0, Math.min(1000, custom.getWTapReactionTimeMs())),
                custom.isBlockHit(),
                custom.isStrafe(),
                custom.isBow(),
                custom.isRod(),
                custom.isLava(),
                Math.max(1, custom.getLavaTicks()),
                custom.isAntiFire(),
                clamp(custom.getHealHealth(), 0.0D, 20.0D)
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
