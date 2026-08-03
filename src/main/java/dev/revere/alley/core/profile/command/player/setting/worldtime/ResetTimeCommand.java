package dev.revere.alley.core.profile.command.player.setting.worldtime;

import dev.revere.alley.core.locale.internal.impl.message.GlobalMessagesLocaleImpl;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import org.bukkit.entity.Player;

/**
 * 重置个人世界时间为服务器时间的命令
 * Command to reset personal world time to the server time.
 *
 * @author Emmy
 * @project Alley
 * @date 02/06/2024 - 10:59
 */
public class ResetTimeCommand extends BaseCommand {
    @CommandData(
            name = "resettime",
            aliases = "currenttime",
            cooldown = 1,
            usage = "resettime",
            description = "Reset your personal world time to the server time."
    )
    @Override
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        Profile profile = this.getProfile(player.getUniqueId());

        profile.getProfileData().getSettingData().setTimeDefault(player);
        player.sendMessage(this.getString(GlobalMessagesLocaleImpl.PROFILE_WORLD_TIME_RESET));
    }
}