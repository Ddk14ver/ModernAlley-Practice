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
public class ClanHomeCommand extends BaseCommand {
    @CommandData(name = "clan.home", usage = "clan home", description = "Teleport to your clan's home.")
    @Override
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        ClanService clanService = this.plugin.getService(ClanService.class);
        Clan clan = clanService.getClanByPlayer(player);
        if (clan == null) { player.sendMessage(CC.translate("&cYou are not in a clan.")); return; }

        if (clan.getHome() == null) {
            player.sendMessage(CC.translate("&cYour clan has no home set. Use &e/clan sethome &cto set one."));
            return;
        }

        player.teleport(clan.getHome());
        player.sendMessage(CC.translate("&aTeleported to your clan home!"));
    }
}
