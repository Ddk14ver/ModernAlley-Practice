package dev.revere.alley.feature.clan.command.impl.leader;

import dev.revere.alley.common.PlayerUtil;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.feature.clan.Clan;
import dev.revere.alley.feature.clan.ClanService;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 26/06/2026
 */
public class ClanKickCommand extends BaseCommand {
    @CommandData(name = "clan.kick", usage = "clan kick <player>", description = "Kick a member from your clan.")
    @Override
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        String[] args = command.getArgs();
        if (args.length < 1) { command.sendUsage(); return; }

        ClanService clanService = this.plugin.getService(ClanService.class);
        Clan clan = clanService.getClanByPlayer(player);
        if (clan == null) { player.sendMessage(CC.translate("&cYou are not in a clan.")); return; }
        if (!clan.isLeaderOrOfficer(player)) { player.sendMessage(CC.translate("&cOnly leaders and officers can kick members.")); return; }

        OfflinePlayer targetOff = PlayerUtil.getOfflinePlayerByName(args[0]);
        if (targetOff == null) { player.sendMessage(CC.translate("&cPlayer not found.")); return; }

        if (!clan.isMember(targetOff.getPlayer())) {
            player.sendMessage(CC.translate("&cThat player is not in your clan."));
            return;
        }
        if (clan.isLeader(targetOff.getPlayer())) {
            player.sendMessage(CC.translate("&cYou cannot kick the leader."));
            return;
        }
        // Officers can only be kicked by the leader
        if (clan.isOfficer(targetOff.getPlayer()) && !clan.isLeader(player)) {
            player.sendMessage(CC.translate("&cOnly the leader can kick officers."));
            return;
        }

        Player target = targetOff.getPlayer();
        clanService.removeMember(clan, target);
        if (target != null && target.isOnline()) {
            target.sendMessage(CC.translate("&cYou have been kicked from clan &6" + clan.getName() + "&c."));
        }
        clan.broadcast(CC.translate("&c" + targetOff.getName() + " &chas been kicked from the clan."));
    }
}
