package dev.revere.alley.feature.kit.command;

import dev.revere.alley.feature.kit.menu.KitManagementMenu;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import org.bukkit.entity.Player;

/**
 * @author Alley
 * @project Alley
 * @since 02/07/2025
 *
 * Command to open the Kit Manager GUI.
 * 用于打开工具包管理器 GUI 的命令。
 */
public class KitManagerCommand extends BaseCommand {

    @CommandData(
            name = "kitmanager",
            isAdminOnly = true,
            usage = "kitmanager",
            description = "Open the graphical Kit Manager."
    )
    @Override
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        new KitManagementMenu().openMenu(player);
    }
}
