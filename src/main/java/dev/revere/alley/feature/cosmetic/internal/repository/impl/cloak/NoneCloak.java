package dev.revere.alley.feature.cosmetic.internal.repository.impl.cloak;

import dev.revere.alley.feature.cosmetic.annotation.CosmeticData;
import dev.revere.alley.feature.cosmetic.model.CosmeticType;
import org.bukkit.Material;
import org.bukkit.entity.Player;

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
        name = "None",
        description = "Remove your cloak",
        icon = Material.BARRIER,
        slot = 10
)
public class NoneCloak extends BaseCloak{
    @Override
    public void render(Player player) {

    }
}
