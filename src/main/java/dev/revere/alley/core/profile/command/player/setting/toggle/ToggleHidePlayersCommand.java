package dev.revere.alley.core.profile.command.player.setting.toggle;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.feature.visibility.VisibilityService;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import org.bukkit.entity.Player;
/**
 * 切换显示大厅玩家的命令
 *
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 20/07/2026
 */
public class ToggleHidePlayersCommand extends BaseCommand {
    @CommandData(
            name = "togglehideplayers",
            cooldown = 1,
            usage = "togglehideplayers",
            description = "Toggle hiding other players in the lobby"
    )
    @Override
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        Profile profile = this.getProfile(player.getUniqueId());
        boolean current = profile.getProfileData().getSettingData().isHidePlayersEnabled();
        profile.getProfileData().getSettingData().setHidePlayersEnabled(!current);

        player.sendMessage(CC.translate("&6&lPlayers Hidden: " + (!current ? "&aEnabled" : "&cDisabled")));

        // Immediately update visibility for all players
        AlleyPlugin.getInstance().getService(VisibilityService.class).updateVisibility(player);
    }
}
