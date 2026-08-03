package dev.revere.alley.feature.cosmetic.internal.repository.impl.cloak;

import dev.revere.alley.common.ParticleEffect;
import dev.revere.alley.feature.cosmetic.annotation.CosmeticData;
import dev.revere.alley.feature.cosmetic.model.CosmeticType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Remi
 * @author 雷米
 * @project alley-practice
 * @project alley-practice 项目
 * @date 4/08/2025
 * @date 2025年4月8日
 */
@CosmeticData(
        type = CosmeticType.CLOAK,
        name = "Halo",
        description = "A holy halo that appears above your head.",
        icon = Material.GOLDEN_HELMET,
        slot = 12,
        price = 1500
)
public class HaloCloak extends BaseCloak {
    private int step = 0;

    @Override
    public void render(Player player) {
        Location center = player.getLocation().add(0, 2.2, 0);
        step++;

        double radius = 0.5;
        int points = 25;

        for (int i = 0; i < points; i++) {
            double angle = (2 * Math.PI / points) * i;
            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;

            Location particleLoc = center.clone().add(x, 0, z);
            ParticleEffect.FLAME.display(0F, 0F, 0F, 0F, 1, particleLoc, 32.0);
        }

        if (step % 25 == 0) {
            double randomAngle = ThreadLocalRandom.current().nextDouble(2 * Math.PI);
            double x = Math.cos(randomAngle) * radius;
            double z = Math.sin(randomAngle) * radius;

            Location dripLoc = center.clone().add(x, 0, z);
            ParticleEffect.DRIP_LAVA.display(0F, 0F, 0F, 0F, 1, dripLoc, 32.0);
        }
    }
}
