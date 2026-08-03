package dev.revere.alley.feature.cosmetic.internal.repository.impl.mvpmusic;

import dev.revere.alley.feature.cosmetic.annotation.CosmeticData;
import dev.revere.alley.feature.cosmetic.model.CosmeticType;
import org.bukkit.Material;

@CosmeticData(
        type = CosmeticType.MVP_MUSIC,
        name = "None",
        description = "Disable your MVP music",
        icon = Material.BARRIER,
        slot = 10,
        price = 0
)
public class NoneMVPMusic extends BaseMVPMusic {
    public NoneMVPMusic() {
        super(null);
    }
}
