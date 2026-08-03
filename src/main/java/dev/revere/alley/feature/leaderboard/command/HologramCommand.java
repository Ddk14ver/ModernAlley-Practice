package dev.revere.alley.feature.leaderboard.command;

import dev.revere.alley.common.text.CC;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.kit.KitService;
import dev.revere.alley.feature.leaderboard.LeaderboardType;
import dev.revere.alley.feature.leaderboard.hologram.Hologram;
import dev.revere.alley.feature.leaderboard.hologram.HologramManager;
import dev.revere.alley.feature.leaderboard.hologram.LeaderboardHologram;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author Alley
 * @project Alley
 * @since 02/07/2025
 *
 * Admin command for managing leaderboard holograms.
 * 管理排行榜全息图的管理员命令。
 */
public class HologramCommand extends BaseCommand {

    @CommandData(
            name = "hologram",
            aliases = {"holo", "leaderboardholo", "lbholo"},
            isAdminOnly = true,
            usage = "hologram <create|delete|list|tp|movehere|setstat|settype|setkit|toggle>",
            description = "Manage leaderboard holograms."
    )
    @Override
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        String[] args = command.getArgs();

        if (args.length == 0) {
            sendHelp(player);
            return;
        }

        HologramManager manager = this.plugin.getService(HologramManager.class);
        String sub = args[0].toLowerCase();

