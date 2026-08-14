package dev.revere.alley.common.server.listener;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.core.profile.Profile;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
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
        // Block world-driven mob generation, while preserving explicit plugin,
        // command, spawner and spawn-egg/breeding sources.
        CreatureSpawnEvent.SpawnReason reason = event.getSpawnReason();
        if (reason == CreatureSpawnEvent.SpawnReason.NATURAL
                || reason == CreatureSpawnEvent.SpawnReason.CHUNK_GEN
                || reason == CreatureSpawnEvent.SpawnReason.VILLAGE_INVASION
                || reason == CreatureSpawnEvent.SpawnReason.PATROL
                || reason == CreatureSpawnEvent.SpawnReason.NETHER_PORTAL
                || reason == CreatureSpawnEvent.SpawnReason.JOCKEY
                || reason == CreatureSpawnEvent.SpawnReason.OCELOT_BABY
                || reason == CreatureSpawnEvent.SpawnReason.REINFORCEMENTS
                || reason == CreatureSpawnEvent.SpawnReason.RAID
                || reason == CreatureSpawnEvent.SpawnReason.TRAP) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onWeatherChange(WeatherChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    private void onWorldLoad(WorldLoadEvent event) {
        event.getWorld().setDifficulty(Difficulty.HARD);
        event.getWorld().setGameRule(GameRule.DO_MOB_SPAWNING, false);
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
