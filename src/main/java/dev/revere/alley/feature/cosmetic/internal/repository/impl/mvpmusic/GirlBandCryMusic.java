package dev.revere.alley.feature.cosmetic.internal.repository.impl.mvpmusic;

import dev.revere.alley.feature.cosmetic.annotation.CosmeticData;
import dev.revere.alley.feature.cosmetic.model.CosmeticType;
import org.bukkit.Material;
/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 13/07/2026
 */
@CosmeticData(type = CosmeticType.MVP_MUSIC, name = "Girls Band Cry", description = "Girls Band Cry MVP music.", permission = "", icon = Material.MUSIC_DISC_OTHERSIDE, slot = 20, price = 800)
public class GirlBandCryMusic extends BaseMVPMusic { public GirlBandCryMusic() { super("alley.mvp.girlbandcry"); } }
