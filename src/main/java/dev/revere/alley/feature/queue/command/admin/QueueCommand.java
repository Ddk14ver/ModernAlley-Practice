package dev.revere.alley.feature.queue.command.admin;

import dev.revere.alley.common.text.CC;
import dev.revere.alley.feature.kit.KitService;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import dev.revere.alley.library.command.annotation.CompleterData;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Emmy
 * @project Alley
 * @date 24/09/2024 - 20:51
 */
public class QueueCommand extends BaseCommand {
    @CompleterData(name = "queue")
    public List<String> queueCompleter(CommandArgs command) {
        List<String> completion = new ArrayList<>();
        String[] args = command.getArgs();

        if (args.length == 1) {
            if (command.getSender().hasPermission(this.getAdminPermission())) {
                completion.addAll(Arrays.asList("force", "reload"));
            }
            return completion;
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("force")) {
            this.plugin.getService(KitService.class).getKits().forEach(kit -> completion.add(kit.getName()));
            return completion;
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("force")) {
            completion.addAll(List.of("true", "false"));
        }

        return completion;
    }

    @CommandData(
            name = "queue",
            isAdminOnly = true,
            inGameOnly = false,
            usage = "queue",
            description = "Main queue command"
    )
    @Override
    public void onCommand(CommandArgs command) {
        CommandSender sender = command.getSender();

        sender.sendMessage(" ");
        sender.sendMessage(CC.translate("&6&lQueue Commands Help:"));
        sender.sendMessage(CC.translate(" &6│ &6/queue force &8(&7player&8) &8(&7kit&8) &8<&7ranked&8> &7| Force a player into a queue"));
        //sender.sendMessage(CC.translate(" &6│ &6/queue remove &8(&7player&8) &7| Remove a player from queue"));
        // 将玩家移出队列
        sender.sendMessage(CC.translate(" &6│ &6/queue reload &7| Reload the queues"));
        sender.sendMessage("");
    }
}