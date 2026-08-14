package dev.revere.alley.feature.knockback.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.feature.bot.match.BotMatchSession;
import dev.revere.alley.feature.knockback.KnockbackManager;
import dev.revere.alley.feature.knockback.KnockbackProfile;
import dev.revere.alley.feature.knockback.data.PlayerKnockbackData;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Packet-level misplace: shifts position update packets for knockback targets.
 * Also supports delayed re-sending of position packets.
 * Based on KBM's MisplaceHandler.
 */
public class MisplaceHandler extends PacketAdapter {

    private static final Set<PacketType> PACKETS = new HashSet<>(Arrays.asList(
            PacketType.Play.Server.ENTITY_TELEPORT,
            PacketType.Play.Server.REL_ENTITY_MOVE_LOOK,
            PacketType.Play.Server.REL_ENTITY_MOVE,
            PacketType.Play.Server.ENTITY_LOOK
    ));

    /** Delayed packet Y below the entity's real Y by more than this many blocks gets corrected. */
    private static final double VERTICAL_SINK_TOLERANCE = 0.6;

    private final KnockbackManager manager;
    private final Map<UUID, Deque<QueuedPacket>> packetQueues = new HashMap<>();
    private final Map<UUID, Long> lastAttackTick = new ConcurrentHashMap<>();
    private final Map<UUID, Player> lastAttacker = new ConcurrentHashMap<>();

    public MisplaceHandler(KnockbackManager manager) {
        super(AlleyPlugin.getInstance(), ListenerPriority.LOWEST, PACKETS);
        this.manager = manager;
    }

    public void enable() {
        ProtocolLibrary.getProtocolManager().addPacketListener(this);
    }

    public void disable() {
        synchronized (packetQueues) {
            packetQueues.clear();
        }
        lastAttackTick.clear();
        lastAttacker.clear();
        ProtocolLibrary.getProtocolManager().removePacketListener(this);
    }

    public void onQuit(Player player) {
        if (player != null) clearPlayer(player.getUniqueId());
    }

    public void clearPlayer(UUID uuid) {
        if (uuid == null) return;

        synchronized (packetQueues) {
            packetQueues.remove(uuid);
            packetQueues.values().forEach(queue -> queue.removeIf(packet ->
                    packet.target != null && packet.target.getUniqueId().equals(uuid)));
            packetQueues.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        }
        lastAttackTick.remove(uuid);
        lastAttacker.remove(uuid);

        List<UUID> affectedViewers = lastAttacker.entrySet().stream()
                .filter(entry -> entry.getValue() != null
                        && entry.getValue().getUniqueId().equals(uuid))
                .map(Map.Entry::getKey)
                .toList();
        affectedViewers.forEach(viewer -> {
            lastAttacker.remove(viewer);
            lastAttackTick.remove(viewer);
        });
    }

