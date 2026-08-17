package dev.revere.alley.core.profile.command.player.setting.toggle;

import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.locale.internal.impl.message.GlobalMessagesLocaleImpl;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.library.assemble.AssembleService;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import org.bukkit.entity.Player;

/**
 * 切换计分板开关的命令
 * Command to toggle the scoreboard on or off.
 *
 * @author Emmy
 * @project Alley
 * @date 25/05/2024 - 23:35
 */

public class ToggleScoreboardCommand extends BaseCommand {
    @CommandData(
            name = "togglescoreboard",
            aliases = {"tsb", "togglesb"},
            cooldown = 1,
            usage = "togglescoreboard",
            description = "Toggle the scoreboard on or off"
    )
    @Override
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        Profile profile = this.getProfile(player.getUniqueId());
        boolean enabled = !profile.getProfileData().getSettingData().isScoreboardEnabled();
        profile.getProfileData().getSettingData().setScoreboardEnabled(enabled);

        AssembleService assembleService = this.plugin.getService(AssembleService.class);
        if (enabled) assembleService.createBoard(player);
        else assembleService.removeBoard(player);

        player.sendMessage(CC.translate(this.getString(GlobalMessagesLocaleImpl.PROFILE_TOGGLED_SCOREBOARD)
                .replace("{status}", enabled ? "&aenabled" : "&cdisabled"))
        );
    }
}
