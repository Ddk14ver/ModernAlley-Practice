package dev.revere.alley.feature.knockback.listener;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.feature.knockback.KnockbackManager;
import dev.revere.alley.feature.knockback.KnockbackProfile;
import dev.revere.alley.feature.knockback.data.PlayerKnockbackData;
import dev.revere.alley.feature.match.MatchService;
import dev.revere.alley.feature.match.combat.legacy.LegacyCombatService;
import dev.revere.alley.feature.match.internal.MatchServiceImpl;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 04/07/2026
 *
 * Controls splash potion trajectory and splash intensity per KB profile.
 */
public class PotionMotionListener implements Listener {
    private static final String MOTION_APPLIED_METADATA = "alley-potion-motion-applied";

    private final KnockbackManager manager;
    private final Set<UUID> customLaunchShooters = new HashSet<>();
    private Method projectileGetHandleMethod;
    private Method setPositionMethod;
    private Method playerGetHandleMethod;
    private Method getKnownMovementMethod;
    private Method levelMethod;
    private Method paperConfigMethod;
    private Method vectorXMethod;
    private Method vectorYMethod;
    private Method vectorZMethod;
    private Field miscField;
    private Field disableRelativeVelocityField;
    private boolean nmsOffsetWarningLogged;
    private boolean speedCompensationWarningLogged;

    public PotionMotionListener(KnockbackManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onLaunch(PlayerLaunchProjectileEvent event) {
        Projectile projectile = event.getProjectile();
        if (projectile.getType() != EntityType.SPLASH_POTION) return;
        Player player = event.getPlayer();
        Double compensationOverride = this.customLaunchShooters.contains(player.getUniqueId()) ? 0.0D : null;
        applyPotionMotion(projectile, player, compensationOverride);
    }

    public void beginCustomLaunch(Player player) {
        this.customLaunchShooters.add(player.getUniqueId());
    }

    public void endCustomLaunch(Player player) {
        this.customLaunchShooters.remove(player.getUniqueId());
    }

    public void applyCustomLaunch(Projectile projectile, Player player) {
        if (projectile == null || projectile.getType() != EntityType.SPLASH_POTION) return;
        applyPotionMotion(projectile, player, 0.0D);
    }

    private void applyPotionMotion(Projectile projectile, Player player, Double currentCompensationOverride) {
        if (projectile.hasMetadata(MOTION_APPLIED_METADATA)) return;

        PlayerKnockbackData data = manager.getPlayerData(player);
        KnockbackProfile profile = manager.getProfile(data.getProfileName());
        if (profile == null || !profile.isPotionEnabled()) return;

        projectile.setMetadata(MOTION_APPLIED_METADATA,
                new FixedMetadataValue(AlleyPlugin.getInstance(), true));
        Vector v = projectile.getVelocity();
        applySpeedCompensation(v, player, profile, currentCompensationOverride);
        applySpawnOffset(projectile, player, profile);
        projectile.setVelocity(new Vector(
                v.getX() * profile.getPotionHorizontalMult(),
                v.getY() * profile.getPotionVerticalMult(),
                v.getZ() * profile.getPotionHorizontalMult()
        ));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSplash(PotionSplashEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player player)) return;

        PlayerKnockbackData data = manager.getPlayerData(player);
        KnockbackProfile profile = manager.getProfile(data.getProfileName());
        if (profile == null || !profile.isPotionEnabled()) return;

        double intensity = event.getIntensity(player);
        event.setIntensity(player, Math.max(0.0, Math.min(1.0, intensity * profile.getPotionCompensationMultiplier())));
    }

