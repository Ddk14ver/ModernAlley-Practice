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
@CosmeticData(type = CosmeticType.SOUND_EFFECT, name = "Decorated Pot (Kill & Death)", description = "Pot shatter on kill, insert on death.", permission = "", icon = Material.DECORATED_POT, slot = 20, price = 500)
public class DecoratedPotSoundEffect extends BaseSoundEffect {
    @Override public void execute(Player p) { p.playSound(p.getLocation(), Sound.BLOCK_DECORATED_POT_SHATTER, 1f, 1f); }
    @Override public void executeDeath(Player p) { p.playSound(p.getLocation(), Sound.BLOCK_DECORATED_POT_INSERT, 1f, 1f); }
}
