package dev.revere.alley.feature.knockback.hitbox;

import org.bukkit.util.Vector;
/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 04/07/2026
 *
 * Axis-Aligned Bounding Box with slab-method ray intersection.
 */
public class AABB {
    public Vector min, max;

    public AABB(Vector min, Vector max) {
        this.min = min.clone();
        this.max = max.clone();
    }

    /**
     * Create an AABB centered at origin with given length (X/Z) and height (Y).
     */
    public static AABB fromSize(double length, double height) {
        double half = length / 2.0;
        return new AABB(new Vector(-half, 0, -half), new Vector(half, height, half));
    }

    /**
     * Shift this AABB by a location vector.
     */
    public AABB translate(Vector v) {
        return new AABB(min.clone().add(v), max.clone().add(v));
    }

    /**
     * Ray vs AABB intersection using the slabs method.
     * Returns the intersection point, or null if no hit.
     */
    public Vector intersectsRay(Ray ray, float minDist, float maxDist) {
        double tmin, tmax, tymin, tymax, tzmin, tzmax;

        if (ray.direction.getX() >= 0) {
            tmin = (min.getX() - ray.origin.getX()) / ray.direction.getX();
            tmax = (max.getX() - ray.origin.getX()) / ray.direction.getX();
        } else {
            tmin = (max.getX() - ray.origin.getX()) / ray.direction.getX();
            tmax = (min.getX() - ray.origin.getX()) / ray.direction.getX();
        }

        if (ray.direction.getY() >= 0) {
            tymin = (min.getY() - ray.origin.getY()) / ray.direction.getY();
            tymax = (max.getY() - ray.origin.getY()) / ray.direction.getY();
        } else {
            tymin = (max.getY() - ray.origin.getY()) / ray.direction.getY();
            tymax = (min.getY() - ray.origin.getY()) / ray.direction.getY();
        }

        if (tmin > tymax || tymin > tmax) return null;
        if (tymin > tmin) tmin = tymin;
        if (tymax < tmax) tmax = tymax;

        if (ray.direction.getZ() >= 0) {
            tzmin = (min.getZ() - ray.origin.getZ()) / ray.direction.getZ();
            tzmax = (max.getZ() - ray.origin.getZ()) / ray.direction.getZ();
        } else {
            tzmin = (max.getZ() - ray.origin.getZ()) / ray.direction.getZ();
            tzmax = (min.getZ() - ray.origin.getZ()) / ray.direction.getZ();
        }

        if (tmin > tzmax || tzmin > tmax) return null;
        if (tzmin > tmin) tmin = tzmin;
        if (tzmax < tmax) tmax = tzmax;

        if (tmin < minDist && tmax < minDist) return null;
        if (tmin > maxDist && tmax > maxDist) return null;

        return ray.getPointAtDistance(tmin);
    }
}
