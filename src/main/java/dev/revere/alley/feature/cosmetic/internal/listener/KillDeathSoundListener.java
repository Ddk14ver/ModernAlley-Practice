package dev.revere.alley.feature.cosmetic.internal.listener;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.feature.cosmetic.CosmeticService;
import dev.revere.alley.feature.cosmetic.internal.repository.SoundEffectRepository;
import dev.revere.alley.feature.cosmetic.internal.repository.impl.soundeffect.BaseSoundEffect;
import dev.revere.alley.feature.cosmetic.model.CosmeticType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 18/06/2026
 */
public class KillDeathSoundListener implements Listener {

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        playDeathSound(victim);
        if (killer != null) playKillSound(killer);
    }

    private void playKillSound(Player player) {
        BaseSoundEffect sound = getSelected(player);
        if (sound != null) sound.execute(player);
    }

    private void playDeathSound(Player player) {
        BaseSoundEffect sound = getSelected(player);
        if (sound != null) sound.executeDeath(player);
    }

    private BaseSoundEffect getSelected(Player player) {
        Profile profile = AlleyPlugin.getInstance().getService(ProfileService.class).getProfile(player.getUniqueId());
        if (profile == null) return null;

        String selected = profile.getProfileData().getCosmeticData().getSelected(CosmeticType.SOUND_EFFECT);
        if (selected == null || selected.equalsIgnoreCase("None")) return null;

        SoundEffectRepository repo = AlleyPlugin.getInstance().getService(CosmeticService.class)
                .getRepository(CosmeticType.SOUND_EFFECT, SoundEffectRepository.class);
        return repo != null ? repo.getCosmetic(selected) : null;
    }
}
