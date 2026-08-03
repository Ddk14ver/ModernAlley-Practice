package dev.revere.alley.feature.cosmetic.internal.repository.impl.killmessage;

import dev.revere.alley.feature.cosmetic.model.CosmeticType;
import dev.revere.alley.feature.cosmetic.annotation.CosmeticData;
import org.bukkit.Material;

/**
 * @author Remi
 * @author 作者 Remi
 * @project alley-practice
 * @project 项目 alley-practice
 * @date 27/06/2025
 * @date 日期 27/06/2025
 */
@CosmeticData(
        type = CosmeticType.KILL_MESSAGE,
        name = "Spigot Community Messages",
        description = "A dive into the spigot community.",
        icon = Material.GOLD_NUGGET,
        slot = 14,
        price = 750
)
public class SpigotCommunityKillMessages extends KillMessagePack {
    @Override
    protected String getResourceFileName() {
        return "spigot_community_messages.yml";
    }
}
