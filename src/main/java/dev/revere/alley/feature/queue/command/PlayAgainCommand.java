package dev.revere.alley.feature.queue.command;

import dev.revere.alley.feature.kit.KitService;
import dev.revere.alley.feature.queue.listener.PlayAgainListener;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import dev.revere.alley.library.command.annotation.CompleterData;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/** Command target used by the clickable Play Again result message. */
public class PlayAgainCommand extends BaseCommand {
    @CompleterData(name = "playagain")
    public List<String> playAgainCompleter(CommandArgs command) {
        List<String> completion = new ArrayList<>();
        if (command.getArgs().length == 1) {
            this.plugin.getService(KitService.class).getKits().forEach(kit -> completion.add(kit.getName()));
        }
        return completion;
    }

    @CommandData(name = "playagain", aliases = {"play-again"},
            usage = "playagain <kit>", description = "Queue for the last unranked solo kit again.")
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        PlayAgainListener.queueForPlayAgain(player,
                command.length() == 0 ? null : command.getArgs(0));
    }
}
