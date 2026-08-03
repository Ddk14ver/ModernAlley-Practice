package dev.revere.alley.feature.host.command;

import dev.revere.alley.core.locale.internal.impl.message.GlobalMessagesLocaleImpl;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.core.profile.enums.ProfileState;
import dev.revere.alley.feature.host.menu.HostMenu;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import org.bukkit.entity.Player;

/**
 * @author Emmy
 * 作者：Emmy
 * @project Alley
 * 项目：Alley
 * @date 08/06/2024 - 21:31
 * 日期：2024年6月8日 - 21:31
 */
public class HostCommand extends BaseCommand {
    @CommandData(
            name = "host",
            permission = "alley.command.donator.host",
            usage = "host",
            description = "Open the host menu"
    )
    @Override
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();

        Profile profile = this.plugin.getService(ProfileService.class).getProfile(player.getUniqueId());
        if (profile.getState() != ProfileState.LOBBY) {
            player.sendMessage(this.getString(GlobalMessagesLocaleImpl.ERROR_YOU_MUST_BE_IN_LOBBY));
            return;
        }

        new HostMenu().openMenu(player);
    }
}
