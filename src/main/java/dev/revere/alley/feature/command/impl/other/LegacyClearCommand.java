package dev.revere.alley.feature.command.impl.other;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.feature.match.MatchService;
import dev.revere.alley.feature.match.internal.MatchServiceImpl;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import dev.revere.alley.common.text.CC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 18/06/2026
 */
public class LegacyClearCommand extends BaseCommand {
    @CommandData(
            name = "legacyclear",
            isAdminOnly = true,
            usage = "legacyclear",
            description = "Clear all 1.8 legacy combat state from all players."
    )
    @Override
    public void onCommand(CommandArgs command) {
        MatchService ms = AlleyPlugin.getInstance().getService(MatchService.class);
        if (ms instanceof MatchServiceImpl impl && impl.getLegacyCombatService() != null) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                impl.getLegacyCombatService().removeAll(p);
                p.sendMessage(CC.translate("&eYour legacy combat state has been cleared."));
            }
            command.getSender().sendMessage(CC.translate("&aLegacy combat state cleared for all online players."));
        }
    }
}
