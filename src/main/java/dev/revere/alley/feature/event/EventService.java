package dev.revere.alley.feature.event;

import dev.revere.alley.bootstrap.lifecycle.Service;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.match.Match;
import org.bukkit.entity.Player;

import java.util.List;

public interface EventService extends Service {
    HostedEvent hostEvent(Player host, EventType type, EventMode mode, Kit kit);

    boolean joinEvent(Player player, HostedEvent event);

    void leaveEvent(Player player);

    void forceStart(HostedEvent event);

    void cancelEvent(HostedEvent event, String reason);

    void handleMatchEnd(Match match);

    void handleDisconnect(Player player);

    HostedEvent getEvent(int numericId);

    HostedEvent getPlayerEvent(Player player);

    List<HostedEvent> getEvents();
}
