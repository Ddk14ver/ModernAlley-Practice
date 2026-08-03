package dev.revere.alley.feature.command.impl.other.troll;

import dev.revere.alley.common.logger.Logger;
import dev.revere.alley.core.locale.internal.impl.message.GlobalMessagesLocaleImpl;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Emmy
 * @project Alley
 * @date 28/10/2024 - 09:00
 */
public class TrollCommand extends BaseCommand {
    @CommandData(
            name = "troll",
            isAdminOnly = true,
            inGameOnly = false,
            usage = "troll <player>",
            description = "Opens demo screen for target player"
            // 为目标玩家打开演示屏幕
    )
    @Override
    public void onCommand(CommandArgs command) {
        CommandSender sender = command.getSender();
        String[] args = command.getArgs();

        if (args.length < 1) {
            command.sendUsage();
            return;
        }

        Player targetPlayer = this.plugin.getServer().getPlayer(args[0]);
        if (targetPlayer == null) {
            sender.sendMessage(this.getString(GlobalMessagesLocaleImpl.ERROR_INVALID_PLAYER));
            return;
        }

        try {
            // In modern Minecraft, we can show the demo screen using this method
            // 在现代Minecraft中，我们可以使用此方法显示演示屏幕
            targetPlayer.showDemoScreen();
        } catch (Exception ex) {
            Logger.error("An error occurred while trying to troll " + targetPlayer.getName() + ": " + ex.getMessage());
        }

        sender.sendMessage(this.getString(GlobalMessagesLocaleImpl.TROLL_PLAYER_DEMO_MENU_OPENED)
                .replace("{name-color}", String.valueOf(this.getProfile(targetPlayer.getUniqueId()).getNameColor()))
                .replace("{player}", targetPlayer.getName())
        );
    }
}