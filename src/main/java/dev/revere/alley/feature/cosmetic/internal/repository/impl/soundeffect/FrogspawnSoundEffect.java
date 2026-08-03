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
@CosmeticData(type = CosmeticType.SOUND_EFFECT, name = "Frogspawn (Kill & Death)", description = "Frogspawn break on kill, frog lay spawn on death.", permission = "", icon = Material.FROGSPAWN, slot = 22, price = 500)
public class FrogspawnSoundEffect extends BaseSoundEffect {
    @Override public void execute(Player p) { p.playSound(p.getLocation(), Sound.BLOCK_FROGSPAWN_BREAK, 1f, 1f); }
    @Override public void executeDeath(Player p) { p.playSound(p.getLocation(), Sound.ENTITY_FROG_LAY_SPAWN, 1f, 1f); }
}
