package dev.revere.alley.feature.event;

import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.match.Match;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Getter
public class HostedEvent {
    private final UUID uniqueId = UUID.randomUUID();
    private final int numericId;
    private final UUID hostUuid;
    private final String hostName;
    private final EventType type;
    private final EventMode mode;
    private final Kit kit;
    /** The kit whose inventory is used to populate SkyWars chests. */
    private final Kit skyWarsResourceKit;
    private final int maxPlayers;
    private final List<UUID> participants = new CopyOnWriteArrayList<>();
    private final List<UUID> remainingPlayers = new CopyOnWriteArrayList<>();
    private final Set<UUID> roundWinners = ConcurrentHashMap.newKeySet();
    private final List<Match> activeMatches = new CopyOnWriteArrayList<>();

    @Setter
    private EventState state = EventState.QUEUED;
    @Setter
    private int countdown;
    @Setter
    private int round;
    @Setter
    private BukkitTask countdownTask;

    public HostedEvent(int numericId, Player host, EventType type, EventMode mode, Kit kit, int maxPlayers) {
        this(numericId, host, type, mode, kit, null, maxPlayers);
    }

    public HostedEvent(int numericId, Player host, EventType type, EventMode mode, Kit kit,
                       Kit skyWarsResourceKit, int maxPlayers) {
        this.numericId = numericId;
        this.hostUuid = host.getUniqueId();
        this.hostName = host.getName();
        this.type = type;
        this.mode = mode;
        this.kit = kit;
        this.skyWarsResourceKit = skyWarsResourceKit;
        this.maxPlayers = maxPlayers;
    }

    public boolean isParticipant(UUID uuid) {
        return this.participants.contains(uuid);
    }

    public List<Player> getOnlinePlayers() {
        return this.participants.stream()
                .map(Bukkit::getPlayer)
                .filter(player -> player != null && player.isOnline())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public String getDisplayName() {
        if (this.type == EventType.SUMO) {
            return this.mode.getDisplayName() + " Sumo";
        }
        return this.type.getDisplayName();
    }
}
