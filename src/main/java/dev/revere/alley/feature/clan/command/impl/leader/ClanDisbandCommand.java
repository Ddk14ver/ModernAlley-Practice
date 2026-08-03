package dev.revere.alley.feature.clan.command.impl.leader;

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
public class ClanDisbandCommand extends BaseCommand {
    @CommandData(name = "clan.disband", aliases = {"clan.delete", "clan.remove"}, isAdminOnly = false, usage = "clan disband [clanName]", description = "Disband your clan (or any clan if admin).")
    @Override
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        String[] args = command.getArgs();
        ClanService clanService = this.plugin.getService(ClanService.class);

        // Admin disband of any clan
        if (args.length >= 1 && player.hasPermission(this.getAdminPermission())) {
            Clan target = clanService.getClanByName(args[0]);
            if (target == null) {
                player.sendMessage(CC.translate("&cClan not found."));
                return;
            }
            clanService.disbandClan(target, player);
            player.sendMessage(CC.translate("&aClan &6" + target.getName() + " &adisbanded."));
            return;
        }

        Clan clan = clanService.getClanByPlayer(player);
        if (clan == null) { player.sendMessage(CC.translate("&cYou are not in a clan.")); return; }
        if (!clan.isLeader(player)) { player.sendMessage(CC.translate("&cOnly the clan leader can disband.")); return; }

        clanService.disbandClan(clan, player);
        player.sendMessage(CC.translate("&aYour clan has been disbanded."));
    }
}
