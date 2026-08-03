package dev.revere.alley.feature.cosmetic.internal.repository.impl.soundeffect;

import dev.revere.alley.feature.cosmetic.annotation.CosmeticData;
import dev.revere.alley.feature.cosmetic.model.CosmeticType;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 16/07/2026
 */
@CosmeticData(type = CosmeticType.SOUND_EFFECT, name = "Chain (Kill & Death)", description = "Chain break on kill, step on death.", permission = "", icon = Material.IRON_BLOCK, slot = 19, price = 500)
public class ChainSoundEffect extends BaseSoundEffect {
    @Override public void execute(Player p) { p.playSound(p.getLocation(), Sound.BLOCK_CHAIN_BREAK, 1f, 1f); }
    @Override public void executeDeath(Player p) { p.playSound(p.getLocation(), Sound.BLOCK_CHAIN_STEP, 1f, 1f); }
}
