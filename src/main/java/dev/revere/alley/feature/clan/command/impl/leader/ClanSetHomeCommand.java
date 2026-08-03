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
public class ClanSetHomeCommand extends BaseCommand {
    @CommandData(name = "clan.sethome", usage = "clan sethome", description = "Set your clan's home location.")
    @Override
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        ClanService clanService = this.plugin.getService(ClanService.class);
        Clan clan = clanService.getClanByPlayer(player);
        if (clan == null) { player.sendMessage(CC.translate("&cYou are not in a clan.")); return; }
        if (!clan.isLeaderOrOfficer(player)) { player.sendMessage(CC.translate("&cOnly leaders and officers can set the home.")); return; }

        clan.setHome(player.getLocation());
        clanService.saveClan(clan);
        clan.broadcast(CC.translate("&aClan home has been set by &6" + player.getName() + "&a!"));
    }
}
