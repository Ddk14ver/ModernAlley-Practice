package dev.revere.alley.feature.match.task.other;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.core.profile.enums.ProfileState;
import dev.revere.alley.feature.cooldown.Cooldown;
import dev.revere.alley.feature.cooldown.CooldownService;
import dev.revere.alley.feature.cooldown.CooldownType;
import dev.revere.alley.feature.kit.setting.types.mechanic.KitSettingPearlCooldownImpl;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Displays the ender pearl cooldown only while the player's current kit enables it.
 */
public class MatchPearlCooldownTask extends BukkitRunnable {
    @Override
    public void run() {
        CooldownService cooldownService = AlleyPlugin.getInstance().getService(CooldownService.class);
        ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);

        for (Player player : Bukkit.getOnlinePlayers()) {
            Profile profile = profileService.getProfile(player.getUniqueId());
            if (!usesPearlCooldown(profile)) {
                clearPearlCooldown(player, cooldownService);
                continue;
            }

            Cooldown cooldown = cooldownService.getCooldown(player.getUniqueId(), CooldownType.ENDER_PEARL);
            if (cooldown == null || !cooldown.isActive()) {
                cooldownService.removeCooldown(player.getUniqueId(), CooldownType.ENDER_PEARL);
                clearExperienceBar(player);
                continue;
            }

            long remainingMillis = cooldown.remainingTimeMillis();
            long totalDuration = cooldown.getDurationMillis();
            player.setLevel(cooldown.remainingTime());
            player.setExp(totalDuration > 0 ? (float) remainingMillis / totalDuration : 0.0F);
        }
    }

    private boolean usesPearlCooldown(Profile profile) {
        if (profile == null) {
            return false;
        }

        if (profile.getState() == ProfileState.PLAYING) {
            return profile.getMatch() != null
                    && hasPearlCooldown(profile.getMatch().getKit().getSetting(KitSettingPearlCooldownImpl.class));
        }

        if (profile.getState() == ProfileState.FFA) {
            return profile.getFfaMatch() != null
                    && hasPearlCooldown(profile.getFfaMatch().getKit().getSetting(KitSettingPearlCooldownImpl.class));
        }

        return false;
    }

    private boolean hasPearlCooldown(KitSettingPearlCooldownImpl setting) {
        return setting != null && setting.getValue() > 0;
    }

    private void clearPearlCooldown(Player player, CooldownService cooldownService) {
        Cooldown cooldown = cooldownService.getCooldown(player.getUniqueId(), CooldownType.ENDER_PEARL);
        if (cooldown != null) {
            cooldown.cancelCooldown();
            cooldownService.removeCooldown(player.getUniqueId(), CooldownType.ENDER_PEARL);
        }
        clearExperienceBar(player);
    }

    private void clearExperienceBar(Player player) {
        if (player.getLevel() != 0) {
            player.setLevel(0);
        }
        if (player.getExp() != 0.0F) {
            player.setExp(0.0F);
        }
    }
}
