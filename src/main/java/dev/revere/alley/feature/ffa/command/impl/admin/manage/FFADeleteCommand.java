package dev.revere.alley.feature.ffa.command.impl.admin.manage;

import dev.revere.alley.core.locale.internal.impl.message.GlobalMessagesLocaleImpl;
import dev.revere.alley.feature.ffa.FFAService;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.kit.KitService;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import org.bukkit.entity.Player;

public class FFADeleteCommand extends BaseCommand {
    @CommandData(
            name = "ffa.delete",
            isAdminOnly = true,
            usage = "ffa delete <kitName>",
            description = "Delete a kit's persistent FFA configuration."
    )
    @Override
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        String[] args = command.getArgs();

        if (args.length != 1) {
            command.sendUsage();
            return;
        }

        KitService kitService = this.plugin.getService(KitService.class);
        Kit kit = kitService.getKit(args[0]);
        if (kit == null) {
            player.sendMessage(this.getString(GlobalMessagesLocaleImpl.KIT_NOT_FOUND)
                    .replace("{kit-name}", args[0]));
            return;
        }

        FFAService ffaService = this.plugin.getService(FFAService.class);
        boolean hasFfaConfiguration = kit.isFfaEnabled()
                || ffaService.getFFAMatch(kit.getName()) != null
                || (kit.getFfaArenaName() != null && !kit.getFfaArenaName().isEmpty())
                || kit.getFfaSlot() != 0
                || kit.getMaxFfaPlayers() != 20;

        if (!hasFfaConfiguration) {
            player.sendMessage(this.getString(GlobalMessagesLocaleImpl.FFA_NOT_FOUND)
                    .replace("{ffa-name}", kit.getName()));
            return;
        }

        ffaService.removeFFAMatch(kit);

        kit.setFfaEnabled(false);
        kit.setFfaArenaName("");
        kit.setFfaSlot(0);
        kit.setMaxFfaPlayers(20);
        kitService.saveKit(kit);

        player.sendMessage(this.getString(GlobalMessagesLocaleImpl.FFA_DELETED)
                .replace("{kit-name}", kit.getName()));
    }
}
