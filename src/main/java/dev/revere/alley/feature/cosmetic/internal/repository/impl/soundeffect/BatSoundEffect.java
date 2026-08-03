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
@CosmeticData(type = CosmeticType.SOUND_EFFECT, name = "Bat (Kill & Death)", description = "Bat death on kill, takeoff on death.", permission = "", icon = Material.BAT_SPAWN_EGG, slot = 16, price = 500)
public class BatSoundEffect extends BaseSoundEffect {
    @Override public void execute(Player p) { p.playSound(p.getLocation(), Sound.ENTITY_BAT_DEATH, 1f, 1f); }
    @Override public void executeDeath(Player p) { p.playSound(p.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1f, 1f); }
}
