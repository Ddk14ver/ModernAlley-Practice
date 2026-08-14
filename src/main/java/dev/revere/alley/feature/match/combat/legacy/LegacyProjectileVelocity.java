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
    private final Map<Class<?>, Method> getHandleMethods = new HashMap<>();
    private final Map<Class<?>, Method> getKnownMovementMethods = new HashMap<>();
    private final Map<Class<?>, Method> levelMethods = new HashMap<>();
    private final Map<Class<?>, Method> paperConfigMethods = new HashMap<>();
    private final Map<Class<?>, Method> vectorXMethods = new HashMap<>();
    private final Map<Class<?>, Method> vectorYMethods = new HashMap<>();
    private final Map<Class<?>, Method> vectorZMethods = new HashMap<>();
    private final Map<Class<?>, Method> setDeltaMovementMethods = new HashMap<>();
    private final Map<Class<?>, Field> miscFields = new HashMap<>();
    private final Map<Class<?>, Field> disableRelativeVelocityFields = new HashMap<>();
    private final Map<Class<?>, Method> projectileGetHandleMethods = new HashMap<>();
    private boolean warningLogged;

    void removeInheritedVelocity(Projectile projectile, Player shooter) {
        try {
            Object handle = getHandle(shooter);
            if (isGloballyDisabled(handle)) return;

            Object movement = getKnownMovement(handle);
            Class<?> movementClass = movement.getClass();
            double x = ((Number) method(this.vectorXMethods, movementClass, "x").invoke(movement)).doubleValue();
            double y = ((Number) method(this.vectorYMethods, movementClass, "y").invoke(movement)).doubleValue();
            double z = ((Number) method(this.vectorZMethods, movementClass, "z").invoke(movement)).doubleValue();
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
        Method setDeltaMovementMethod = method(this.setDeltaMovementMethods, projectileHandle.getClass(),
                "setDeltaMovement", double.class, double.class, double.class);
        setDeltaMovementMethod.invoke(
                projectileHandle, velocity.getX(), velocity.getY(), velocity.getZ());
    }

    private Object getHandle(Player shooter) throws ReflectiveOperationException {
        return method(this.getHandleMethods, shooter.getClass(), "getHandle").invoke(shooter);
    }

    private Object getKnownMovement(Object handle) throws ReflectiveOperationException {
        return method(this.getKnownMovementMethods, handle.getClass(), "getKnownMovement").invoke(handle);
    }

    private boolean isGloballyDisabled(Object handle) throws ReflectiveOperationException {
        Object level = method(this.levelMethods, handle.getClass(), "level").invoke(handle);
        Object worldConfig = method(this.paperConfigMethods, level.getClass(), "paperConfig").invoke(level);
        Field miscField = field(this.miscFields, worldConfig.getClass(), "misc");
        Object misc = miscField.get(worldConfig);
        Field disabledField = field(this.disableRelativeVelocityFields, misc.getClass(),
                "disableRelativeProjectileVelocity");
        return disabledField.getBoolean(misc);
    }

    private Method method(Map<Class<?>, Method> methods, Class<?> type, String name,
                          Class<?>... parameterTypes) throws NoSuchMethodException {
        Method cached = methods.get(type);
        if (cached != null) return cached;
        Method resolved = type.getMethod(name, parameterTypes);
        methods.put(type, resolved);
        return resolved;
    }

    private Field field(Map<Class<?>, Field> fields, Class<?> type, String name)
            throws NoSuchFieldException {
        Field cached = fields.get(type);
        if (cached != null) return cached;
        Field resolved = type.getField(name);
        fields.put(type, resolved);
        return resolved;
    }

    private void logWarning(ReflectiveOperationException exception) {
        if (this.warningLogged) return;
        this.warningLogged = true;
        AlleyPlugin.getInstance().getLogger().warning(
                "Unable to remove modern projectile velocity inheritance: "
                        + exception.getClass().getSimpleName());
    }
}
