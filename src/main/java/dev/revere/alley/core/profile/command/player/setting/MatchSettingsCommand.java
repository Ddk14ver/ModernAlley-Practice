package dev.revere.alley.core.profile.command.player.setting;

import dev.revere.alley.core.profile.menu.setting.MatchSettingsMenu;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import org.bukkit.entity.Player;

/** Opens the player's personal match settings menu. */
public class MatchSettingsCommand extends BaseCommand {
    @CommandData(
            name = "matchsettings",
            usage = "matchsettings",
            description = "Open the match settings menu."
    )
    @Override
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        new MatchSettingsMenu().openMenu(player);
    }
}
