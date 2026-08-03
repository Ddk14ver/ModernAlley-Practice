package dev.revere.alley.feature.clan.command.impl.member;

import dev.revere.alley.common.text.CC;
import dev.revere.alley.feature.clan.Clan;
import dev.revere.alley.feature.clan.ClanService;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import org.bukkit.entity.Player;
/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 26/06/2026
 */
public class ClanChatCommand extends BaseCommand {
    @CommandData(name = "clan.chat", aliases = {"clan.c"}, usage = "clan chat <message>", description = "Send a message to your clan.")
    @Override
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        String[] args = command.getArgs();
        if (args.length < 1) { command.sendUsage(); return; }

        ClanService clanService = this.plugin.getService(ClanService.class);
        Clan clan = clanService.getClanByPlayer(player);
        if (clan == null) { player.sendMessage(CC.translate("&cYou are not in a clan.")); return; }

        if (clan.isChatMuted() && !clan.isLeaderOrOfficer(player)) {
            player.sendMessage(CC.translate("&cClan chat is currently muted."));
            return;
        }

        StringBuilder msg = new StringBuilder();
        for (String arg : args) msg.append(arg).append(" ");

        String formatted = clanService.getChatFormat()
                .replace("{player}", player.getName())
                .replace("{message}", msg.toString().trim());
        clan.broadcast(CC.translate(formatted));
    }
}
