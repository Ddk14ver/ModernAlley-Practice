package dev.revere.alley.feature.cosmetic.internal.repository.impl.killeffect;

import dev.revere.alley.feature.cosmetic.model.CosmeticType;
import dev.revere.alley.feature.cosmetic.annotation.CosmeticData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @author Remi
 * @project Alley
 * @date 01/06/2024
 */
@CosmeticData(type = CosmeticType.KILL_EFFECT, name = "Blood", description = "Spawn blood particles", permission = "blood", icon = Material.REDSTONE, slot = 12)
public class BloodKillEffect extends BaseKillEffect {
    @Override
    public void execute(Player victim) {
        Location deathLocation = victim.getLocation().clone().add(0.0, 1.0, 0.0);
        World world = deathLocation.getWorld();
        if (world == null) return;

        ItemStack redstone = new ItemStack(Material.REDSTONE);
        for (int i = 0; i < 3; i++) {
            world.spawnParticle(Particle.ITEM, deathLocation, 50,
                    0.5, 0.5, 0.5, 0.1, redstone);
            world.spawnParticle(Particle.BLOCK, deathLocation, 50,
                    0.5, 0.5, 0.5, 0.1, Material.REDSTONE_BLOCK.createBlockData());
        }
    }
}
