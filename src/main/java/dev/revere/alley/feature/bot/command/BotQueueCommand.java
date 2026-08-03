package dev.revere.alley.feature.bot.command;

import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.core.profile.enums.ProfileState;
import dev.revere.alley.feature.party.PartyService;
import dev.revere.alley.feature.queue.menu.sub.BotQueueMenu;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import org.bukkit.entity.Player;

public class BotQueueCommand extends BaseCommand {
    @Override
    @CommandData(
            name = "botqueue",
            aliases = {"botduel", "bots"},
            usage = "botqueue",
            description = "Open the bot duel menu."
    )
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        Profile profile = this.plugin.getService(ProfileService.class).getProfile(player.getUniqueId());
        if (profile == null || profile.getState() != ProfileState.LOBBY) {
            player.sendMessage(CC.translate("&cYou must be in the lobby to start a bot duel."));
            return;
        }
        if (this.plugin.getService(PartyService.class).getParty(player) != null) {
            player.sendMessage(CC.translate("&cLeave your party before starting a bot duel."));
            return;
        }

        new BotQueueMenu().openMenu(player);
    }
}