    public void recordAttack(Player attacker, Player victim) {
        // Packet effects are viewer-side. A server-side Bot has no client packet
        // stream, so storing it as a viewer can never be consumed.
        if (isBot(victim)) return;
        UUID v = victim.getUniqueId();
        lastAttackTick.put(v, manager.getCurrentTick());
        lastAttacker.put(v, attacker);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        if (!PACKETS.contains(event.getPacketType())) return;

        Player viewer = event.getPlayer();
        UUID viewerUuid = viewer.getUniqueId();

        PlayerKnockbackData viewerData = manager.getPlayerData(viewerUuid);
        KnockbackProfile profile = viewerData != null ? manager.getProfile(viewerData.getProfileName()) : null;
        if (profile == null) return;

        PacketContainer packet = event.getPacket();
        StructureModifier<Integer> integers = packet.getIntegers();
        int entityId = integers.read(0);
        if (entityId == viewer.getEntityId()) return;

        // --- Misplace ---
        if (profile.isPacketMisplaceEnabled() && event.getPacketType() == PacketType.Play.Server.ENTITY_TELEPORT) {
            Player target = lastAttacker.get(viewerUuid);
            Long lastTick = lastAttackTick.get(viewerUuid);
            long now = manager.getCurrentTick();
            int noDamage = viewer.getMaximumNoDamageTicks();
            if (target != null && lastTick != null
                    && entityId == target.getEntityId()
                    && now - lastTick <= noDamage / 2L + 3L) {

                Location vLoc = viewer.getLocation();
                double vx = vLoc.getX(), vz = vLoc.getZ();
                double ex, ez;

                StructureModifier<Double> doubles = packet.getDoubles();
                if (doubles.size() >= 3) {
                    ex = doubles.read(0);
                    ez = doubles.read(2);
                } else {
                    if (integers.size() < 4) return;
                    ex = integers.read(1) / 32.0;
                    ez = integers.read(3) / 32.0;
                }

                double dx = vx - ex, dz = vz - ez;
                double len = Math.sqrt(dx * dx + dz * dz);
                if (len > 0) {
                    dx /= len; dz /= len;
                    double dist = profile.getPacketMisplaceDistance();
                    // Push the attacker's visual position AWAY from the victim so the victim
                    // perceives an exaggerated attack range (the "misplace" feel). Only x/z
                    // are shifted; the Y stays untouched so the entity never appears sunk.
                    // 将攻击者的视觉位置推离受害者，让被连击者产生"攻击距离很远"的错位感。
                    // 只偏移X/Z，Y保持不动，实体不会出现沉地/悬空的视觉。
                    if (doubles.size() >= 3) {
                        doubles.write(0, ex - dx * dist);
                        doubles.write(2, ez - dz * dist);
                    } else {
                        integers.write(1, (int) Math.floor((ex - dx * dist) * 32.0));
                        integers.write(3, (int) Math.floor((ez - dz * dist) * 32.0));
                    }
                }
            }
        }

        // --- Packet Delay ---
        if (profile.isPacketDelayEnabled()) {
            // Delay only relative movement packets. ENTITY_TELEPORT carries an
            // absolute position and becomes invalid as soon as the player is
            // teleported, respawned, crosses a world boundary, or is corrected
            // by another movement system; replaying it later causes a large snap.
            if (event.getPacketType() != PacketType.Play.Server.REL_ENTITY_MOVE
                    && event.getPacketType() != PacketType.Play.Server.REL_ENTITY_MOVE_LOOK) {
                return;
            }

            Player attacker = lastAttacker.get(viewerUuid);
            // A server-side Bot has no client connection. Replaying its mixed
            // teleport/relative-move stream later only presents stale positions
            // to the real player and causes sinking or an authoritative snap.
            if (isBot(attacker) && entityId == attacker.getEntityId()) return;

            Long lastTick = lastAttackTick.get(viewerUuid);
            long now = manager.getCurrentTick();
            int noDamage = viewer.getMaximumNoDamageTicks();
            boolean inWindow = attacker != null && lastTick != null
                    && entityId == attacker.getEntityId()
                    && now - lastTick <= noDamage;

            boolean sameEntityQueued;
            synchronized (packetQueues) {
                Deque<QueuedPacket> queue = packetQueues.get(viewerUuid);
                sameEntityQueued = queue != null
                        && queue.stream().anyMatch(queued -> queued.entityId == entityId);
            }

            // Preserve strict packet order for this entity. If one of its older packets
            // is still queued, this one must trail it even outside the window; otherwise
            // the client would receive a newer position and then snap back to the stale one — the
            // "body half-stuck in the ground" flicker. The old drop-next-real-packet
            // mechanism is removed entirely (it silently discarded position updates).
            // Keep unrelated entities out of this queue to avoid needless per-viewer growth.
            // 保持同一实体的严格包顺序：只要队列里还有旧包，本包（即使窗口已结束）也排在其后。
            // 否则客户端会先收到新位置、再被旧包拉回，产生"半截身体卡在土里"的闪烁。
            // 同时移除了旧的"丢弃下一个真实包"机制（它会静默丢失位置更新）。
            if (inWindow || sameEntityQueued) {
                int delay = inWindow ? Math.max(1, profile.getPacketDelayTicks()) : 0;
                PacketContainer cloned = packet.deepClone();
                event.setCancelled(true);

                synchronized (packetQueues) {
                    packetQueues.computeIfAbsent(viewerUuid, k -> new ArrayDeque<>())
                            .addLast(new QueuedPacket(
                                    viewer, cloned, manager.getCurrentTick() + delay, attacker));
                }
            }
        }
    }

