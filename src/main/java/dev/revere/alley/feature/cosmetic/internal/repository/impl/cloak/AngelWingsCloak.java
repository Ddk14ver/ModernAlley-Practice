package dev.revere.alley.feature.cosmetic.internal.repository.impl.cloak;

import dev.revere.alley.common.ParticleEffect;
import dev.revere.alley.feature.cosmetic.annotation.CosmeticData;
import dev.revere.alley.feature.cosmetic.model.CosmeticType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * @author Remi
 * @project alley-practice
 * @date 4/08/2025
 */
@CosmeticData(
        type = CosmeticType.CLOAK,
        name = "Angel Wings",
        description = "Display majestic angel wings behind your back.",
        icon = Material.FEATHER,
        slot = 11,
        price = 2000
)
public class AngelWingsCloak extends BaseCloak {
    @Override
    public void render(Player player) {
        Location baseLocation = player.getLocation();
        Vector direction = baseLocation.getDirection().normalize().multiply(-0.4);
        Location spawnCenter = baseLocation.add(direction).add(0, 1.4, 0);

        drawWing(spawnCenter, true);
        drawWing(spawnCenter, false);
    }

    private void drawWing(Location center, boolean isRightWing) {
        double wingScale = 1.2;
        int points = 25;

        for (int i = 0; i < points; i++) {
            double angle = Math.toRadians(180.0 / points * i);
            double x = Math.cos(angle) * wingScale;
            double y = Math.sin(angle) * wingScale;

            double finalX = isRightWing ? x : -x;
            Vector rotated = rotateAroundAxisY(new Vector(finalX, y, 0), center.getYaw());

            Location particleLoc = center.clone().add(rotated);

            ParticleEffect.FLAME.display(0F, 0F, 0F, 0F, 1, particleLoc, 20);

            if (i > points / 2) {
                ParticleEffect.FIREWORKS_SPARK.display(0F, 0F, 0F, 0F, 1, particleLoc, 20);
            }
        }

        for (int i = 0; i < points; i++) {
            double angle = Math.toRadians(180.0 / points * i);
            double x = Math.cos(angle) * (wingScale * 0.7);
            double y = Math.sin(angle) * (wingScale * 0.7);

            double finalX = isRightWing ? x : -x;
            Vector offset = new Vector(0, -0.5, -0.1);
            Vector rotated = rotateAroundAxisY(new Vector(finalX, y, 0).add(offset), center.getYaw());

            Location particleLoc = center.clone().add(rotated);

            ParticleEffect.FLAME.display(0F, 0F, 0F, 0F, 1, particleLoc, 20);
        }
    }
}
