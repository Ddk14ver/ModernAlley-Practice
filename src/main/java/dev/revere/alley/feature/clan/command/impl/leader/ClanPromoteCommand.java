package dev.revere.alley.feature.clan.command.impl.leader;

import dev.revere.alley.common.PlayerUtil;
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
public class ClanPromoteCommand extends BaseCommand {
    @CommandData(name = "clan.promote", usage = "clan promote <player>", description = "Promote a member to officer.")
    @Override
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        String[] args = command.getArgs();
        if (args.length < 1) { command.sendUsage(); return; }

        ClanService clanService = this.plugin.getService(ClanService.class);
        Clan clan = clanService.getClanByPlayer(player);
        if (clan == null) { player.sendMessage(CC.translate("&cYou are not in a clan.")); return; }
        if (!clan.isLeader(player)) { player.sendMessage(CC.translate("&cOnly the leader can promote members.")); return; }

        Player target = this.plugin.getServer().getPlayer(args[0]);
        if (target == null) { player.sendMessage(CC.translate("&cPlayer not found or offline.")); return; }
        if (!clan.isMember(target)) { player.sendMessage(CC.translate("&cThat player is not in your clan.")); return; }
        if (clan.isOfficer(target)) { player.sendMessage(CC.translate("&cThat player is already an officer.")); return; }
        if (clan.isLeader(target)) { player.sendMessage(CC.translate("&cThe leader is already the highest rank.")); return; }

        clanService.promoteToOfficer(clan, target);
        clan.broadcast(CC.translate("&a" + target.getName() + " &ahas been promoted to &eOfficer&a!"));
    }
}
