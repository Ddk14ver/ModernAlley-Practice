package dev.revere.alley.core.profile.data.command.ranked;

import dev.revere.alley.common.text.CC;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * @author Emmy
 * @project Alley
 * @since 13/03/2025
 */
public class RankedCommand extends BaseCommand {


    @CommandData(
            name = "ranked",
            isAdminOnly = true,
            usage = "ranked",
            description = "Manage ranked allowance."
            // 管理排位赛权限。
    )
    @Override
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();

        Arrays.asList(
                " ",
                "&6&lRanked Commands Help:",
                " &6│ &6/ranked ban &8(&7player&8) &7| Ban a player from ranked matches.",
                //  &6│ &6/ranked ban &8(&7玩家&8) &7| 禁止玩家参与排位赛。
                " &6│ &6/ranked unban &8(&7player&8) &7| Unban a player from ranked matches.",
                //  &6│ &6/ranked unban &8(&7玩家&8) &7| 解除玩家的排位赛封禁。
                " "
        ).forEach(message -> player.sendMessage(CC.translate(message)));
    }
}
