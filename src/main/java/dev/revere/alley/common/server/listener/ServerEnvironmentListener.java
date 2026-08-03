package dev.revere.alley.common.server.listener;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.core.profile.Profile;
import org.bukkit.Difficulty;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;

/**
 * Server environment listener for handling world events.
 * 服务器环境监听器，用于处理世界事件。
 *
 * @author Emmy
 * @project Alley
 * @date 15/09/2024 - 19:23
 */
public class ServerEnvironmentListener implements Listener {

    @EventHandler
    private void onUnloadChunk(ChunkUnloadEvent event) {
        // ChunkUnloadEvent is no longer cancellable in Paper 1.21
        // ChunkUnloadEvent在Paper 1.21中不再可取消
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onPrime(ExplosionPrimeEvent event) {
        if (event.getEntityType() == EntityType.END_CRYSTAL) {
            event.setRadius(6.0F);
        }
    }

    @EventHandler
    private void onBlockBurn(BlockBurnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    private void onBlockSpread(BlockSpreadEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    private void onLeavesDecay(LeavesDecayEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    private void onBlockIgnite(BlockIgniteEvent event) {
        if (event.getCause() == BlockIgniteEvent.IgniteCause.LIGHTNING) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getEntityType() == EntityType.ENDERMITE
                && event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.ENDER_PEARL) {
            event.setCancelled(true);
            return;
        }
        if (event.getEntityType() == EntityType.CHICKEN
                && (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.EGG
                || event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.DISPENSE_EGG)) {
            event.setCancelled(true);
            return;
        }

        // Only block natural spawning — allow plugin-triggered spawns
        // (ender crystals, kill-effect entities, etc.)
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NATURAL
                || event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CHUNK_GEN
                || event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.VILLAGE_INVASION
                || event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.PATROL
                || event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SLIME_SPLIT
                || event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.NETHER_PORTAL
                || event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.JOCKEY
                || event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.BEEHIVE
                || event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.DROWNED
                || event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SILVERFISH_BLOCK
                || event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.TRIAL_SPAWNER
                || event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.OCELOT_BABY
                || event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.REINFORCEMENTS) {
            event.setCancelled(true);
        }
        // SpawnReason.CUSTOM, DEFAULT, SPAWNER_EGG, COMMAND, etc. are allowed
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onEggThrow(PlayerEggThrowEvent event) {
        event.setHatching(false);
        event.setNumHatches((byte) 0);
    }

    @EventHandler
    private void onWeatherChange(WeatherChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    private void onWorldLoad(WorldLoadEvent event) {
        event.getWorld().getEntities().clear();
        event.getWorld().setDifficulty(Difficulty.HARD);
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if (event.getChangedType() == Material.GRASS_BLOCK && event.getBlock().getType() == Material.DIRT) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onWorldTeleport(PlayerTeleportEvent event) {
        if (event.getFrom().getWorld() != event.getTo().getWorld()) {
            Player player = event.getPlayer();
            Profile profile = AlleyPlugin.getInstance().getService(ProfileService.class).getProfile(player.getUniqueId());
            profile.getProfileData().getSettingData().setTimeBasedOnProfileSetting(player);
        }
    }
}
