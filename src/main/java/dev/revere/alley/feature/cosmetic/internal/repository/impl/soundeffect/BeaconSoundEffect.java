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
@CosmeticData(type = CosmeticType.SOUND_EFFECT, name = "Beacon (Kill & Death)", description = "Beacon power on kill, deactivate on death.", permission = "", icon = Material.BEACON, slot = 15, price = 500)
public class BeaconSoundEffect extends BaseSoundEffect {
    @Override public void execute(Player p) { p.playSound(p.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1f, 1f); }
    @Override public void executeDeath(Player p) { p.playSound(p.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1f, 1f); }
}
