package dev.revere.alley.feature.arena.command;

import dev.revere.alley.feature.arena.menu.ArenaManagementMenu;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import org.bukkit.entity.Player;

/**
 * @author Alley
 * @project Alley
 * @since 02/07/2025
 *
 * Command to open the Arena Manager GUI.
 * 用于打开竞技场管理器 GUI 的命令。
 */
public class ArenaManagerCommand extends BaseCommand {

    @CommandData(
            name = "arenamanager",
            isAdminOnly = true,
            usage = "arenamanager",
            description = "Open the graphical Arena Manager."
    )
    @Override
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        new ArenaManagementMenu().openMenu(player);
    }
}
