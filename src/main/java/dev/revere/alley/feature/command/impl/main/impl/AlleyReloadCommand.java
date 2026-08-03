package dev.revere.alley.feature.command.impl.main.impl;

import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import org.bukkit.entity.Player;
/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 15/07/2026
 *
 * Opens a GUI menu with granular reload options:
 * Config files, queues, arena caches, knockback, leaderboards, or reload all.
 */
public class AlleyReloadCommand extends BaseCommand {

    @CommandData(
            name = "alley.reload",
            isAdminOnly = true,
            inGameOnly = true,
            usage = "alley.reload",
            description = "Open the reload GUI to selectively reload Alley subsystems."
    )
    @Override
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        new ReloadMenu().openMenu(player);
    }
}
