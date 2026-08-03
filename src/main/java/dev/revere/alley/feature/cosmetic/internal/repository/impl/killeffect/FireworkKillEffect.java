package dev.revere.alley.feature.cosmetic.internal.repository.impl.killeffect;

import dev.revere.alley.feature.cosmetic.model.BaseCosmetic;
import dev.revere.alley.feature.cosmetic.model.CosmeticType;
import dev.revere.alley.feature.cosmetic.annotation.CosmeticData;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.stream.IntStream;

/**
 * @author Emmy
 *   作者: Emmy
 * @project Alley
 *   项目: Alley
 * @since 02/04/2025
 *   自: 02/04/2025
 */
@CosmeticData(type = CosmeticType.KILL_EFFECT, name = "Firework", description = "Spawn a firework at the opponent", permission = "firework", icon = Material.FIREWORK_ROCKET, slot = 14)
public class FireworkKillEffect extends BaseKillEffect {
    @Override
    public void execute(Player player) {
        IntStream.range(0, 3).forEach(i -> player.getWorld().spawnEntity(player.getLocation(), EntityType.FIREWORK_ROCKET));
    }
}