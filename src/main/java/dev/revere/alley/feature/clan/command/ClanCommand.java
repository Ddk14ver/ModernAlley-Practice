package dev.revere.alley.feature.clan.command;

import dev.revere.alley.common.text.CC;
import dev.revere.alley.common.text.ClickableUtil;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import org.bukkit.entity.Player;

/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 26/06/2026
 *
 * Root clan command - displays clan help pages.
 * 根公会命令 - 显示公会帮助页面。
 */
public class ClanCommand extends BaseCommand {

    @CommandData(
            name = "clan",
            aliases = {"c", "guild", "g", "team", "t"},
            usage = "clan help [page]",
            description = "Clan management commands."
    )
    @Override
    public void onCommand(CommandArgs command) {
        int page = 1;
        String[] args = command.getArgs();

        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {}
        }

        if (page < 1 || page > pages.length) {
            page = 1;
        }

        command.getSender().sendMessage("");
        command.getSender().sendMessage(CC.translate("&6&lClan Commands &8(&7Page &f" + page + "&7/&f" + pages.length + "&8)"));
        for (String line : pages[page - 1]) {
            command.getSender().sendMessage(CC.translate(line));
        }
        command.getSender().sendMessage("");

        if (command.getSender() instanceof Player player) {
            ClickableUtil.sendPageNavigation(player, page, pages.length, "/clan", false, true);
        }
    }

    private final String[][] pages = {
            {
                    " &6│ &6/clan create <name> &7| Create a clan",
                    " &6│ &6/clan disband &7| Disband your clan",
                    " &6│ &6/clan invite <player> &7| Invite a player",
                    " &6│ &6/clan accept <clanName> &7| Accept a clan invite",
                    " &6│ &6/clan leave &7| Leave your clan",
                    " &6│ &6/clan kick <player> &7| Kick a member",
            },
            {
                    " &6│ &6/clan info [clanName] &7| View clan information",
                    " &6│ &6/clan list &7| List all clans",
                    " &6│ &6/clan chat <message> &7| Send clan chat message",
                    " &6│ &6/clan c <message> &7| Alias for clan chat",
                    " &6│ &6/clan home &7| Teleport to clan home",
                    " &6│ &6/clan sethome &7| Set clan home location",
            },
            {
                    " &6│ &6/clan promote <player> &7| Promote to officer",
                    " &6│ &6/clan demote <player> &7| Demote from officer",
                    " &6│ &6/clan rename <name> &7| Rename your clan",
                    " &6│ &6/clan setcolor <color> &7| Set clan display color",
                    " &6│ &6/clan setdescription <desc> &7| Set clan description",
                    " &6│ &6/clan ban <player> &7| Ban a player from clan",
                    " &6│ &6/clan unban <player> &7| Unban a player",
            }
    };
}
