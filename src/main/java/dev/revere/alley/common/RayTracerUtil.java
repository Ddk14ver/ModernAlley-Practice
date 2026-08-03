package dev.revere.alley.common;

import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.Vector;

/**
 * @author Emmy
 * @project Alley
 * @date 10/06/2024 - 20:50
 */
@UtilityClass
public class RayTracerUtil {
    /**
     * Ray trace from a location in a direction.
     * 从某个位置沿某个方向进行射线追踪。
     *
     * @param startLocation The location to start the ray trace from.
     *                      开始射线追踪的起始位置。
     * @param direction     The direction to ray trace.
     *                      射线追踪的方向。
     * @return The location of the ray trace.
     *         射线追踪到的位置。
     */
    public Location rayTrace(Location startLocation, Vector direction) {
        Location currentLocation = startLocation.clone();
        while (currentLocation.getBlock().getType() == Material.AIR) {
            currentLocation.add(direction);
        }
        return currentLocation;
    }
}