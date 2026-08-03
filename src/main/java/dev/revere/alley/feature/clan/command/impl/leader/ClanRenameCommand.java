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
public class ClanRenameCommand extends BaseCommand {
    @CommandData(name = "clan.rename", usage = "clan rename <newName>", description = "Rename your clan.")
    @Override
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        String[] args = command.getArgs();
        if (args.length < 1) { command.sendUsage(); return; }

        ClanService clanService = this.plugin.getService(ClanService.class);
        Clan clan = clanService.getClanByPlayer(player);
        if (clan == null) { player.sendMessage(CC.translate("&cYou are not in a clan.")); return; }
        if (!clan.isLeader(player)) { player.sendMessage(CC.translate("&cOnly the leader can rename the clan.")); return; }

        String newName = args[0].replaceAll("&[0-9a-fk-or]", "").trim();
        if (newName.length() < 2 || newName.length() > 12) {
            player.sendMessage(CC.translate("&cClan name must be 2-12 characters."));
            return;
        }
        if (clanService.getClanByName(newName) != null) {
            player.sendMessage(CC.translate("&cA clan with that name already exists."));
            return;
        }

        String oldName = clan.getName();
        clanService.renameClan(clan, newName);
        clan.broadcast(CC.translate("&aClan renamed from &6" + oldName + " &ato &6" + newName + "&a!"));
    }
}
