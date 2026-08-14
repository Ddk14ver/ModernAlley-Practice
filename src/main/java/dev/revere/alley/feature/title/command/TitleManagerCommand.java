package dev.revere.alley.feature.title.command;

import dev.revere.alley.common.text.CC;
import dev.revere.alley.feature.division.Division;
import dev.revere.alley.feature.division.DivisionService;
import dev.revere.alley.feature.title.TitleService;
import dev.revere.alley.feature.title.internal.TitleServiceImpl;
import dev.revere.alley.feature.title.menu.TitleManagementMenu;
import dev.revere.alley.feature.title.model.TitleRecord;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import dev.revere.alley.library.command.annotation.CompleterData;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Alley
 * @project Alley
 * @since 03/07/2025
 */
public class TitleManagerCommand extends BaseCommand {

    @CompleterData(name = "titlemanager")
    public List<String> titleManagerCompleter(CommandArgs command) {
        List<String> completion = new ArrayList<>();
        String[] args = command.getArgs();

        if (!command.getSender().hasPermission(this.getAdminPermission())) {
            return completion;
        }

        if (args.length == 1) {
            completion.addAll(Arrays.asList("help", "create", "delete", "rename", "setprefix", "setrequired"));
            return completion;
        }

        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("delete") || subCommand.equals("rename")
                    || subCommand.equals("setprefix") || subCommand.equals("setrequired")) {
                this.plugin.getService(TitleService.class).getTitles().values()
                        .forEach(title -> completion.add(title.getName()));
            }
            return completion;
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("setrequired")) {
            this.plugin.getService(DivisionService.class).getDivisions()
                    .forEach(division -> completion.add(division.getName()));
        }

        return completion;
    }

    @CommandData(
            name = "titlemanager",
            isAdminOnly = true,
            usage = "titlemanager <help|create|delete|rename|setprefix|setrequired>",
            description = "Manage all titles via GUI or subcommands."
    )
    @Override
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        String[] args = command.getArgs();

        if (args.length == 0) {
            new TitleManagementMenu().openMenu(player);
            return;
        }

        TitleServiceImpl titleService = (TitleServiceImpl) this.plugin.getService(TitleService.class);

        switch (args[0].toLowerCase()) {
            case "help" -> sendHelp(player);
            case "create" -> handleCreate(player, args, titleService);
            case "delete" -> handleDelete(player, args, titleService);
            case "rename" -> handleRename(player, args, titleService);
            case "setprefix" -> handleSetPrefix(player, args, titleService);
            case "setrequired" -> handleSetRequired(player, args, titleService);
            default -> new TitleManagementMenu().openMenu(player);
        }
    }

    private void handleCreate(Player player, String[] args, TitleServiceImpl titleService) {
        if (args.length < 3) {
            player.sendMessage(CC.translate("&cUsage: /titlemanager create <name> <prefix>"));
            player.sendMessage(CC.translate("&7Creates a custom title not tied to any kit."));
            return;
        }

        String name = args[1];
        StringBuilder prefix = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            prefix.append(args[i]).append(" ");
        }

        TitleRecord existing = titleService.getTitle(name);
        if (existing != null) {
            player.sendMessage(CC.translate("&cA title with that name already exists."));
            return;
        }

        titleService.createCustomTitle(name, prefix.toString().trim());
        player.sendMessage(CC.translate("&aCustom title '&6" + name + "&a' created with prefix: " + prefix.toString().trim()));
    }

    private void handleDelete(Player player, String[] args, TitleServiceImpl titleService) {
        if (args.length < 2) {
            player.sendMessage(CC.translate("&cUsage: /titlemanager delete <name>"));
            return;
        }

        TitleRecord title = titleService.getTitle(args[1]);
        if (title == null) {
            player.sendMessage(CC.translate("&cTitle not found: " + args[1]));
            return;
        }

        if (title.getKit() != null) {
            player.sendMessage(CC.translate("&cThis is a system-generated title and cannot be deleted."));
            player.sendMessage(CC.translate("&7Only custom titles (not tied to a kit) can be deleted."));
            return;
        }

        String name = title.getName();
        titleService.deleteTitle(title);
        player.sendMessage(CC.translate("&aCustom title '&6" + name + "&a' has been deleted."));
    }

    private void handleRename(Player player, String[] args, TitleServiceImpl titleService) {
        if (args.length < 3) {
            player.sendMessage(CC.translate("&cUsage: /titlemanager rename <currentName> <newName>"));
            return;
        }

        TitleRecord title = titleService.getTitle(args[1]);
        if (title == null) {
            player.sendMessage(CC.translate("&cTitle not found: " + args[1]));
            return;
        }

        String oldName = title.getName();
        titleService.renameTitle(title, args[2]);
        player.sendMessage(CC.translate("&aTitle renamed from '&6" + oldName + "&a' to '&6" + args[2] + "&a'."));
    }

    private void handleSetPrefix(Player player, String[] args, TitleServiceImpl titleService) {
        if (args.length < 3) {
            player.sendMessage(CC.translate("&cUsage: /titlemanager setprefix <name> <prefix>"));
            return;
        }

        TitleRecord title = titleService.getTitle(args[1]);
        if (title == null) {
            player.sendMessage(CC.translate("&cTitle not found: " + args[1]));
            return;
        }

        StringBuilder prefix = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            prefix.append(args[i]).append(" ");
        }
        title.setPrefix(prefix.toString().trim());
        titleService.saveTitle(title);
        player.sendMessage(CC.translate("&aPrefix for '&6" + args[1] + "&a' set to: " + prefix.toString().trim()));
    }

    private void handleSetRequired(Player player, String[] args, TitleServiceImpl titleService) {
        if (args.length < 3) {
            player.sendMessage(CC.translate("&cUsage: /titlemanager setrequired <name> <division>"));
            return;
        }

        TitleRecord title = titleService.getTitle(args[1]);
        if (title == null) {
            player.sendMessage(CC.translate("&cTitle not found: " + args[1]));
            return;
        }

        Division division = this.plugin.getService(DivisionService.class).getDivision(args[2]);
        if (division == null) {
            player.sendMessage(CC.translate("&cDivision not found: " + args[2]));
            return;
        }

        title.setRequiredDivision(division);
        titleService.saveTitle(title);
        player.sendMessage(CC.translate("&aRequired division for '&6" + args[1] + "&a' set to '&6" + division.getName() + "&a'."));
    }

    private void sendHelp(Player player) {
        player.sendMessage("");
        player.sendMessage(CC.translate("&6&lTitle Manager Commands"));
        player.sendMessage(CC.translate("&8&m----------------------------------------"));
        player.sendMessage(CC.translate(" &6│ &6/titlemanager &7| Open the GUI"));
        player.sendMessage(CC.translate(" &6│ &6/titlemanager help &7| Show this help"));
        player.sendMessage(CC.translate(" &6│ &6/titlemanager create <name> <prefix> &7| Create custom title"));
        player.sendMessage(CC.translate(" &6│ &6/titlemanager delete <name> &7| Delete a custom title"));
        player.sendMessage(CC.translate(" &6│ &6/titlemanager rename <name> <newName> &7| Rename a title"));
        player.sendMessage(CC.translate(" &6│ &6/titlemanager setprefix <name> <prefix> &7| Set title prefix"));
        player.sendMessage(CC.translate(" &6│ &6/titlemanager setrequired <name> <div> &7| Set required division"));
        player.sendMessage(CC.translate("&8&m----------------------------------------"));
        player.sendMessage(CC.translate("&7In the GUI you can also: toggle enable/disable,"));
        player.sendMessage(CC.translate("&7adjust slot position, set purchasable, and more."));
    }
}
