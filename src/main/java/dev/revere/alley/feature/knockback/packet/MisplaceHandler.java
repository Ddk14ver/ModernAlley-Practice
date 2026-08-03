package dev.revere.alley.feature.knockback.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.feature.knockback.KnockbackManager;
import dev.revere.alley.feature.knockback.KnockbackProfile;
import dev.revere.alley.feature.knockback.data.PlayerKnockbackData;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

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

    private final KnockbackManager manager;
    private final Map<UUID, Deque<QueuedPacket>> packetQueues = new HashMap<>();
    private final Map<UUID, Set<String>> expectedPackets = new HashMap<>();
    private final Map<UUID, Long> lastAttackTick = new HashMap<>();
    private final Map<UUID, Player> lastAttacker = new HashMap<>();

    public MisplaceHandler(KnockbackManager manager) {
        super(AlleyPlugin.getInstance(), ListenerPriority.LOWEST, PACKETS);
        this.manager = manager;
    }

    public void enable() {
        ProtocolLibrary.getProtocolManager().addPacketListener(this);
    }

    public void disable() {
        packetQueues.clear();
        expectedPackets.clear();
        ProtocolLibrary.getProtocolManager().removePacketListener(this);
    }

    public void onQuit(Player player) {
        UUID uuid = player.getUniqueId();
        packetQueues.remove(uuid);
        expectedPackets.remove(uuid);
        lastAttackTick.remove(uuid);
        lastAttacker.remove(uuid);
    }

    public void recordAttack(Player attacker, Player victim) {
        UUID v = victim.getUniqueId();
        lastAttackTick.put(v, System.currentTimeMillis());
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
            long now = System.currentTimeMillis();
            int noDamage = viewer.getMaximumNoDamageTicks();
            if (target != null && lastTick != null
                    && entityId == target.getEntityId()
                    && now - lastTick <= (noDamage / 2 + 3) * 50L) {

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
                    if (doubles.size() >= 3) {
                        doubles.write(0, ex + dx * dist);
                        doubles.write(2, ez + dz * dist);
                    } else {
                        integers.write(1, (int) Math.floor((ex + dx * dist) * 32.0));
                        integers.write(3, (int) Math.floor((ez + dz * dist) * 32.0));
                    }
                }
            }
        }

        // --- Packet Delay ---
        if (profile.isPacketDelayEnabled()) {
            Set<String> set = expectedPackets.get(viewerUuid);
            String key = event.getPacketType().name() + ":" + entityId;
            if (set != null && set.remove(key)) return;

            Player attacker = lastAttacker.get(viewerUuid);
            Long lastTick = lastAttackTick.get(viewerUuid);
            long now = System.currentTimeMillis();
            int noDamage = viewer.getMaximumNoDamageTicks();

            if (attacker != null && lastTick != null
                    && entityId == attacker.getEntityId()
                    && now - lastTick <= noDamage * 50L) {

                int delay = Math.max(1, profile.getPacketDelayTicks());
                PacketContainer cloned = packet.deepClone();
                event.setCancelled(true);

                synchronized (packetQueues) {
                    packetQueues.computeIfAbsent(viewerUuid, k -> new ArrayDeque<>())
                            .addLast(new QueuedPacket(viewer, cloned, delay));
                }
            }
        }
    }

    public void tick() {
        synchronized (packetQueues) {
            Iterator<Map.Entry<UUID, Deque<QueuedPacket>>> it = packetQueues.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<UUID, Deque<QueuedPacket>> entry = it.next();
                UUID uuid = entry.getKey();
                Deque<QueuedPacket> queue = entry.getValue();

                while (!queue.isEmpty() && queue.peekFirst().canSend()) {
                    QueuedPacket q = queue.pollFirst();
                    String key = q.packet.getType().name() + ":" + q.packet.getIntegers().read(0);
                    expectedPackets.computeIfAbsent(uuid, k -> new HashSet<>()).add(key);
                    try {
                        ProtocolLibrary.getProtocolManager().sendServerPacket(q.player, q.packet);
                    } catch (Exception ignored) {}
                }

                if (queue.isEmpty()) it.remove();
            }
        }
    }

    private static class QueuedPacket {
        final Player player;
        final PacketContainer packet;
        final long sendAt;

        QueuedPacket(Player player, PacketContainer packet, int delayTicks) {
            this.player = player;
            this.packet = packet;
            this.sendAt = System.currentTimeMillis() + delayTicks * 50L;
        }

        boolean canSend() { return System.currentTimeMillis() >= sendAt; }
    }
}
