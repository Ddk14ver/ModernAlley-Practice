package dev.revere.alley.feature.cosmetic.internal.repository.impl.mvpmusic;

import dev.revere.alley.feature.cosmetic.annotation.CosmeticData;
import dev.revere.alley.feature.cosmetic.model.CosmeticType;
import org.bukkit.Material;
/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 13/07/2026
 */
@CosmeticData(type = CosmeticType.MVP_MUSIC, name = "EZ4ENCE", description = "The Verkkars — EZ4ENCE CS MVP music.", permission = "", icon = Material.MUSIC_DISC_STRAD, slot = 12, price = 800)
public class EZ4ENCEMusic extends BaseMVPMusic { public EZ4ENCEMusic() { super("alley.mvp.ez4ence"); } }
