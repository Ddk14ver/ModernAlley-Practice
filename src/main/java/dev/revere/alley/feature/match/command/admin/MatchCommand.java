package dev.revere.alley.feature.match.command.admin;

import dev.revere.alley.common.text.CC;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import org.bukkit.command.CommandSender;

/**
 * @author Remi
 * 作者 Remi
 * @project Alley
 * 项目 Alley
 * @date 5/26/2024
 * 日期 2024年5月26日
 */
public class MatchCommand extends BaseCommand {
    @CommandData(
            name = "match",
            isAdminOnly = true,
            inGameOnly = false,
            usage = "match",
            description = "Manage matches"
    )
    @Override
    public void onCommand(CommandArgs args) {
        CommandSender sender = args.getSender();

        sender.sendMessage(" ");
        sender.sendMessage(CC.translate("&6&lMatch Commands Help:"));
        sender.sendMessage(CC.translate(" &6│ &6/match start &8(&7p1&8) &8(&7p2&8) &8(&7arena&8) &8(&7kit&8) &7| Start a match"));
        sender.sendMessage(CC.translate(" &6│ &6/match cancel &8(&7player&8) &7| Cancel a match"));
        sender.sendMessage(CC.translate(" &6│ &6/match info &8(&7player&8) &7| Get match info of a player"));
        sender.sendMessage(CC.translate(" &6│ &6/match pull &8(&7player&8) &8(&7target&8) &8[&7newTeam&8] &7| Pull a lobby player into the target's match"));
        sender.sendMessage(CC.translate(" &6│ &6/match revive &8(&7player&8) &8(&7silent&8) &7| Revive a spectating player in their match"));
        sender.sendMessage("");
    }
}
