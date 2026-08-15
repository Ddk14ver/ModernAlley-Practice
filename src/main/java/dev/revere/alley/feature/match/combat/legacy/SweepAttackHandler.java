package dev.revere.alley.feature.match.combat.legacy;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import dev.revere.alley.AlleyPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

/**
 * Suppresses the 1.21 sweep-attack arc particle for oldSwordBlockKB players.
 * 为 oldSwordBlockKB 玩家抑制 1.21 横扫攻击的弧形粒子。
 *
 * <p>Cancelling {@code ENTITY_SWEEP_ATTACK} only removes the extra damage.
 * 1.21 delivers the remaining visual as {@code ParticleTypes.SWEEP_ATTACK}
 * world particles, not entity-status 55 (that byte is SWAP_HANDS now).
 */
public class SweepAttackHandler extends PacketAdapter {

    private final LegacyCombatService svc;

    public SweepAttackHandler(LegacyCombatService svc) {
        super(AlleyPlugin.getInstance(), ListenerPriority.HIGH, PacketType.Play.Server.WORLD_PARTICLES);
        this.svc = svc;
    }

    public void enable() {
        ProtocolLibrary.getProtocolManager().addPacketListener(this);
    }

    public void disable() {
        ProtocolLibrary.getProtocolManager().removePacketListener(this);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.WORLD_PARTICLES) {
            suppressSweepParticle(event);
        }
    }

    private void suppressSweepParticle(PacketEvent event) {
        PacketContainer packet = event.getPacket();
        if (!isSweepAttackParticle(packet)) return;

        Player viewer = event.getPlayer();
        if (this.svc.hasSwordBlockKB(viewer.getUniqueId())) {
            event.setCancelled(true);
            return;
        }

        Location origin = readParticleOrigin(packet, viewer);
        if (origin == null || origin.getWorld() == null) return;
        for (Entity entity : origin.getWorld().getNearbyEntities(origin, 3.0, 3.0, 3.0)) {
            if (entity instanceof Player attacker && this.svc.hasSwordBlockKB(attacker.getUniqueId())) {
                event.setCancelled(true);
                return;
            }
        }
    }

    private boolean isSweepAttackParticle(PacketContainer packet) {
        try {
            if (packet.getNewParticles().size() > 0) {
                Object particle = packet.getNewParticles().read(0);
                return particleNameContainsSweep(particle);
            }
        } catch (Throwable ignored) {
        }
        try {
            if (packet.getParticles().size() > 0) {
                Object particle = packet.getParticles().read(0);
                return particleNameContainsSweep(particle);
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    private boolean particleNameContainsSweep(Object particle) {
        if (particle == null) return false;
        String name = particle.toString();
        return name.contains("SWEEP") || name.contains("sweep");
    }

    private Location readParticleOrigin(PacketContainer packet, Player viewer) {
        try {
            if (packet.getDoubles().size() >= 3) {
                return new Location(viewer.getWorld(),
                        packet.getDoubles().read(0),
                        packet.getDoubles().read(1),
                        packet.getDoubles().read(2));
            }
        } catch (Throwable ignored) {
        }
        try {
            if (packet.getFloat().size() >= 3) {
                return new Location(viewer.getWorld(),
                        packet.getFloat().read(0),
                        packet.getFloat().read(1),
                        packet.getFloat().read(2));
            }
        } catch (Throwable ignored) {
        }
        return viewer.getLocation();
    }
}
