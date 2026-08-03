package dev.revere.alley.feature.leaderboard.command;

import dev.revere.alley.feature.leaderboard.menu.HologramMenu;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import org.bukkit.entity.Player;

/**
 * @author Alley
 * @project Alley
 * @since 02/07/2025
 *
 * Admin command to open the Hologram Manager GUI.
 * 打开全息图管理器 GUI 的管理员命令。
 */
public class HologramMenuCommand extends BaseCommand {

    @CommandData(
            name = "hologrammanager",
            isAdminOnly = true,
            usage = "hologrammenu",
            description = "Open the graphical Hologram Manager."
    )
    @Override
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        new HologramMenu().openMenu(player);
    }
}
