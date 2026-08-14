package dev.revere.alley.feature.cps;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import dev.revere.alley.AlleyPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Records main-hand swing packets so entity attacks, misses and air clicks
 * all use the same source of truth on modern clients.
 */
public final class CPSListener implements Listener {

    private static final CPSManager INSTANCE = new CPSManager();

    private final CPSManager cpsManager;
    private final Set<UUID> diggingPlayers = ConcurrentHashMap.newKeySet();
    private PacketAdapter packetListener;

    public CPSListener() {
        this.cpsManager = INSTANCE;
    }

    /** Shared CPS manager used by the scoreboard and PlaceholderAPI. */
    public static CPSManager getCpsManager() {
        return INSTANCE;
    }

    /** Start the 1-tick cleanup loop. */
    public void startTicking() {
        registerPacketListener();
        org.bukkit.Bukkit.getScheduler().runTaskTimerAsynchronously(
                AlleyPlugin.getInstance(), cpsManager::tick, 1L, 1L);
    }

    private void registerPacketListener() {
        if (this.packetListener != null) return;
        this.packetListener = new PacketAdapter(
                AlleyPlugin.getInstance(),
                ListenerPriority.MONITOR,
                PacketType.Play.Client.ARM_ANIMATION,
                PacketType.Play.Client.BLOCK_DIG
        ) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                UUID playerId = event.getPlayer().getUniqueId();
                if (event.getPacketType() == PacketType.Play.Client.BLOCK_DIG) {
                    handleDigPacket(playerId, event);
                    return;
                }

                EnumWrappers.Hand hand = event.getPacket().getHands().readSafely(0);
                if (hand != EnumWrappers.Hand.OFF_HAND && !diggingPlayers.contains(playerId)) {
                    cpsManager.recordClick(playerId);
                }
            }
        };
        ProtocolLibrary.getProtocolManager().addPacketListener(this.packetListener);
    }

    private void handleDigPacket(UUID playerId, PacketEvent event) {
        EnumWrappers.PlayerDigType action = event.getPacket().getPlayerDigTypes().readSafely(0);
        if (action == EnumWrappers.PlayerDigType.START_DESTROY_BLOCK) {
            this.diggingPlayers.add(playerId);
        } else if (action == EnumWrappers.PlayerDigType.ABORT_DESTROY_BLOCK
                || action == EnumWrappers.PlayerDigType.STOP_DESTROY_BLOCK) {
            this.diggingPlayers.remove(playerId);
        }
    }

    // ---- cleanup ----

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        this.diggingPlayers.remove(playerId);
        cpsManager.remove(playerId);
    }
}
