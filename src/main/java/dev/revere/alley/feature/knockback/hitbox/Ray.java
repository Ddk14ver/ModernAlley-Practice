package dev.revere.alley.feature.knockback.hitbox;

import org.bukkit.util.Vector;
/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 04/07/2026
 *
 * Ray with origin and direction for AABB intersection.
 */
public class Ray {
    public final Vector origin;
    public final Vector direction;

    public Ray(Vector origin, Vector direction) {
        this.origin = origin.clone();
        this.direction = direction.clone().normalize();
    }

    public Vector getPointAtDistance(double distance) {
        return origin.clone().add(direction.clone().multiply(distance));
    }
}
