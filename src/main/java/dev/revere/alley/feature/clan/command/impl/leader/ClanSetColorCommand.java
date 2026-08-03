package dev.revere.alley.feature.clan.command.impl.leader;

import dev.revere.alley.common.text.CC;
import dev.revere.alley.feature.clan.Clan;
import dev.revere.alley.feature.clan.ClanService;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 26/06/2026
 */
public class ClanSetColorCommand extends BaseCommand {
    @CommandData(name = "clan.setcolor", aliases = {"clan.color"}, usage = "clan setcolor <color>", description = "Set your clan's display color.")
    @Override
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        String[] args = command.getArgs();
        if (args.length < 1) { command.sendUsage(); return; }

        ClanService clanService = this.plugin.getService(ClanService.class);
        Clan clan = clanService.getClanByPlayer(player);
        if (clan == null) { player.sendMessage(CC.translate("&cYou are not in a clan.")); return; }
        if (!clan.isLeader(player)) { player.sendMessage(CC.translate("&cOnly the leader can set the color.")); return; }

        ChatColor color;
        try {
            color = ChatColor.valueOf(args[0].toUpperCase());
            if (!color.isColor()) throw new IllegalArgumentException();
        } catch (IllegalArgumentException e) {
            player.sendMessage(CC.translate("&cInvalid color. Available: BLACK, DARK_BLUE, DARK_GREEN, DARK_AQUA, DARK_RED, DARK_PURPLE, GOLD, GRAY, DARK_GRAY, BLUE, GREEN, AQUA, RED, LIGHT_PURPLE, YELLOW, WHITE"));
            return;
        }

        clan.setColor(color);
        clanService.saveClan(clan);
        clan.broadcast(CC.translate("&aClan color set to " + color + color.name() + "&a!"));
    }
}
