package dev.revere.alley.feature.kit.setting.listener;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.core.profile.enums.ProfileState;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.kit.setting.types.mechanic.KitSettingDisableSwimmingImpl;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleSwimEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class KitSettingSwimmingListener implements Listener {
    private final AlleyPlugin plugin;
    private final Set<UUID> monitoredPlayers = ConcurrentHashMap.newKeySet();
    private final Set<UUID> forcedStanding = ConcurrentHashMap.newKeySet();
    private BukkitTask enforcementTask;

    public KitSettingSwimmingListener(AlleyPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        this.enforcementTask = Bukkit.getScheduler().runTaskTimer(this.plugin, this::enforceStandingPose, 1L, 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onEntityToggleSwim(EntityToggleSwimEvent event) {
        if (!event.isSwimming() || !(event.getEntity() instanceof Player player)) {
            return;
        }

        if (this.shouldPreventSwimming(player)) {
            this.monitoredPlayers.add(player.getUniqueId());
            event.setCancelled(true);
            this.forceStanding(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uniqueId = player.getUniqueId();

        if (!this.shouldPreventSwimming(player)) {
            this.monitoredPlayers.remove(uniqueId);
            this.releaseStanding(player);
            return;
        }

        this.monitoredPlayers.add(uniqueId);
        if (player.isSwimming() || player.getPose() == Pose.SWIMMING) {
            this.forceStanding(player);
        }
    }

    private void enforceStandingPose() {
        for (UUID uniqueId : this.monitoredPlayers) {
            Player player = Bukkit.getPlayer(uniqueId);
            if (player == null || !this.shouldPreventSwimming(player)) {
                this.monitoredPlayers.remove(uniqueId);
                if (player != null) {
                    this.releaseStanding(player);
                }
                continue;
            }

            if (player.isSwimming() || player.getPose() == Pose.SWIMMING) {
                this.forceStanding(player);
            } else {
                this.releaseStanding(player);
            }
        }
    }

    private boolean shouldPreventSwimming(Player player) {
        Profile profile = this.plugin.getService(ProfileService.class).getProfile(player.getUniqueId());
        Kit kit = this.getActiveKit(profile);
        return kit != null && kit.isSettingEnabled(KitSettingDisableSwimmingImpl.class);
    }

    private Kit getActiveKit(Profile profile) {
        if (profile == null) {
            return null;
        }

        if (profile.getState() == ProfileState.PLAYING && profile.getMatch() != null) {
            return profile.getMatch().getKit();
        }
        if (profile.getState() == ProfileState.FFA && profile.getFfaMatch() != null) {
            return profile.getFfaMatch().getKit();
        }
        return null;
    }

    private void forceStanding(Player player) {
        player.setSwimming(false);
        player.setPose(Pose.STANDING, true);
        this.forcedStanding.add(player.getUniqueId());
    }

    private void releaseStanding(Player player) {
        if (this.forcedStanding.remove(player.getUniqueId())) {
            player.setPose(Pose.STANDING, false);
        }
    }
}