    private void applySpeedCompensation(Vector velocity, Player player, KnockbackProfile profile,
                                        Double currentCompensationOverride) {
        try {
            Object handle = getPlayerHandle(player);
            double currentCompensation;
            if (currentCompensationOverride != null) {
                currentCompensation = currentCompensationOverride;
            } else {
                boolean globallyDisabled = isRelativeVelocityGloballyDisabled(handle);
                boolean legacyRemoved = !globallyDisabled && hasLegacySwordCombat(player);
                currentCompensation = globallyDisabled || legacyRemoved ? 0.0 : 1.0;
            }
            double adjustment = profile.getPotionSpeedCompensation() - currentCompensation;
            if (Math.abs(adjustment) < 1.0E-8) return;

            Object movement = getKnownMovement(handle);
            double x = ((Number) this.vectorXMethod.invoke(movement)).doubleValue();
            double y = ((Number) this.vectorYMethod.invoke(movement)).doubleValue();
            double z = ((Number) this.vectorZMethod.invoke(movement)).doubleValue();
            if (Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z)) return;

            velocity.setX(velocity.getX() + x * adjustment);
            velocity.setZ(velocity.getZ() + z * adjustment);
            if (!player.isOnGround()) {
                velocity.setY(velocity.getY() + y * adjustment);
            }
        } catch (ReflectiveOperationException exception) {
            if (!this.speedCompensationWarningLogged) {
                this.speedCompensationWarningLogged = true;
                AlleyPlugin.getInstance().getLogger().warning(
                        "Unable to adjust potion shooter-velocity compensation: "
                                + exception.getClass().getSimpleName());
            }
        }
    }

    private Object getPlayerHandle(Player player) throws ReflectiveOperationException {
        if (this.playerGetHandleMethod == null) {
            this.playerGetHandleMethod = player.getClass().getMethod("getHandle");
        }
        return this.playerGetHandleMethod.invoke(player);
    }

    private Object getKnownMovement(Object handle) throws ReflectiveOperationException {
        if (this.getKnownMovementMethod == null) {
            this.getKnownMovementMethod = handle.getClass().getMethod("getKnownMovement");
        }
        Object movement = this.getKnownMovementMethod.invoke(handle);
        if (this.vectorXMethod == null) {
            Class<?> vectorType = movement.getClass();
            this.vectorXMethod = vectorType.getMethod("x");
            this.vectorYMethod = vectorType.getMethod("y");
            this.vectorZMethod = vectorType.getMethod("z");
        }
        return movement;
    }

    private boolean isRelativeVelocityGloballyDisabled(Object handle) throws ReflectiveOperationException {
        if (this.levelMethod == null) {
            this.levelMethod = handle.getClass().getMethod("level");
        }
        Object level = this.levelMethod.invoke(handle);
        if (this.paperConfigMethod == null) {
            this.paperConfigMethod = level.getClass().getMethod("paperConfig");
        }
        Object worldConfig = this.paperConfigMethod.invoke(level);
        if (this.miscField == null) {
            this.miscField = worldConfig.getClass().getField("misc");
        }
        Object misc = this.miscField.get(worldConfig);
        if (this.disableRelativeVelocityField == null) {
            this.disableRelativeVelocityField = misc.getClass().getField("disableRelativeProjectileVelocity");
        }
        return this.disableRelativeVelocityField.getBoolean(misc);
    }

    private boolean hasLegacySwordCombat(Player player) {
        MatchService matchService = AlleyPlugin.getInstance().getService(MatchService.class);
        if (!(matchService instanceof MatchServiceImpl service)) return false;
        LegacyCombatService legacyCombatService = service.getLegacyCombatService();
        return legacyCombatService != null && legacyCombatService.hasSwordBlockKB(player.getUniqueId());
    }

    private void applySpawnOffset(Projectile projectile, Player player, KnockbackProfile profile) {
        double horizontalOffset = profile.getPotionHorizontalOffset();
        double horizontalCompensation = profile.getPotionHorizontalCompensation();
        double verticalOffset = profile.getPotionVerticalOffset();
        if (horizontalOffset == 0.0 && horizontalCompensation == 0.0 && verticalOffset == 0.0) return;

        double compensatedHorizontalOffset = horizontalOffset
                + getHorizontalMovementSpeed(player) * horizontalCompensation;

        Location location = projectile.getLocation();
        Vector direction = player.getLocation().getDirection().setY(0.0);
        if (direction.lengthSquared() > 1.0E-8) {
            location.add(direction.normalize().multiply(compensatedHorizontalOffset));
        }
        location.add(0.0, verticalOffset, 0.0);
        if (!setInitialPosition(projectile, location)) {
            AlleyPlugin.getInstance().getServer().getScheduler().runTask(
                    AlleyPlugin.getInstance(),
                    () -> {
                        if (projectile.isValid()) {
                            projectile.teleport(location);
                        }
                    }
            );
        }
    }

    private double getHorizontalMovementSpeed(Player player) {
        try {
            Object movement = getKnownMovement(getPlayerHandle(player));
            double x = ((Number) this.vectorXMethod.invoke(movement)).doubleValue();
            double z = ((Number) this.vectorZMethod.invoke(movement)).doubleValue();
            if (Double.isFinite(x) && Double.isFinite(z)) {
                return Math.hypot(x, z);
            }
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            // Bukkit velocity is a safe fallback if the NMS movement accessor changes.
        }

        Vector velocity = player.getVelocity();
        return Math.hypot(velocity.getX(), velocity.getZ());
    }

    private boolean setInitialPosition(Projectile projectile, Location location) {
        try {
            if (this.projectileGetHandleMethod == null) {
                this.projectileGetHandleMethod = projectile.getClass().getMethod("getHandle");
            }
            Object handle = this.projectileGetHandleMethod.invoke(projectile);
            if (this.setPositionMethod == null) {
                this.setPositionMethod = handle.getClass().getMethod(
                        "setPos", double.class, double.class, double.class);
            }
            this.setPositionMethod.invoke(handle, location.getX(), location.getY(), location.getZ());
            return true;
        } catch (ReflectiveOperationException exception) {
            if (!this.nmsOffsetWarningLogged) {
                this.nmsOffsetWarningLogged = true;
                AlleyPlugin.getInstance().getLogger().warning(
                        "Unable to set the potion spawn offset through NMS; using Bukkit teleport fallback: "
                                + exception.getClass().getSimpleName());
            }
            return false;
        }
    }
}
