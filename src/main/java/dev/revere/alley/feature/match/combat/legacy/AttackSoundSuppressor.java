package dev.revere.alley.feature.match.combat.legacy;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import dev.revere.alley.AlleyPlugin;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Removes the 1.9+ sword "swoosh" attack sounds for oldOffhandsounds players.
 * 为 oldOffhandsounds 玩家移除 1.9+ 的挥剑剑风音效。
 *
 * <p>In 1.9+ every fully-charged sword swing plays an attack sound
 * ({@code entity.player.attack.strong}, {@code .sweep}, {@code .crit},
 * {@code .knockback}) to the attacker and everyone nearby. 1.8 has no such
 * sound — the only combat sound is the victim's hurt sound. The oldOffhand
 * module is the one that restores 1.8 combat sounds (it already plays the old
 * {@code entity.player.hurt} sound on damage), so this packet filter drops the
 * swoosh for oldOffhand players (recipient check) and, as a fallback, whenever
 * the sound originates at an oldOffhand attacker's position (so spectators of a
 * 1.8 fight don't hear it either). The victim's {@code entity.player.hurt}
 * sound is a different sound key and is untouched.
 *
 * <p>1.9+ 中每次满蓄力的挥剑都会向攻击者及周围玩家播放攻击音效
 * （{@code entity.player.attack.strong}、{@code .sweep}、{@code .crit}、
 * {@code .knockback}）。1.8 没有这种音效——唯一的战斗音效是被击者的扣血音。
 * oldOffhand 模块负责还原 1.8 战斗音效（它已经在受伤时播放老版
 * {@code entity.player.hurt} 扣血音），因此该包过滤器对 oldOffhand 玩家
 * （接收方检查）丢弃剑风音，并作为兜底在音效位置附近存在 oldOffhand 攻击者时
 * （让 1.8 战斗的观战者也不会听到）同样丢弃。被击者的 {@code entity.player.hurt}
 * 扣血音是另一个音效键，不受影响。
 */
public class AttackSoundSuppressor extends PacketAdapter {

    private final LegacyCombatService svc;
    private final Queue<PearlTeleportSound> pearlTeleportSounds = new ConcurrentLinkedQueue<>();

    public AttackSoundSuppressor(LegacyCombatService svc) {
        super(AlleyPlugin.getInstance(), ListenerPriority.HIGH, PacketType.Play.Server.NAMED_SOUND_EFFECT);
        this.svc = svc;
    }

    public void enable() {
        ProtocolLibrary.getProtocolManager().addPacketListener(this);
    }

    public void disable() {
        ProtocolLibrary.getProtocolManager().removePacketListener(this);
        pearlTeleportSounds.clear();
    }

    public void markPearlTeleport(Location from, Location to) {
        long now = System.currentTimeMillis();
        pearlTeleportSounds.removeIf(marker -> marker.expiresAt() < now);
        long expiresAt = now + 250L;
        if (from != null && from.getWorld() != null) {
            pearlTeleportSounds.add(new PearlTeleportSound(from.clone(), expiresAt));
        }
        if (to != null && to.getWorld() != null) {
            pearlTeleportSounds.add(new PearlTeleportSound(to.clone(), expiresAt));
        }
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        PacketContainer packet = event.getPacket();
        if (packet.getSoundEffects().size() == 0) {
            return;
        }
        Sound sound = packet.getSoundEffects().read(0);
        if (isPearlTeleportSound(sound) && isMarkedPearlTeleport(packet, event.getPlayer())) {
            event.setCancelled(true);
            return;
        }
        if (!isPlayerAttackSound(sound)) {
            return;
        }

        Player recipient = event.getPlayer();
        if (this.svc.hasOldOffhand(recipient.getUniqueId())) {
            event.setCancelled(true);
            return;
        }

        // Fallback: hide the swoosh from everyone when it originates from an oldOffhand attacker.
        if (packet.getIntegers().size() >= 3) {
            double x = packet.getIntegers().read(0) / 8.0;
            double y = packet.getIntegers().read(1) / 8.0;
            double z = packet.getIntegers().read(2) / 8.0;
            World world = recipient.getWorld();
            for (Entity entity : world.getNearbyEntities(new Location(world, x, y, z), 0.75, 0.75, 0.75)) {
                if (entity instanceof Player attacker && this.svc.hasOldOffhand(attacker.getUniqueId())) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    /**
     * Do not call Sound#key here. ProtocolLib can expose direct sound-event
     * holders (for example string-based music-disc playback) as an unregistered
     * Bukkit Sound. Paper 1.21.11 correctly throws when key() is requested for
     * such a holder. Identity checks against the registered player-attack
     * constants avoid touching the registry for unrelated packets.
     */
    private boolean isPlayerAttackSound(Sound sound) {
        return sound == Sound.ENTITY_PLAYER_ATTACK_CRIT
                || sound == Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK
                || sound == Sound.ENTITY_PLAYER_ATTACK_NODAMAGE
                || sound == Sound.ENTITY_PLAYER_ATTACK_STRONG
                || sound == Sound.ENTITY_PLAYER_ATTACK_SWEEP
                || sound == Sound.ENTITY_PLAYER_ATTACK_WEAK;
    }

    private boolean isPearlTeleportSound(Sound sound) {
        return sound == Sound.ENTITY_PLAYER_TELEPORT
                || sound == Sound.ENTITY_ENDERMAN_TELEPORT;
    }

    private boolean isMarkedPearlTeleport(PacketContainer packet, Player recipient) {
        if (packet.getIntegers().size() < 3) return false;

        long now = System.currentTimeMillis();
        pearlTeleportSounds.removeIf(marker -> marker.expiresAt() < now);
        double x = packet.getIntegers().read(0) / 8.0D;
        double y = packet.getIntegers().read(1) / 8.0D;
        double z = packet.getIntegers().read(2) / 8.0D;
        for (PearlTeleportSound marker : pearlTeleportSounds) {
            Location location = marker.location();
            if (!location.getWorld().equals(recipient.getWorld())) continue;
            double dx = location.getX() - x;
            double dy = location.getY() - y;
            double dz = location.getZ() - z;
            if (dx * dx + dy * dy + dz * dz <= 16.0D) return true;
        }
        return false;
    }

    private record PearlTeleportSound(Location location, long expiresAt) { }
}
