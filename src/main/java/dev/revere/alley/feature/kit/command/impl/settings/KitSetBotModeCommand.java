package dev.revere.alley.feature.kit.command.impl.settings;

import dev.revere.alley.common.text.CC;
import dev.revere.alley.feature.bot.BotAiMode;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.kit.KitService;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.stream.Collectors;

public class KitSetBotModeCommand extends BaseCommand {
    @Override
    @CommandData(
            name = "kit.setbotmode",
            aliases = {"kit.botmode", "kit.setbotai"},
            isAdminOnly = true,
            usage = "kit setbotmode <kit> <melee|potpvp|builduhc|gomoku>",
            description = "Set the AI mode used by a kit in bot matches."
    )
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        String[] args = command.getArgs();
        if (args.length != 2) {
            command.sendUsage();
            return;
        }

        Kit kit = this.plugin.getService(KitService.class).getKit(args[0]);
        if (kit == null) {
            player.sendMessage(CC.translate("&cThat kit does not exist."));
            return;
        }

        BotAiMode mode = Arrays.stream(BotAiMode.values())
                .filter(candidate -> candidate.name().equalsIgnoreCase(args[1]))
                .findFirst()
                .orElse(null);
        if (mode == null) {
            String modes = Arrays.stream(BotAiMode.values())
                    .map(candidate -> candidate.name().toLowerCase())
                    .collect(Collectors.joining(", "));
            player.sendMessage(CC.translate("&cUnknown bot AI mode. Available: &f" + modes));
            return;
        }

        kit.setBotAiMode(mode);
        this.plugin.getService(KitService.class).saveKit(kit);
        player.sendMessage(CC.translate("&aBot AI mode for &6" + kit.getName()
                + " &ahas been set to &6" + mode.name() + "&a."));
    }
}
