package dev.revere.alley.feature.match.command.player;

import dev.revere.alley.core.locale.internal.impl.message.GlobalMessagesLocaleImpl;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.core.profile.enums.ProfileState;
import dev.revere.alley.feature.match.menu.SpectatorTeleportMenu;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import org.bukkit.entity.Player;

/**
 * @author Emmy
 * 作者 Emmy
 * @project Alley
 * 项目 Alley
 * @since 26/06/2025
 * 自 2025年6月26日起
 */
public class ViewPlayersCommand extends BaseCommand {
    @CommandData(
            name = "viewplayers",
            usage = "viewplayers",
            description = "View all players in the match (spectator only)."
    )
    @Override
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();

        ProfileService profileService = this.plugin.getService(ProfileService.class);
        Profile profile = profileService.getProfile(player.getUniqueId());
        if (profile.getState() != ProfileState.SPECTATING) {
            player.sendMessage(this.getString(GlobalMessagesLocaleImpl.ERROR_YOU_NOT_SPECTATING_MATCH));
            return;
        }

        new SpectatorTeleportMenu(profile.getMatch()).openMenu(player);
    }
}
