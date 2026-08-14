package dev.revere.alley.feature.arena.command.impl.kit;

import dev.revere.alley.core.locale.internal.impl.message.GlobalMessagesLocaleImpl;
import dev.revere.alley.feature.arena.Arena;
import dev.revere.alley.feature.arena.ArenaService;
import dev.revere.alley.feature.arena.ArenaType;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.kit.KitService;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import dev.revere.alley.library.command.annotation.CompleterData;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Remi
 * @project Alley
 * @date 5/20/2024
 */
public class ArenaAddKitCommand extends BaseCommand {
    @CompleterData(name = "arena.addkit")
    public List<String> arenaAddKitCompleter(CommandArgs command) {
        List<String> completion = new ArrayList<>();
        String[] args = command.getArgs();

        if (!command.getPlayer().hasPermission(this.getAdminPermission())) {
            return completion;
        }

        if (args.length == 1) {
            this.plugin.getService(ArenaService.class).getArenas().forEach(arena -> completion.add(arena.getName()));
        } else if (args.length == 2) {
            this.plugin.getService(KitService.class).getKits().forEach(kit -> completion.add(kit.getName()));
        }

        return completion;
    }

    @CommandData(
            name = "arena.addkit",
            isAdminOnly = true,
            usage = "arena addkit <arenaName> <kitName>",
            description = "Adds a kit to an arena"
            // 向竞技场添加一个套件
    )
    @Override
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        String[] args = command.getArgs();

        if (args.length < 2) {
            command.sendUsage();
            return;
        }

        String arenaName = args[0];
        String kitName = args[1];

        ArenaService arenaService = this.plugin.getService(ArenaService.class);
        Arena arena = arenaService.getArenaByName(arenaName);
        if (arena == null) {
            player.sendMessage(this.getString(GlobalMessagesLocaleImpl.ARENA_NOT_FOUND).replace("{arena-name}", arenaName));
            return;
        }

        if (arena.getType() == ArenaType.FFA) {
            player.sendMessage(this.getString(GlobalMessagesLocaleImpl.ARENA_CANNOT_ADD_KITS_TO_FFA));
            return;
        }

        Kit kit = this.plugin.getService(KitService.class).getKit(kitName);
        if (kit == null) {
            player.sendMessage(this.getString(GlobalMessagesLocaleImpl.KIT_NOT_FOUND).replace("{kit-name}", kitName));
            return;
        }

        if (arena.getKits().contains(kit.getName())) {
            player.sendMessage(this.getString(GlobalMessagesLocaleImpl.ARENA_KIT_ALREADY_ADDED)
                    .replace("{arena-name}", arenaName)
                    .replace("{kit-name}", kitName)
            );
            return;
        }

        arena.getKits().add(kit.getName());
        arenaService.saveArena(arena);
        player.sendMessage(this.getString(GlobalMessagesLocaleImpl.ARENA_KIT_ADDED)
                .replace("{arena-name}", arenaName)
                .replace("{kit-name}", kitName)
        );
    }
}