        switch (sub) {
            case "create" -> handleCreate(player, args, manager);
            case "delete", "remove" -> handleDelete(player, args, manager);
            case "list" -> handleList(player, manager);
            case "tp", "teleport" -> handleTeleport(player, args, manager);
            case "movehere" -> handleMoveHere(player, args, manager);
            case "setstat" -> handleSetStat(player, args, manager);
            case "settype" -> handleSetType(player, args, manager);
            case "setkit" -> handleSetKit(player, args, manager);
            case "toggle" -> handleToggle(player, args, manager);
            default -> sendHelp(player);
        }
    }

    private void handleCreate(Player player, String[] args, HologramManager manager) {
        if (args.length < 3) {
            player.sendMessage(CC.translate("&cUsage: /hologram create <name> <type> <kit>"));
            player.sendMessage(CC.translate("&7Types: RANKED, UNRANKED, UNRANKED_MONTHLY, WIN_STREAK, FFA, TOURNAMENT"));
            player.sendMessage(CC.translate("&7Kit: kit name, or 'all' for all kits"));
            return;
        }

        String name = args[1];

        // Prevent duplicate hologram names
        if (manager.getHologram(name).isPresent()) {
            player.sendMessage(CC.translate("&cA hologram named '&6" + name + "&c' already exists!"));
            player.sendMessage(CC.translate("&7Use &e/hologram movehere " + name + " &7to relocate it, or &e/hologram delete " + name + " &7to remove it first."));
            return;
        }

        LeaderboardType type;
        try {
            type = LeaderboardType.valueOf(args[2].toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(CC.translate("&cInvalid type. Use: RANKED, UNRANKED, UNRANKED_MONTHLY, WIN_STREAK, FFA, TOURNAMENT"));
            return;
        }

        String kitName = args.length > 3 ? args[3] : null;
        if (kitName != null && !kitName.equalsIgnoreCase("all")) {
            Kit kit = this.plugin.getService(KitService.class).getKit(kitName);
            if (kit == null) {
                player.sendMessage(CC.translate("&cKit not found: " + kitName));
                return;
            }
            kitName = kit.getName();
        } else if (kitName != null && kitName.equalsIgnoreCase("all")) {
            kitName = null; // null = global/all
        }

        Location loc = player.getLocation();
        manager.createHologram(name, loc, kitName, type);
        player.sendMessage(CC.translate("&aHologram '&6" + name + "&a' created at your location!"));
    }

    private void handleDelete(Player player, String[] args, HologramManager manager) {
        if (args.length < 2) {
            player.sendMessage(CC.translate("&cUsage: /hologram delete <name>"));
            return;
        }
        String name = args[1].trim();
        Optional<Hologram> opt = manager.getHologram(name);
        if (opt.isEmpty()) {
            player.sendMessage(CC.translate("&cHologram not found: " + name));
            player.sendMessage(CC.translate("&7Use &e/hologram list &7to see all holograms."));
            return;
        }
        manager.deleteHologram(opt.get().getName());
        player.sendMessage(CC.translate("&aHologram '&6" + name + "&a' deleted."));
    }

    private void handleList(Player player, HologramManager manager) {
        java.util.List<Hologram> holos = manager.getHolograms();
        if (holos.isEmpty()) {
            player.sendMessage(CC.translate("&cNo holograms exist yet."));
            return;
        }
        player.sendMessage(CC.translate("&6&lHolograms &7(" + holos.size() + ")"));
        for (Hologram h : holos) {
            String status = h.isEnabled() ? "&a✔" : "&c✖";
            String loc = h.getBaseLocation() != null ? " &7at &f" + h.getBaseLocation().getWorld().getName() : "";
            String info = "";
            if (h instanceof LeaderboardHologram lb) {
                info = " &8[&7" + lb.getLeaderboardType().name() + "&8]";
                if (lb.getKitName() != null) info += " &8[&7" + lb.getKitName() + "&8]";
            }
            player.sendMessage(CC.translate(" " + status + " &6" + h.getName() + info + loc));
        }
    }

    private void handleTeleport(Player player, String[] args, HologramManager manager) {
        if (args.length < 2) {
            player.sendMessage(CC.translate("&cUsage: /hologram tp <name>"));
            return;
        }
        Optional<Hologram> opt = manager.getHologram(args[1]);
        if (opt.isEmpty()) {
            player.sendMessage(CC.translate("&cHologram not found: " + args[1]));
            return;
        }
        Hologram h = opt.get();
        if (h.getBaseLocation() == null) {
            player.sendMessage(CC.translate("&cThis hologram has no location."));
            return;
        }
        player.teleport(h.getBaseLocation().clone().add(0, 2, 0));
        player.sendMessage(CC.translate("&aTeleported to hologram '&6" + h.getName() + "&a'."));
    }

    private void handleMoveHere(Player player, String[] args, HologramManager manager) {
        if (args.length < 2) {
            player.sendMessage(CC.translate("&cUsage: /hologram movehere <name>"));
            return;
        }
        Optional<Hologram> opt = manager.getHologram(args[1]);
        if (opt.isEmpty()) {
            player.sendMessage(CC.translate("&cHologram not found: " + args[1]));
            return;
        }
        Hologram h = opt.get();
        h.moveTo(player.getLocation());
        manager.saveHologram(h);
        h.updateContent();
        player.sendMessage(CC.translate("&aHologram '&6" + h.getName() + "&a' moved to your location."));
    }

    private void handleSetStat(Player player, String[] args, HologramManager manager) {
        if (args.length < 3) {
            player.sendMessage(CC.translate("&cUsage: /hologram setstat <name> <number>"));
            return;
        }
        Optional<Hologram> opt = manager.getHologram(args[1]);
        if (opt.isEmpty()) {
            player.sendMessage(CC.translate("&cHologram not found: " + args[1]));
            return;
        }
        try {
            int count = Integer.parseInt(args[2]);
            if (count < 1 || count > 20) {
                player.sendMessage(CC.translate("&cStat count must be between 1 and 20."));
                return;
            }
            Hologram h = opt.get();
            h.setShowStat(count);
            manager.saveHologram(h);
            h.updateContent();
            player.sendMessage(CC.translate("&aHologram '&6" + h.getName() + "&a' now shows top &6" + count + " &aplayers."));
        } catch (NumberFormatException e) {
            player.sendMessage(CC.translate("&cInvalid number: " + args[2]));
        }
    }

    private void handleSetType(Player player, String[] args, HologramManager manager) {
        if (args.length < 3) {
            player.sendMessage(CC.translate("&cUsage: /hologram settype <name> <type>"));
            player.sendMessage(CC.translate("&7Types: RANKED, UNRANKED, UNRANKED_MONTHLY, WIN_STREAK, FFA, TOURNAMENT"));
            return;
        }
        Optional<Hologram> opt = manager.getHologram(args[1]);
        if (opt.isEmpty()) {
            player.sendMessage(CC.translate("&cHologram not found: " + args[1]));
            return;
        }
        try {
            LeaderboardType type = LeaderboardType.valueOf(args[2].toUpperCase());
            Hologram h = opt.get();
            if (h instanceof LeaderboardHologram lb) {
                lb.setLeaderboardType(type);
                manager.saveHologram(lb);
                lb.updateContent();
                player.sendMessage(CC.translate("&aType set to &6" + type.name() + " &afor hologram '&6" + h.getName() + "&a'."));
            }
        } catch (IllegalArgumentException e) {
            player.sendMessage(CC.translate("&cInvalid type."));
        }
    }

    private void handleSetKit(Player player, String[] args, HologramManager manager) {
        if (args.length < 3) {
            player.sendMessage(CC.translate("&cUsage: /hologram setkit <name> <kitName|all>"));
            return;
        }
        Optional<Hologram> opt = manager.getHologram(args[1]);
        if (opt.isEmpty()) {
            player.sendMessage(CC.translate("&cHologram not found: " + args[1]));
            return;
        }
        Hologram h = opt.get();
        if (!(h instanceof LeaderboardHologram lb)) return;

        String kitName = args[2];
        if (kitName.equalsIgnoreCase("all") || kitName.equalsIgnoreCase("global")) {
            lb.setKitName(null);
        } else {
            Kit kit = this.plugin.getService(KitService.class).getKit(kitName);
            if (kit == null) {
                player.sendMessage(CC.translate("&cKit not found: " + kitName));
                return;
            }
            lb.setKitName(kit.getName());
        }
        manager.saveHologram(lb);
        lb.updateContent();
        player.sendMessage(CC.translate("&aKit set for hologram '&6" + h.getName() + "&a'."));
    }

    private void handleToggle(Player player, String[] args, HologramManager manager) {
        if (args.length < 2) {
            player.sendMessage(CC.translate("&cUsage: /hologram toggle <name>"));
            return;
        }
        Optional<Hologram> opt = manager.getHologram(args[1]);
        if (opt.isEmpty()) {
            player.sendMessage(CC.translate("&cHologram not found: " + args[1]));
            return;
        }
        Hologram h = opt.get();
        h.setEnabled(!h.isEnabled());
        manager.saveHologram(h);
        if (h.isEnabled()) {
            h.updateContent();
            player.sendMessage(CC.translate("&aHologram '&6" + h.getName() + "&a' enabled."));
        } else {
            player.sendMessage(CC.translate("&cHologram '&6" + h.getName() + "&c' disabled."));
        }
    }

    private void sendHelp(Player player) {
        player.sendMessage("");
        player.sendMessage(CC.translate("&6&lHologram Commands"));
        player.sendMessage(CC.translate("&8&m---------------------------"));
        player.sendMessage(CC.translate(" &6│ &6/hologram create <name> <type> [kit] &7| Create"));
        player.sendMessage(CC.translate(" &6│ &6/hologram delete <name> &7| Delete"));
        player.sendMessage(CC.translate(" &6│ &6/hologram list &7| List all"));
        player.sendMessage(CC.translate(" &6│ &6/hologram tp <name> &7| Teleport to"));
        player.sendMessage(CC.translate(" &6│ &6/hologram movehere <name> &7| Move here"));
        player.sendMessage(CC.translate(" &6│ &6/hologram setstat <name> <num> &7| Top N count"));
        player.sendMessage(CC.translate(" &6│ &6/hologram settype <name> <type> &7| Change type"));
        player.sendMessage(CC.translate(" &6│ &6/hologram setkit <name> <kit> &7| Change kit"));
        player.sendMessage(CC.translate(" &6│ &6/hologram toggle <name> &7| Enable/Disable"));
        player.sendMessage(CC.translate("&8&m---------------------------"));
    }
}
