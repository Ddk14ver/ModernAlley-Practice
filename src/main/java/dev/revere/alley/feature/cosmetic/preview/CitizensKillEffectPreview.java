package dev.revere.alley.feature.cosmetic.preview;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.feature.cosmetic.internal.repository.impl.killeffect.BaseKillEffect;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

final class CitizensKillEffectPreview {
    private static final Map<UUID, Session> SESSIONS = new ConcurrentHashMap<>();

    private CitizensKillEffectPreview() {
    }

    static boolean start(Player viewer, BaseKillEffect effect, Location location, Runnable completion) {
        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "Preview Dummy");
        npc.setProtected(true);
        if (!npc.spawn(location)) {
            npc.destroy();
            return false;
        }

        UUID viewerId = viewer.getUniqueId();
        BukkitTask deathTask = AlleyPlugin.getInstance().getServer().getScheduler().runTaskLater(
                AlleyPlugin.getInstance(), () -> {
                    if (!viewer.isOnline() || !npc.isSpawned() || !(npc.getEntity() instanceof Player dummy)) return;
                    dummy.playEffect(EntityEffect.DEATH);
                    effect.execute(dummy);
                }, 10L);

        BukkitTask cleanupTask = AlleyPlugin.getInstance().getServer().getScheduler().runTaskLater(
                AlleyPlugin.getInstance(), () -> {
                    Session session = SESSIONS.remove(viewerId);
                    if (session == null) return;
                    session.destroyNpc();
                    completion.run();
                }, 110L);

        SESSIONS.put(viewerId, new Session(npc, deathTask, cleanupTask));
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
        private final NPC npc;
        private final BukkitTask deathTask;
        private final BukkitTask cleanupTask;

        private Session(NPC npc, BukkitTask deathTask, BukkitTask cleanupTask) {
            this.npc = npc;
            this.deathTask = deathTask;
            this.cleanupTask = cleanupTask;
        }

        private void destroyNpc() {
            if (npc.isSpawned()) npc.despawn();
            npc.destroy();
        }
    }
}
