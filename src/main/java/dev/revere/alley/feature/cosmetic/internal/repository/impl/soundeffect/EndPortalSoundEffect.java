package dev.revere.alley.feature.cosmetic.internal.repository.impl.soundeffect;

import dev.revere.alley.feature.cosmetic.annotation.CosmeticData;
import dev.revere.alley.feature.cosmetic.model.CosmeticType;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 15/07/2026
 */
@CosmeticData(type = CosmeticType.SOUND_EFFECT, name = "End Portal (Kill & Death)", description = "Portal spawn on kill, eye place on death.", permission = "", icon = Material.END_PORTAL_FRAME, slot = 21, price = 500)
public class EndPortalSoundEffect extends BaseSoundEffect {
    @Override public void execute(Player p) { p.playSound(p.getLocation(), Sound.BLOCK_END_PORTAL_SPAWN, 0.7f, 1f); }
    @Override public void executeDeath(Player p) { p.playSound(p.getLocation(), Sound.BLOCK_END_PORTAL_FRAME_FILL, 1f, 1f); }
}
