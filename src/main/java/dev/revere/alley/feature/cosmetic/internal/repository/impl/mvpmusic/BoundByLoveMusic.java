package dev.revere.alley.feature.cosmetic.internal.repository.impl.mvpmusic;

import dev.revere.alley.feature.cosmetic.annotation.CosmeticData;
import dev.revere.alley.feature.cosmetic.model.CosmeticType;
import org.bukkit.Material;
/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 18/06/2026
 *
 * Ciallo～(∠・ω< )⌒★
 */
@CosmeticData(type = CosmeticType.MVP_MUSIC, name = "Bound By Love", description = "Gal MVP music.", permission = "", icon = Material.MUSIC_DISC_CHIRP, slot = 19, price = 800)
public class BoundByLoveMusic extends BaseMVPMusic { public BoundByLoveMusic() { super("alley.mvp.boundbylove"); } }
