package dev.revere.alley.visual.nametag.internal;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.library.assemble.events.AssembleBoardCreatedEvent;
import dev.revere.alley.library.assemble.events.AssembleBoardDestroyEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/** Keeps nametag teams in sync when the sidebar scoreboard is replaced. */
public final class NametagScoreboardListener implements Listener {
    private final NametagServiceImpl service;

    public NametagScoreboardListener(NametagServiceImpl service) {
        this.service = service;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBoardCreated(AssembleBoardCreatedEvent event) {
        Player player = Bukkit.getPlayer(event.getBoard().getUuid());
        if (player == null) return;
        Bukkit.getScheduler().runTask(AlleyPlugin.getInstance(),
                () -> service.refreshAfterScoreboardChange(player));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBoardDestroyed(AssembleBoardDestroyEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;
        Bukkit.getScheduler().runTask(AlleyPlugin.getInstance(),
                () -> service.refreshAfterScoreboardChange(player));
    }
}