    public void tick() {
        long currentTick = manager.getCurrentTick();
        List<QueuedPacket> ready = new ArrayList<>();
        synchronized (packetQueues) {
            Iterator<Map.Entry<UUID, Deque<QueuedPacket>>> it = packetQueues.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<UUID, Deque<QueuedPacket>> entry = it.next();
                Deque<QueuedPacket> queue = entry.getValue();

                while (!queue.isEmpty() && queue.peekFirst().canSend(currentTick)) {
                    ready.add(queue.pollFirst());
                }

                if (queue.isEmpty()) it.remove();
            }
        }

        // ProtocolLib may invoke packet listeners while sending. Never hold the
        // queue monitor across that boundary; a listener is allowed to enqueue.
        for (QueuedPacket packet : ready) {
            sendDelayedPacket(packet);
        }
    }

    /**
     * Sends a queued packet, guarding against vertical "sinking": a delayed teleport that
     * sits well below the target's real position (the target is rising fast, e.g. after
     * being knocked into the air) would render the entity halfway inside the ground.
     * Rewrite the Y to the real position in that case; relative moves are smooth enough
     * that a 1-2 tick lag is handled by the client's interpolation.
     * 发送延迟包并做垂直保护：若延迟的传送包明显低于目标真实位置（目标正在快速上升，
     * 例如被击退弹起），会导致实体看起来半截埋在土里。此时将Y修正为真实位置。
     */
    private void sendDelayedPacket(QueuedPacket q) {
        PacketContainer packet = q.packet;
        // Delayed packets in the queue are the attacker's position updates, so the
        // recorded attacker reference is the reliable vertical anchor here.
        if (packet.getType() == PacketType.Play.Server.ENTITY_TELEPORT
                && q.target != null
                && packet.getIntegers().read(0) == q.target.getEntityId()) {
            StructureModifier<Double> doubles = packet.getDoubles();
            if (doubles.size() >= 3) {
                double realY = q.target.getY();
                double packetY = doubles.read(1);
                // A server-controlled player emits a different movement-packet mix.
                // Keep its delayed horizontal position but always anchor Y to
                // the real entity so stale teleports cannot render it inside blocks.
                if (isBot(q.target) || realY - packetY > VERTICAL_SINK_TOLERANCE) {
                    doubles.write(1, realY);
                }
            }
        }
        try {
            // This packet has already passed this listener once. Re-running
            // outbound filters would enqueue it again and create an endless
            // send -> listener -> queue -> send loop on the server thread.
            ProtocolLibrary.getProtocolManager().sendServerPacket(q.player, packet, false);
        } catch (Exception ignored) {}

    }

    private static class QueuedPacket {
        final Player player;
        final PacketContainer packet;
        final long sendAtTick;
        final int entityId;
        /** Attacker whose packets are delayed; used for the vertical sink guard (may be null). */
        final Player target;

        QueuedPacket(Player player, PacketContainer packet, long sendAtTick, Player target) {
            this.player = player;
            this.packet = packet;
            this.entityId = packet.getIntegers().read(0);
            this.target = target;
            this.sendAtTick = sendAtTick;
        }

        boolean canSend(long currentTick) { return currentTick >= sendAtTick; }
    }

    private static boolean isBot(Player player) {
        return player != null
                && player.getScoreboardTags().contains(BotMatchSession.BOT_ENTITY_TAG);
    }
}
