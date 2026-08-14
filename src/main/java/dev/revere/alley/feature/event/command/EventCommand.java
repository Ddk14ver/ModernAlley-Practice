package dev.revere.alley.feature.event.command;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.feature.event.EventService;
import dev.revere.alley.feature.event.EventState;
import dev.revere.alley.feature.event.HostedEvent;
import dev.revere.alley.feature.event.menu.EventsMenu;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import org.bukkit.entity.Player;

import java.util.List;

public class EventCommand extends BaseCommand {
    private final EventService eventService = AlleyPlugin.getInstance().getService(EventService.class);

    @Override
    @CommandData(name = "event", aliases = {"events"}, description = "View active server events.")
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        if (this.eventService.getEvents().isEmpty()) {
            player.sendMessage(CC.translate("&cThere are no active or queued events."));
            return;
        }
        new EventsMenu().openMenu(player);
    }

    @CommandData(name = "event.list", description = "Lists active server events.")
    public void list(CommandArgs command) {
        Player player = command.getPlayer();
        List<HostedEvent> events = this.eventService.getEvents();
        player.sendMessage("");
        player.sendMessage(CC.translate("&6&lServer Events &7(" + events.size() + ")"));
        if (events.isEmpty()) {
            player.sendMessage(CC.translate(" &cNo events are active or queued."));
        } else {
            for (HostedEvent event : events) {
                player.sendMessage(CC.translate(" &6#" + event.getNumericId() + " &f" + event.getDisplayName()
                        + " &7- " + event.getState().getDisplayName() + " &7(" + event.getParticipants().size()
                        + "/" + event.getMaxPlayers() + ")"));
            }
        }
        player.sendMessage("");
    }

    @CommandData(name = "event.join", description = "Joins a server event.")
    public void join(CommandArgs command) {
        Player player = command.getPlayer();
        HostedEvent event = resolve(command.getArgs());
        if (event == null) {
            player.sendMessage(CC.translate("&cUsage: /event join <id>"));
            return;
        }
        this.eventService.joinEvent(player, event);
    }

    @CommandData(name = "event.leave", description = "Leaves your current server event.")
    public void leave(CommandArgs command) {
        this.eventService.leaveEvent(command.getPlayer());
    }

    @CommandData(name = "event.start", permission = "alley.event.admin.start", description = "Force-starts an event.")
    public void start(CommandArgs command) {
        Player player = command.getPlayer();
        HostedEvent event = resolve(command.getArgs());
        if (event == null || event.getState() != EventState.STARTING) {
            player.sendMessage(CC.translate("&cThat event cannot be started."));
            return;
        }
        this.eventService.forceStart(event);
    }

    @CommandData(name = "event.cancel", permission = "alley.event.admin.cancel", description = "Cancels an event.")
    public void cancel(CommandArgs command) {
        Player player = command.getPlayer();
        String[] args = command.getArgs();
        HostedEvent event = resolve(args);
        if (event == null) {
            player.sendMessage(CC.translate("&cUsage: /event cancel <id> [reason]"));
            return;
        }
        if (event.getState() == EventState.RUNNING && !event.getActiveMatches().isEmpty()) {
            player.sendMessage(CC.translate("&cA running event cannot be cancelled while matches are active."));
            return;
        }
        String reason = args.length > 1
                ? String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length))
                : "Cancelled by an administrator";
        this.eventService.cancelEvent(event, reason);
    }

    private HostedEvent resolve(String[] args) {
        if (args.length == 0) {
            List<HostedEvent> events = this.eventService.getEvents();
            return events.size() == 1 ? events.get(0) : null;
        }
        try {
            return this.eventService.getEvent(Integer.parseInt(args[0].replace("#", "")));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
