package dev.revere.alley.feature.clan.command;

import dev.revere.alley.feature.clan.menu.ClanManagementMenu;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import org.bukkit.entity.Player;

/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 26/06/2026
 *
 * Admin command to open the Clan Manager GUI.
 * 打开公会管理器 GUI 的管理员命令。
 */
public class ClanManagerCommand extends BaseCommand {

    @CommandData(
            name = "clanmanager",
            isAdminOnly = true,
            usage = "clanmanager",
            description = "Open the graphical Clan Manager."
    )
    @Override
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        new ClanManagementMenu().openMenu(player);
    }
}
