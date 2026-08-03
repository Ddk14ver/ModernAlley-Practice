package dev.revere.alley.feature.cosmetic.internal.repository.impl.soundeffect;

import dev.revere.alley.feature.cosmetic.model.BaseCosmetic;
import dev.revere.alley.feature.cosmetic.model.CosmeticType;
import dev.revere.alley.feature.cosmetic.annotation.CosmeticData;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * @author Remi
 * 作者：Remi
 * @project Alley
 * 项目：Alley
 * @date 01/06/2024
 * 日期：2024年1月6日
 */
@CosmeticData(type = CosmeticType.SOUND_EFFECT, name = "None", description = "Remove your sound effect", icon = Material.BARRIER, slot = 10)
public class NoneSoundEffect extends BaseSoundEffect {
    @Override
    public void execute(Player player) {

    }
}
