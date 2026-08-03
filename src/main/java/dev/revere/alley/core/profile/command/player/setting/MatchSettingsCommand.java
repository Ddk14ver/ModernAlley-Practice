package dev.revere.alley.core.profile.command.player.setting;

import dev.revere.alley.common.constants.MessageConstant;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import org.bukkit.entity.Player;

/**
 * 打开比赛设置菜单的命令
 * Command to open the match settings menu.
 *
 * @author Emmy
 * @project Alley
 * @date 19/05/2024 - 11:27
 */
public class MatchSettingsCommand extends BaseCommand {
    @CommandData(
            name = "matchsettings",
            usage = "matchsettings",
            description = "Open the match settings menu."
    )
    @Override
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();

        // 比赛设置菜单（功能开发中）
        // Match settings menu (in development)
        //new MatchSettingsMenu().openMenu(player);
        player.sendMessage(MessageConstant.IN_DEVELOPMENT);
    }
}