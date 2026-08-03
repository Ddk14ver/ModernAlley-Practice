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
public class ClanLeaveCommand extends BaseCommand {
    @CommandData(name = "clan.leave", aliases = {"clan.quit"}, usage = "clan leave", description = "Leave your current clan.")
    @Override
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        ClanService clanService = this.plugin.getService(ClanService.class);
        Clan clan = clanService.getClanByPlayer(player);
        if (clan == null) { player.sendMessage(CC.translate("&cYou are not in a clan.")); return; }
        if (clan.isLeader(player)) {
            player.sendMessage(CC.translate("&cYou are the leader. Use &e/clan disband &cto disband the clan."));
            return;
        }

        clanService.removeMember(clan, player);
        clan.broadcast(CC.translate("&e" + player.getName() + " &chas left the clan."));
        player.sendMessage(CC.translate("&cYou have left the clan."));
    }
}
