package dev.revere.alley.feature.match.combat.legacy;

import dev.revere.alley.AlleyPlugin;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/** Removes the modern shooter-motion inheritance from legacy projectiles. */
final class LegacyProjectileVelocity {
    private Method getHandleMethod;
    private Method getKnownMovementMethod;
    private Method levelMethod;
    private Method paperConfigMethod;
    private Method vectorXMethod;
    private Method vectorYMethod;
    private Method vectorZMethod;
    private Method setDeltaMovementMethod;
    private Field miscField;
    private Field disableRelativeVelocityField;
    private final Map<Class<?>, Method> projectileGetHandleMethods = new HashMap<>();
    private boolean warningLogged;

    void removeInheritedVelocity(Projectile projectile, Player shooter) {
        try {
            Object handle = getHandle(shooter);
            if (isGloballyDisabled(handle)) return;

            Object movement = getKnownMovement(handle);
            double x = ((Number) this.vectorXMethod.invoke(movement)).doubleValue();
            double y = ((Number) this.vectorYMethod.invoke(movement)).doubleValue();
            double z = ((Number) this.vectorZMethod.invoke(movement)).doubleValue();
            if (Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z)) return;

            Vector velocity = projectile.getVelocity();
            velocity.setX(velocity.getX() - x);
            velocity.setZ(velocity.getZ() - z);
            if (!shooter.isOnGround()) {
                velocity.setY(velocity.getY() - y);
            }
            setDeltaMovement(projectile, velocity);
        } catch (ReflectiveOperationException exception) {
            logWarning(exception);
        }
    }

    private void setDeltaMovement(Projectile projectile, Vector velocity) throws ReflectiveOperationException {
        Class<?> projectileClass = projectile.getClass();
        Method projectileGetHandleMethod = this.projectileGetHandleMethods.get(projectileClass);
        if (projectileGetHandleMethod == null) {
            projectileGetHandleMethod = projectileClass.getMethod("getHandle");
            this.projectileGetHandleMethods.put(projectileClass, projectileGetHandleMethod);
        }
        Object projectileHandle = projectileGetHandleMethod.invoke(projectile);
        if (this.setDeltaMovementMethod == null) {
            this.setDeltaMovementMethod = projectileHandle.getClass().getMethod(
                    "setDeltaMovement", double.class, double.class, double.class);
        }
        this.setDeltaMovementMethod.invoke(
                projectileHandle, velocity.getX(), velocity.getY(), velocity.getZ());
    }

    private Object getHandle(Player shooter) throws ReflectiveOperationException {
        if (this.getHandleMethod == null) {
            this.getHandleMethod = shooter.getClass().getMethod("getHandle");
        }
        return this.getHandleMethod.invoke(shooter);
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

    private boolean isGloballyDisabled(Object handle) throws ReflectiveOperationException {
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

    private void logWarning(ReflectiveOperationException exception) {
        if (this.warningLogged) return;
        this.warningLogged = true;
        AlleyPlugin.getInstance().getLogger().warning(
                "Unable to remove modern projectile velocity inheritance: "
                        + exception.getClass().getSimpleName());
    }
}
