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
public class ClanBanCommand extends BaseCommand {
    @CommandData(name = "clan.ban", usage = "clan ban <player>", description = "Ban a player from your clan.")
    @Override
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        String[] args = command.getArgs();
        if (args.length < 1) { command.sendUsage(); return; }

        ClanService clanService = this.plugin.getService(ClanService.class);
        Clan clan = clanService.getClanByPlayer(player);
        if (clan == null) { player.sendMessage(CC.translate("&cYou are not in a clan.")); return; }
        if (!clan.isLeaderOrOfficer(player)) { player.sendMessage(CC.translate("&cOnly leaders and officers can ban.")); return; }

        OfflinePlayer target = PlayerUtil.getOfflinePlayerByName(args[0]);
        if (target == null) { player.sendMessage(CC.translate("&cPlayer not found.")); return; }
        if (clan.isLeader(target.getPlayer())) { player.sendMessage(CC.translate("&cYou cannot ban the leader.")); return; }
        if (target.getUniqueId().equals(player.getUniqueId())) { player.sendMessage(CC.translate("&cYou cannot ban yourself.")); return; }

        clanService.banPlayer(clan, target.getPlayer());
        clan.broadcast(CC.translate("&c" + target.getName() + " &chas been banned from the clan."));
    }
}
