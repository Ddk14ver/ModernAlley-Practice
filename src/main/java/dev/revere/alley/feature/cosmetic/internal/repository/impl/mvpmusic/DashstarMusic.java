package dev.revere.alley.feature.cosmetic.internal.repository.impl.mvpmusic;

import dev.revere.alley.feature.cosmetic.annotation.CosmeticData;
import dev.revere.alley.feature.cosmetic.model.CosmeticType;
import org.bukkit.Material;
/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 13/07/2026
 */
@CosmeticData(type = CosmeticType.MVP_MUSIC, name = "Dashstar", description = "Knock2 — Dashstar CS2 MVP music.", permission = "", icon = Material.MUSIC_DISC_RELIC, slot = 14, price = 800)
public class DashstarMusic extends BaseMVPMusic { public DashstarMusic() { super("alley.mvp.dashstar"); } }
