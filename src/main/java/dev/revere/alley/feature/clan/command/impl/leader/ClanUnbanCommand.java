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
public class ClanUnbanCommand extends BaseCommand {
    @CommandData(name = "clan.unban", usage = "clan unban <player>", description = "Unban a player from your clan.")
    @Override
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        String[] args = command.getArgs();
        if (args.length < 1) { command.sendUsage(); return; }

        ClanService clanService = this.plugin.getService(ClanService.class);
        Clan clan = clanService.getClanByPlayer(player);
        if (clan == null) { player.sendMessage(CC.translate("&cYou are not in a clan.")); return; }
        if (!clan.isLeaderOrOfficer(player)) { player.sendMessage(CC.translate("&cOnly leaders and officers can unban.")); return; }

        OfflinePlayer target = PlayerUtil.getOfflinePlayerByName(args[0]);
        if (target == null) { player.sendMessage(CC.translate("&cPlayer not found.")); return; }
        if (!clan.getBannedPlayers().contains(target.getUniqueId())) {
            player.sendMessage(CC.translate("&cThat player is not banned."));
            return;
        }

        clanService.unbanPlayer(clan, target.getPlayer());
        player.sendMessage(CC.translate("&a" + target.getName() + " &ahas been unbanned from the clan."));
    }
}
