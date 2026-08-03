package dev.revere.alley.common.reflect.internal.types;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.EnumWrappers;
import dev.revere.alley.common.logger.Logger;
import dev.revere.alley.common.reflect.Reflection;
import dev.revere.alley.common.TaskUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * @author Emmy
 * @project Alley
 * @since 02/04/2025
 */
public class DeathReflectionServiceImpl implements Reflection {
    private final int FAKE_ENTITY_ID;
    private final int VIEW_RADIUS;
    private final ProtocolManager protocolManager;

    public DeathReflectionServiceImpl() {
        this.FAKE_ENTITY_ID = Integer.MAX_VALUE - 1;
        this.VIEW_RADIUS = 64;
        this.protocolManager = ProtocolLibrary.getProtocolManager();
    }

    /**
     * Visualizes the death of a player by sending fake entity packets to nearby players.
     * 通过向附近玩家发送假实体数据包来可视化玩家的死亡。
     *
     * @param player The supposedly dying player.
     *               据称正在死亡的玩家。
     */
    public void animateDeath(Player player) {
        Set<Player> playersInRange = this.getPlayersInRange(player);

        for (Player watcher : playersInRange) {
            sendFakeDeathAnimation(player, watcher);
        }

        TaskUtil.runLater(() -> this.removeFakeEntities(playersInRange), 40L);
    }

    private void sendFakeDeathAnimation(Player target, Player watcher) {
        try {
            // Send fake named entity spawn
            // 发送假的命名实体生成包
            PacketContainer spawnPacket = protocolManager.createPacket(PacketType.Play.Server.NAMED_ENTITY_SPAWN);
            spawnPacket.getIntegers().write(0, FAKE_ENTITY_ID);
            spawnPacket.getUUIDs().write(0, target.getUniqueId());
            spawnPacket.getDoubles()
                .write(0, target.getLocation().getX())
                .write(1, target.getLocation().getY())
                .write(2, target.getLocation().getZ());
            spawnPacket.getBytes()
                .write(0, (byte) ((int) (target.getLocation().getYaw() * 256.0F / 360.0F)))
                .write(1, (byte) ((int) (target.getLocation().getPitch() * 256.0F / 360.0F)));

            protocolManager.sendServerPacket(watcher, spawnPacket);

            // Send entity status (death = 3)
            // 发送实体状态（死亡 = 3）
            PacketContainer statusPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_STATUS);
            statusPacket.getIntegers().write(0, FAKE_ENTITY_ID);
            statusPacket.getBytes().write(0, (byte) 3); // Death status
                                                         // 死亡状态
            protocolManager.sendServerPacket(watcher, statusPacket);

            // Send metadata with health = 0 via ProtocolLib 5.x API
            // 通过 ProtocolLib 5.x API 发送生命值为 0 的元数据
            PacketContainer metadataPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
            metadataPacket.getIntegers().write(0, FAKE_ENTITY_ID);
            List<com.comphenix.protocol.wrappers.WrappedWatchableObject> dataValues = new ArrayList<>();
            WrappedDataWatcher watcher_dw = new WrappedDataWatcher();
            watcher_dw.setObject(6, WrappedDataWatcher.Registry.get(Float.class), 0.0F);
            dataValues.addAll(watcher_dw.getWatchableObjects());
            metadataPacket.getWatchableCollectionModifier().write(0, dataValues);
            protocolManager.sendServerPacket(watcher, metadataPacket);
        } catch (Exception exception) {
            Logger.logException("Failed to send death animation packet.", exception);
        }
    }

    /**
     * Retrieves a set of players within a certain radius of the player.
     * 获取玩家周围一定半径内的一组玩家。
     *
     * @param player The player to check around.
     *               要检查周围的玩家。
     * @return A set of nearby players.
     *         附近玩家的集合。
     */
    private Set<Player> getPlayersInRange(Player player) {
        Set<Player> playersInRange = new HashSet<>();
        for (Entity entity : player.getNearbyEntities(this.VIEW_RADIUS, this.VIEW_RADIUS, this.VIEW_RADIUS)) {
            if (entity instanceof Player && !entity.getUniqueId().equals(player.getUniqueId())) {
                playersInRange.add((Player) entity);
            }
        }
        return playersInRange;
    }

    /**
     * Removes the fake death entity from players' views.
     * 从玩家的视野中移除假死亡实体。
     *
     * @param players The players to remove the fake entity for.
     *                要为其移除假实体的玩家。
     */
    private void removeFakeEntities(Set<Player> players) {
        try {
            PacketContainer destroyPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
            destroyPacket.getIntLists().write(0, List.of(FAKE_ENTITY_ID));
            for (Player player : players) {
                protocolManager.sendServerPacket(player, destroyPacket);
            }
        } catch (Exception exception) {
            Logger.logException("Failed to destroy fake death entity.", exception);
        }
    }
}