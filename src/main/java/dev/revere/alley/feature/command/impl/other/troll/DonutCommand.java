package dev.revere.alley.feature.command.impl.other.troll;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.core.locale.internal.impl.message.GlobalMessagesLocaleImpl;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Remi
 * @project alley-practice
 * @date 22/07/2025
 */
public class DonutCommand extends BaseCommand {
    private static final double DONUT_RADIUS = 2.5;
    private static final double TUBE_RADIUS = 0.5;
    private static final int MAIN_SEGMENTS = 250;
    private static final int TUBE_SEGMENTS = 250;
    private static int FAKE_ENTITY_ID_COUNTER = Integer.MAX_VALUE - 1_100_100;
    private static final ProtocolManager PROTOCOL_MANAGER = ProtocolLibrary.getProtocolManager();

    @CommandData(
            name = "donut",
            isAdminOnly = true,
            inGameOnly = false,
            usage = "donut <player>",
            description = "Spawns a donut of boats around a player"
            // 在玩家周围生成一圈甜甜圈状的船
    )
    @Override
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        String[] args = command.getArgs();

        if (args.length == 0) {
            command.sendUsage();
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            player.sendMessage(this.getString(GlobalMessagesLocaleImpl.ERROR_INVALID_PLAYER));
            return;
        }

        spawnDonut(target);
        player.sendMessage(this.getString(GlobalMessagesLocaleImpl.TROLL_PLAYER_DONUTED)
                .replace("{name-color}", String.valueOf(target.getDisplayName()))
                .replace("{player}", target.getName())
        );
    }

    private void spawnDonut(Player target) {
        Location location = target.getLocation();
        List<Integer> fakeEntityIds = new ArrayList<>(MAIN_SEGMENTS * TUBE_SEGMENTS);

        for (int i = 0; i < MAIN_SEGMENTS; i++) {
            double theta = 2 * Math.PI * i / MAIN_SEGMENTS;
            double cosTheta = Math.cos(theta);
            double sinTheta = Math.sin(theta);

            for (int j = 0; j < TUBE_SEGMENTS; j++) {
                double phi = 2 * Math.PI * j / TUBE_SEGMENTS;
                double cosPhi = Math.cos(phi);
                double sinPhi = Math.sin(phi);

                double x = (DONUT_RADIUS + TUBE_RADIUS * cosPhi) * cosTheta;
                double y = TUBE_RADIUS * sinPhi;
                double z = (DONUT_RADIUS + TUBE_RADIUS * cosPhi) * sinTheta;

                int fakeId = getNextFakeEntityId();
                fakeEntityIds.add(fakeId);

                sendBoatSpawnPacket(target, fakeId,
                    location.getX() + x,
                    location.getY() + y + 1.0f,
                    location.getZ() + z);
            }
        }

        int[] idsToDestroy = fakeEntityIds.stream().mapToInt(Integer::intValue).toArray();

        Bukkit.getScheduler().runTaskLater(AlleyPlugin.getInstance(), () -> {
            if (target.isOnline()) {
                sendEntityDestroyPacket(target, idsToDestroy);
            }
        }, 1200L);
    }

    private void sendBoatSpawnPacket(Player target, int entityId, double x, double y, double z) {
        try {
            PacketContainer spawnPacket = PROTOCOL_MANAGER.createPacket(PacketType.Play.Server.SPAWN_ENTITY);
            spawnPacket.getIntegers().write(0, entityId);
            spawnPacket.getUUIDs().write(0, UUID.randomUUID());
            spawnPacket.getEntityTypeModifier().write(0, EntityType.OAK_BOAT);
            spawnPacket.getDoubles()
                .write(0, x)
                .write(1, y)
                .write(2, z);
            spawnPacket.getIntegers().write(1, 0); // velocity x
            // 速度 x
            spawnPacket.getIntegers().write(2, 0); // velocity y
            // 速度 y
            spawnPacket.getIntegers().write(3, 0); // velocity z
            // 速度 z

            PROTOCOL_MANAGER.sendServerPacket(target, spawnPacket);
        } catch (Exception e) {
            // silently fail for each individual boat
            // 对于每条单独的船，静默失败
        }
    }

    private void sendEntityDestroyPacket(Player target, int[] entityIds) {
        try {
            PacketContainer destroyPacket = PROTOCOL_MANAGER.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
            List<Integer> idList = new ArrayList<>();
            for (int id : entityIds) {
                idList.add(id);
            }
            destroyPacket.getIntLists().write(0, idList);
            PROTOCOL_MANAGER.sendServerPacket(target, destroyPacket);
        } catch (Exception e) {
            // silently fail
            // 静默失败
        }
    }

    private int getNextFakeEntityId() {
        return FAKE_ENTITY_ID_COUNTER--;
    }
}