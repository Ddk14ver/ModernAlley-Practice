package dev.revere.alley.feature.cosmetic.internal.repository.impl.cloak;

import dev.revere.alley.common.ParticleEffect;
import dev.revere.alley.feature.cosmetic.annotation.CosmeticData;
import dev.revere.alley.feature.cosmetic.model.CosmeticType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Remi
 * @project alley-practice
 * @date 4/08/2025
 */
@CosmeticData(
        type = CosmeticType.CLOAK,
        name = "Vail",
        description = "You're all harmless.",
        icon = Material.FLINT,
        slot = 13,
        price = 1500
)
public class VailCloak extends BaseCloak {
    @Override
    public void render(Player player) {
        Location center = player.getLocation().add(0, 1.0, 0);
        float playerYaw = player.getLocation().getYaw();
        int particleCount = 3;
        double radius = 0.7;
        double verticalSpread = 0.8;

        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (int i = 0; i < particleCount; i++) {
            double angle = random.nextDouble(Math.PI);
            double currentRadius = random.nextDouble(radius);

            double relativeX = Math.cos(angle) * currentRadius;
            double relativeZ = Math.sin(angle) * currentRadius + 0.2;
            double relativeY = random.nextDouble(-verticalSpread, verticalSpread);

            Vector offset = new Vector(relativeX, relativeY, relativeZ);

            rotateAroundAxisY(offset, playerYaw + 180);

            Location particleLocation = center.clone().add(offset);

            if (random.nextBoolean()) {
                ParticleEffect.SMOKE_NORMAL.display(0F, 0F, 0F, 0.1F, 1, particleLocation, 20);
            } else {
                ParticleEffect.SMOKE_LARGE.display(0F, 0F, 0F, 0F, 1, particleLocation, 20);
            }
        }
    }
}
