package dev.revere.alley.feature.match.combat.legacy;

import dev.revere.alley.AlleyPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Projectile;
import org.bukkit.persistence.PersistentDataType;

/** Stores launch-time legacy projectile state until the projectile hits. */
public final class LegacyProjectileData {
    private static final NamespacedKey LEGACY_PROJECTILE = new NamespacedKey(AlleyPlugin.getInstance(), "legacy_projectile");
    private static final NamespacedKey PUNCH_LEVEL = new NamespacedKey(AlleyPlugin.getInstance(), "legacy_punch_level");
    private static final NamespacedKey POWER_LEVEL = new NamespacedKey(AlleyPlugin.getInstance(), "legacy_power_level");

    private LegacyProjectileData() {
    }

    public static void mark(Projectile projectile, int punchLevel) {
        projectile.getPersistentDataContainer().set(LEGACY_PROJECTILE, PersistentDataType.BYTE, (byte) 1);
        storeBowPunch(projectile, punchLevel);
    }

    /** Stores the launch-time Punch level for every bow arrow, legacy or modern. */
    public static void storeBowPunch(Projectile projectile, int punchLevel) {
        projectile.getPersistentDataContainer().set(PUNCH_LEVEL, PersistentDataType.INTEGER, Math.max(0, punchLevel));
    }

    public static void markArrow(Projectile arrow, int punchLevel, int powerLevel) {
        mark(arrow, punchLevel);
        arrow.getPersistentDataContainer().set(POWER_LEVEL, PersistentDataType.INTEGER, Math.max(0, powerLevel));
    }

    public static boolean isMarked(Projectile projectile) {
        return projectile.getPersistentDataContainer().has(LEGACY_PROJECTILE, PersistentDataType.BYTE);
    }

    public static int getPunchLevel(Projectile projectile) {
        Integer punchLevel = projectile.getPersistentDataContainer().get(PUNCH_LEVEL, PersistentDataType.INTEGER);
        return punchLevel == null ? 0 : punchLevel;
    }

    public static int getPowerLevel(Projectile arrow) {
        Integer powerLevel = arrow.getPersistentDataContainer().get(POWER_LEVEL, PersistentDataType.INTEGER);
        return powerLevel == null ? 0 : powerLevel;
    }
}
