package dev.revere.alley.feature.cosmetic.internal.repository.impl.mvpmusic;

import dev.revere.alley.feature.cosmetic.annotation.CosmeticData;
import dev.revere.alley.feature.cosmetic.model.CosmeticType;
import org.bukkit.Material;
/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 13/07/2026
 */
@CosmeticData(type = CosmeticType.MVP_MUSIC, name = "Flashbang", description = "Flashbang CS MVP music.", permission = "", icon = Material.MUSIC_DISC_WAIT, slot = 15, price = 800)
public class FlashbangMusic extends BaseMVPMusic { public FlashbangMusic() { super("alley.mvp.flashbang"); } }
