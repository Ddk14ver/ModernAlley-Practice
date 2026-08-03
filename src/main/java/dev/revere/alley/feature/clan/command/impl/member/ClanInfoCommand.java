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
public class ClanInfoCommand extends BaseCommand {
    @CommandData(name = "clan.info", aliases = {"clan.show", "clan.i"}, usage = "clan info [clanName]", description = "View clan information.")
    @Override
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        String[] args = command.getArgs();
        ClanService clanService = this.plugin.getService(ClanService.class);
        Clan clan;

        if (args.length >= 1) {
            clan = clanService.getClanByName(args[0]);
            if (clan == null) {
                player.sendMessage(CC.translate("&cClan not found."));
                return;
            }
        } else {
            clan = clanService.getClanByPlayer(player);
            if (clan == null) {
                player.sendMessage(CC.translate("&cYou are not in a clan. Use &e/clan info <name> &cto view another clan."));
                return;
            }
        }

        player.sendMessage("");
        player.sendMessage(CC.translate("&6&l" + clan.getColoredName() + " &7- &fClan Information"));
        player.sendMessage(CC.translate("&8&m---------------------------"));
        player.sendMessage(CC.translate(" &6│ &7Description: &f" + clan.getDescription()));
        player.sendMessage(CC.translate(" &6│ &7Leader: &f" + (this.plugin.getServer().getOfflinePlayer(clan.getLeader()).getName() != null ? this.plugin.getServer().getOfflinePlayer(clan.getLeader()).getName() : "Unknown")));
        player.sendMessage(CC.translate(" &6│ &7Members: &f" + clan.getOnlineCount() + "&7/&f" + clan.getMemberCount() + " &7online"));
        player.sendMessage(CC.translate(" &6│ &7Points: &f" + clan.getPoints()));
        player.sendMessage(CC.translate(" &6│ &7Color: " + clan.getColor() + clan.getColor().name()));
        player.sendMessage(CC.translate(" &6│ &7Created: &f" + new java.text.SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date(clan.getCreatedAt()))));
        if (clan.getHome() != null) {
            player.sendMessage(CC.translate(" &6│ &7Home: &fSet"));
        }
        player.sendMessage(CC.translate(" &6│ &7Members: &f" + clan.getMemberListFormatted()));
        player.sendMessage(CC.translate("&8&m---------------------------"));
    }
}
