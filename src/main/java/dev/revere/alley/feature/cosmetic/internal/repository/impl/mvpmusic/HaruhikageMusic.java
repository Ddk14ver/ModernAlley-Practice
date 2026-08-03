package dev.revere.alley.feature.cosmetic.internal.repository.impl.mvpmusic;

import dev.revere.alley.feature.cosmetic.annotation.CosmeticData;
import dev.revere.alley.feature.cosmetic.model.CosmeticType;
import org.bukkit.Material;
/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 13/07/2026
 */
@CosmeticData(type = CosmeticType.MVP_MUSIC, name = "Haruhikage", description = "TOGENASHI TOGEARI — Haruhikage It's Mygo MVP music.", permission = "", icon = Material.MUSIC_DISC_MALL, slot = 13, price = 800)
public class HaruhikageMusic extends BaseMVPMusic { public HaruhikageMusic() { super("alley.mvp.haruhikage"); } }
