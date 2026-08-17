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

    public static AABB fromBoundingBox(org.bukkit.util.BoundingBox box) {
        return new AABB(box.getMin(), box.getMax());
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
        if (minDist > maxDist) return null;

        double entry = minDist;
        double exit = maxDist;

        double direction = ray.direction.getX();
        double origin = ray.origin.getX();
        if (Math.abs(direction) < 1.0E-12D) {
            if (origin < min.getX() || origin > max.getX()) return null;
        } else {
            double first = (min.getX() - origin) / direction;
            double second = (max.getX() - origin) / direction;
            if (first > second) {
                double swap = first;
                first = second;
                second = swap;
            }
            entry = Math.max(entry, first);
            exit = Math.min(exit, second);
            if (entry > exit) return null;
        }

        direction = ray.direction.getY();
        origin = ray.origin.getY();
        if (Math.abs(direction) < 1.0E-12D) {
            if (origin < min.getY() || origin > max.getY()) return null;
        } else {
            double first = (min.getY() - origin) / direction;
            double second = (max.getY() - origin) / direction;
            if (first > second) {
                double swap = first;
                first = second;
                second = swap;
            }
            entry = Math.max(entry, first);
            exit = Math.min(exit, second);
            if (entry > exit) return null;
        }

        direction = ray.direction.getZ();
        origin = ray.origin.getZ();
        if (Math.abs(direction) < 1.0E-12D) {
            if (origin < min.getZ() || origin > max.getZ()) return null;
        } else {
            double first = (min.getZ() - origin) / direction;
            double second = (max.getZ() - origin) / direction;
            if (first > second) {
                double swap = first;
                first = second;
                second = swap;
            }
            entry = Math.max(entry, first);
            exit = Math.min(exit, second);
            if (entry > exit) return null;
        }

        return ray.getPointAtDistance(entry);
    }
}
