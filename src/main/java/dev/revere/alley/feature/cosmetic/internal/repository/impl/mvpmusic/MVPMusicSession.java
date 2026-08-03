package dev.revere.alley.feature.cosmetic.internal.repository.impl.mvpmusic;

import dev.revere.alley.AlleyPlugin;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * EogdMusicPlayer-style music session: MVP sound playback is triggered
 * by {@link PlayerResourcePackStatusEvent} SUCCESSFULLY_LOADED, not in
 * the middle of the match-end teleport flow.  This guarantees the player
 * is settled in the lobby with a stable location before the sound fires.
 */
public final class MVPMusicSession implements Listener {
    private static final long PLAYBACK_TICKS = 140L;

    /** UUID → sound event name pending playback after pack loads. */
    static final Map<UUID, String> PENDING = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> ACTIVE_UNTIL = new ConcurrentHashMap<>();

    public static void play(Player player, String song) {
        if (player == null || song == null || !player.isOnline()) return;

        long delay = player.isDead() ? 3L : 0L;
        ACTIVE_UNTIL.put(player.getUniqueId(),
                System.currentTimeMillis() + (PLAYBACK_TICKS + delay) * 50L);

        Runnable playback = () -> {
            if (!player.isOnline()) return;
            player.stopSound(song, SoundCategory.RECORDS);
            player.playSound(player, song, SoundCategory.RECORDS, 1.0f, 1.0f);
        };

        if (delay == 0L) {
            playback.run();
        } else {
            AlleyPlugin.getInstance().getServer().getScheduler().runTaskLater(
                    AlleyPlugin.getInstance(), playback, delay);
        }
    }

    public static long getRemainingTicks(UUID playerId) {
        Long until = ACTIVE_UNTIL.get(playerId);
        if (until == null) return 0L;

        long remainingMillis = until - System.currentTimeMillis();
        if (remainingMillis <= 0L) {
            ACTIVE_UNTIL.remove(playerId, until);
            return 0L;
        }
        return Math.max(1L, (remainingMillis + 49L) / 50L);
    }

    /**
     * Schedules MVP music playback for a player.
     * The sound will play when the resource pack finishes loading
     * (or immediately if already loaded — see {link PackHandler}).
     */
    public static void schedule(UUID playerId, String soundEventName) {
        if (soundEventName != null) {
            PENDING.put(playerId, soundEventName);
        }
    }

    @EventHandler
    public void onPackStatus(PlayerResourcePackStatusEvent event) {
        if (event.getStatus() != PlayerResourcePackStatusEvent.Status.SUCCESSFULLY_LOADED) {
            return;
        }

        Player player = event.getPlayer();
        String song = PENDING.remove(player.getUniqueId());
        if (song == null) return;

        play(player, song);
    }
}
