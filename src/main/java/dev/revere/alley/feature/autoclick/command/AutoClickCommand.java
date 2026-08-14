package dev.revere.alley.feature.autoclick.command;

import dev.revere.alley.feature.autoclick.AutoClickService;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/** Toggles the opt-in server-side autoclick session. */
public final class AutoClickCommand extends BaseCommand {

    @CommandData(
            name = "autoclick",
            aliases = {"ac"},
            usage = "autoclick",
            description = "Toggle the server-side autoclick test feature."
    )
    @Override
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        if (player == null) return;

        AutoClickService service = this.plugin.getService(AutoClickService.class);
        boolean enabled = service.toggle(player);
        player.sendMessage(enabled
                ? ChatColor.GREEN + "AutoClick enabled. Hold left click on the probe to attack."
                : ChatColor.YELLOW + "AutoClick disabled.");
    }
}
