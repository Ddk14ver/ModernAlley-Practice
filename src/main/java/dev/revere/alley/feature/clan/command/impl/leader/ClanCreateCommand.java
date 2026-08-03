package dev.revere.alley.feature.clan.command.impl.leader;

import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
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
public class ClanCreateCommand extends BaseCommand {
    @CommandData(name = "clan.create", usage = "clan create <name>", description = "Create a new clan.")
    @Override
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        String[] args = command.getArgs();
        if (args.length < 1) { command.sendUsage(); return; }

        String name = args[0].replaceAll("&[0-9a-fk-or]", "").trim();
        if (name.length() < 2 || name.length() > 12) {
            player.sendMessage(CC.translate("&cClan name must be 2-12 characters."));
            return;
        }

        ClanService clanService = this.plugin.getService(ClanService.class);
        if (clanService.getClanByName(name) != null) {
            player.sendMessage(CC.translate("&cA clan with that name already exists."));
            return;
        }

        Profile profile = this.plugin.getService(ProfileService.class).getProfile(player.getUniqueId());
        if (clanService.getClanByPlayer(player) != null) {
            player.sendMessage(CC.translate("&cYou are already in a clan!"));
            return;
        }

        clanService.createClan(name, player);
        player.sendMessage(CC.translate("&aClan &6" + name + " &acreated! Use &e/clan invite <player> &ato recruit members."));
    }
}
