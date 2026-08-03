package dev.revere.alley.feature.cosmetic.internal.repository.impl.mvpmusic;

import dev.revere.alley.feature.cosmetic.annotation.CosmeticData;
import dev.revere.alley.feature.cosmetic.model.CosmeticType;
import org.bukkit.Material;
/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 13/07/2026
 */

@CosmeticData(type = CosmeticType.MVP_MUSIC, name = "On My Own", description = "Blitz kids — On My Own CS MVP music.", permission = "", icon = Material.MUSIC_DISC_PIGSTEP, slot = 11, price = 800)
public class OnMyOwnMusic extends BaseMVPMusic { public OnMyOwnMusic() { super("alley.mvp.onmyown"); } }
