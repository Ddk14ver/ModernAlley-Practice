package dev.revere.alley.feature.cosmetic.preview;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.feature.bot.entity.NativeBotPlayer;
import dev.revere.alley.feature.cosmetic.internal.repository.impl.killeffect.BaseKillEffect;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

final class NativeKillEffectPreview {
    private static final Map<UUID, Session> SESSIONS = new ConcurrentHashMap<>();

    private NativeKillEffectPreview() {
    }

    static boolean start(Player viewer, BaseKillEffect effect, Location location, Runnable completion) {
        if (!NativeBotPlayer.isSupported()) return false;

        NativeBotPlayer dummy;
        try {
            dummy = NativeBotPlayer.spawn(location, "PreviewDummy", 0);
        } catch (RuntimeException exception) {
            return false;
        }
        Player dummyPlayer = dummy.player();
        dummyPlayer.setInvulnerable(true);

        UUID viewerId = viewer.getUniqueId();
        BukkitTask deathTask = AlleyPlugin.getInstance().getServer().getScheduler().runTaskLater(
                AlleyPlugin.getInstance(), () -> {
                    if (!viewer.isOnline() || !dummy.isSpawned()) return;
                    dummyPlayer.playEffect(EntityEffect.DEATH);
                    effect.execute(dummyPlayer);
                }, 10L);

        BukkitTask cleanupTask = AlleyPlugin.getInstance().getServer().getScheduler().runTaskLater(
                AlleyPlugin.getInstance(), () -> {
                    Session session = SESSIONS.remove(viewerId);
                    if (session == null) return;
                    session.destroyNpc();
                    completion.run();
                }, 110L);

        SESSIONS.put(viewerId, new Session(dummy, deathTask, cleanupTask));
        return true;
    }

    static void cancel(UUID viewerId) {
        Session session = SESSIONS.remove(viewerId);
        if (session == null) return;
        session.deathTask.cancel();
        session.cleanupTask.cancel();
        session.destroyNpc();
    }

    private static final class Session {
        private final NativeBotPlayer dummy;
        private final BukkitTask deathTask;
        private final BukkitTask cleanupTask;

        private Session(NativeBotPlayer dummy, BukkitTask deathTask, BukkitTask cleanupTask) {
            this.dummy = dummy;
            this.deathTask = deathTask;
            this.cleanupTask = cleanupTask;
        }

        private void destroyNpc() {
            dummy.remove();
        }
    }
}
