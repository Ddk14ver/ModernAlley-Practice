package dev.revere.alley.feature.cosmetic.task;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.PlayerUtil;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.feature.cosmetic.CosmeticListener;
import dev.revere.alley.feature.cosmetic.CosmeticService;
import dev.revere.alley.feature.cosmetic.internal.repository.CloakRepository;
import dev.revere.alley.feature.cosmetic.internal.repository.impl.cloak.BaseCloak;
import dev.revere.alley.feature.cosmetic.model.CosmeticType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author Remi
 * @project alley-practice
 * @date 4/08/2025
 */
public class CosmeticTask extends BukkitRunnable {

    private final AlleyPlugin plugin;
    private static final long CLOAK_STILL_DELAY = 1500;

    public CosmeticTask(AlleyPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        ProfileService profileService = plugin.getService(ProfileService.class);
        CosmeticService cosmeticService = plugin.getService(CosmeticService.class);
        if (profileService == null || cosmeticService == null) return;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (CosmeticListener.isPlayerStill(player, CLOAK_STILL_DELAY) && PlayerUtil.inLobby(player)) {
                Profile profile = profileService.getProfile(player.getUniqueId());
                if (profile == null) continue;

                String cloakName = profile.getProfileData().getCosmeticData().getSelected(CosmeticType.CLOAK);
                if (cloakName == null || cloakName.equalsIgnoreCase("None")) continue;

                CloakRepository repo = cosmeticService.getRepository(CosmeticType.CLOAK, CloakRepository.class);
                if (repo == null) continue;

                BaseCloak cloak = repo.getCosmetic(cloakName);
                if (cloak != null) {
                    cloak.render(player);
                }
            }
        }
    }
}