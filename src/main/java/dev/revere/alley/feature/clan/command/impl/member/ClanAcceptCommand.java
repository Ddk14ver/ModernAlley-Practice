package dev.revere.alley.feature.clan.command.impl.member;

import dev.revere.alley.common.text.CC;
import dev.revere.alley.feature.clan.Clan;
import dev.revere.alley.feature.clan.ClanInvite;
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
public class ClanAcceptCommand extends BaseCommand {
    @CommandData(name = "clan.accept", aliases = {"clan.join"}, usage = "clan accept <clanName>", description = "Accept a clan invitation.")
    @Override
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        String[] args = command.getArgs();
        if (args.length < 1) { command.sendUsage(); return; }

        ClanService clanService = this.plugin.getService(ClanService.class);
        if (clanService.getClanByPlayer(player) != null) {
            player.sendMessage(CC.translate("&cYou are already in a clan."));
            return;
        }

        ClanInvite invite = clanService.getInvite(player, args[0]);
        if (invite == null || invite.isExpired()) {
            player.sendMessage(CC.translate("&cNo valid invite found for that clan. It may have expired."));
            return;
        }

        Clan clan = clanService.getClanByName(invite.getClanName());
        if (clan == null) {
            player.sendMessage(CC.translate("&cThat clan no longer exists."));
            clanService.removeInvite(player, args[0]);
            return;
        }

        if (clan.getMemberCount() >= clanService.getMaxMembers()) {
            player.sendMessage(CC.translate("&cThat clan is full."));
            return;
        }

        if (clan.isBanned(player)) {
            player.sendMessage(CC.translate("&cYou are banned from that clan."));
            return;
        }

        clanService.addMember(clan, player);
        clan.broadcast(CC.translate("&a&l" + player.getName() + " &ahas joined the clan!"));
        player.sendMessage(CC.translate("&aWelcome to &6" + clan.getColoredName() + "&a!"));
    }
}
