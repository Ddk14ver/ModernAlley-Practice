package dev.revere.alley.feature.cosmetic.internal.repository.impl.killmessage;

import dev.revere.alley.feature.cosmetic.model.CosmeticType;
import dev.revere.alley.feature.cosmetic.annotation.CosmeticData;
import org.bukkit.Material;

/**
 * @author Remi
 * @project alley-practice
 * @date 27/06/2025
 */
@CosmeticData(
        type = CosmeticType.KILL_MESSAGE,
        name = "Nerd Messages",
        description = "Debug your opponents out of existence.",
        icon = Material.COMMAND_BLOCK,
        slot = 13,
        price = 750
)
public class NerdKillMessages extends KillMessagePack {
    @Override
    protected String getResourceFileName() {
        return "nerd_messages.yml";
    }
}
