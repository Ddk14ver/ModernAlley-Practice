package dev.revere.alley.feature.knockback.nms;

import dev.revere.alley.AlleyPlugin;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Applies server-side Legacy motion changes without scheduling a client velocity packet. */
public final class LegacyMotionBridge {
    private final Map<Class<?>, Method> getHandleMethods = new ConcurrentHashMap<>();
    private final Map<Class<?>, Method> setDeltaMovementMethods = new ConcurrentHashMap<>();
    private boolean warningLogged;

    public boolean setDeltaMovement(Player player, Vector velocity) {
        try {
            Method getHandle = getHandleMethods.computeIfAbsent(player.getClass(), type -> {
                try {
                    return type.getMethod("getHandle");
                } catch (NoSuchMethodException exception) {
                    throw new MotionReflectionException(exception);
                }
            });
            Object handle = getHandle.invoke(player);
            Method setDeltaMovement = setDeltaMovementMethods.computeIfAbsent(handle.getClass(), type -> {
                try {
                    return type.getMethod(
                            "setDeltaMovement", double.class, double.class, double.class);
                } catch (NoSuchMethodException exception) {
                    throw new MotionReflectionException(exception);
                }
            });
            setDeltaMovement.invoke(handle, velocity.getX(), velocity.getY(), velocity.getZ());
            return true;
        } catch (ReflectiveOperationException | MotionReflectionException exception) {
            logWarning(exception);
            return false;
        }
    }

    private void logWarning(Exception exception) {
        if (this.warningLogged) return;
        this.warningLogged = true;
        Throwable cause = exception instanceof MotionReflectionException && exception.getCause() != null
                ? exception.getCause() : exception;
        AlleyPlugin.getInstance().getLogger().warning(
                "Unable to apply silent Legacy attacker motion: "
                        + cause.getClass().getSimpleName());
    }

    private static final class MotionReflectionException extends RuntimeException {
        private MotionReflectionException(ReflectiveOperationException cause) {
            super(cause);
        }
    }
}
