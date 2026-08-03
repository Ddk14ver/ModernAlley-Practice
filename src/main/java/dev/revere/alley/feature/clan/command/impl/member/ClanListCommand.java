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
public class ClanListCommand extends BaseCommand {
    @CommandData(name = "clan.list", usage = "clan list", description = "List all clans.")
    @Override
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        ClanService clanService = this.plugin.getService(ClanService.class);
        java.util.List<Clan> clans = clanService.getClans();

        if (clans.isEmpty()) {
            player.sendMessage(CC.translate("&cThere are no clans yet. Create one with &e/clan create <name>&c!"));
            return;
        }

        player.sendMessage("");
        player.sendMessage(CC.translate("&6&lClans &7(" + clans.size() + ")"));
        player.sendMessage(CC.translate("&8&m---------------------------"));

        for (Clan clan : clans) {
            player.sendMessage(CC.translate(" &6│ " + clan.getColoredName() + " &7- &f" + clan.getOnlineCount() + "&7/&f" + clan.getMemberCount() + " &7online &8| &7" + clan.getDescription()));
        }

        player.sendMessage(CC.translate("&8&m---------------------------"));
    }
}
