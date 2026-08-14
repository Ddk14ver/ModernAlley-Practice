package dev.revere.alley.feature.match.combat.legacy;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import dev.revere.alley.AlleyPlugin;
import org.bukkit.entity.Entity;

/**
 * Suppresses the sweep-attack arc particle for oldSwordBlockKB players.
 * 为 oldSwordBlockKB 玩家抑制横扫攻击的弧形粒子。
 *
 * <p>oldSwordBlockKB removes the modern attack cooldown, so every non-sprint
 * sword swing is a full-cooldown sweep. The sweep's arc particle is delivered to
 * clients as vanilla entity-status {@code 55} (SWEEP_ATTACK) — there is no Bukkit
 * event for it, and cancelling the {@code ENTITY_SWEEP_ATTACK} damage event only
 * removes the damage, not the visual. This packet filter drops that status packet
 * whenever the sweeping entity is in oldSwordBlockKB mode, hiding the arc entirely.
 *
 * <p>oldSwordBlockKB 会把玩家的攻击速度设为 24，因此每次不冲刺的挥剑都是满蓄力横扫。
 * 横扫的弧形粒子通过原版实体状态 {@code 55}（SWEEP_ATTACK）发给客户端——Bukkit
 * 没有对应事件，且取消 {@code ENTITY_SWEEP_ATTACK} 伤害事件只会移除伤害而保留视觉。
 * 该包过滤器在横扫实体处于 oldSwordBlockKB 模式时丢弃该状态包，彻底隐藏弧形。
 */
public class SweepAttackHandler extends PacketAdapter {

    /** Vanilla entity-status byte for a sword sweep attack. 横扫攻击的实体状态字节。 */
    private static final byte SWEEP_ATTACK_STATUS = 55;

    private final LegacyCombatService svc;

    public SweepAttackHandler(LegacyCombatService svc) {
        super(AlleyPlugin.getInstance(), ListenerPriority.HIGH, PacketType.Play.Server.ENTITY_STATUS);
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
        PacketContainer packet = event.getPacket();
        if (packet.getIntegers().size() == 0 || packet.getBytes().size() == 0) {
            return;
        }
        if (packet.getBytes().read(0) != SWEEP_ATTACK_STATUS) {
            return;
        }

        Entity entity = ProtocolLibrary.getProtocolManager()
                .getEntityFromID(event.getPlayer().getWorld(), packet.getIntegers().read(0));
        if (entity == null || !this.svc.hasSwordBlockKB(entity.getUniqueId())) {
            return;
        }

        event.setCancelled(true);
    }
}
