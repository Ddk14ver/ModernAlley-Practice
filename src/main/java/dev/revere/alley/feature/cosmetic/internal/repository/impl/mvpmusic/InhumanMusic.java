package dev.revere.alley.feature.cosmetic.internal.repository.impl.mvpmusic;

import dev.revere.alley.feature.cosmetic.annotation.CosmeticData;
import dev.revere.alley.feature.cosmetic.model.CosmeticType;
import org.bukkit.Material;
/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 13/07/2026
 */
@CosmeticData(type = CosmeticType.MVP_MUSIC, name = "Inhuman", description = "Inhuman CS MVP music.", permission = "", icon = Material.MUSIC_DISC_WARD, slot = 16, price = 800)
public class InhumanMusic extends BaseMVPMusic { public InhumanMusic() { super("alley.mvp.inhuman"); } }
