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
public class ClanSetDescriptionCommand extends BaseCommand {
    @CommandData(name = "clan.setdescription", aliases = {"clan.desc", "clan.setdesc"}, usage = "clan setdescription <description>", description = "Set your clan's description.")
    @Override
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        String[] args = command.getArgs();
        if (args.length < 1) { command.sendUsage(); return; }

        ClanService clanService = this.plugin.getService(ClanService.class);
        Clan clan = clanService.getClanByPlayer(player);
        if (clan == null) { player.sendMessage(CC.translate("&cYou are not in a clan.")); return; }
        if (!clan.isLeaderOrOfficer(player)) { player.sendMessage(CC.translate("&cOnly leaders and officers can set the description.")); return; }

        StringBuilder desc = new StringBuilder();
        for (String arg : args) {
            desc.append(arg).append(" ");
        }
        clan.setDescription(desc.toString().trim());
        clanService.saveClan(clan);
        clan.broadcast(CC.translate("&aClan description updated!"));
    }
}
