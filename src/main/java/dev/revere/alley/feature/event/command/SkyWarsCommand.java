package dev.revere.alley.feature.event.command;

import dev.revere.alley.common.text.CC;
import dev.revere.alley.feature.arena.Arena;
import dev.revere.alley.feature.arena.ArenaService;
import dev.revere.alley.feature.arena.internal.types.StandAloneArena;
import dev.revere.alley.feature.event.skywars.SkyWarsLoot;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.kit.KitService;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

/** Administrator setup commands for dedicated SkyWars arenas and their resource kits. */
public class SkyWarsCommand extends BaseCommand {
    @Override
    @CommandData(name = "skywars", aliases = {"skywarsadmin"}, isAdminOnly = true,
            usage = "skywars", description = "Shows SkyWars arena setup commands.")
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        player.sendMessage(CC.translate("&b&lSkyWars Setup"));
        player.sendMessage(CC.translate(" &b/skywars arena set <arena> &7- Mark a standalone arena as SkyWars."));
        player.sendMessage(CC.translate(" &b/skywars spawn add <arena> &7- Add a spawn at your location."));
        player.sendMessage(CC.translate(" &b/skywars spawn remove <arena> <number> &7- Remove a spawn."));
        player.sendMessage(CC.translate(" &b/skywars spawn list <arena> &7- List configured spawns."));
        player.sendMessage(CC.translate(" &b/skywars lootkit <skywarsKit> <resourceKit> &7- Set chest resource Kit."));
        player.sendMessage(CC.translate(" &7Use /arena addkit <arena> <skywarsKit> to link the arena and event Kit."));
    }

    @CommandData(name = "skywars.arena.set", isAdminOnly = true,
            usage = "skywars arena set <arena>", description = "Marks a standalone arena as a SkyWars arena.")
    public void setArena(CommandArgs command) {
        if (command.length() < 1) {
            command.sendUsage();
            return;
        }

        Player player = command.getPlayer();
        StandAloneArena arena = getStandaloneArena(player, command.getArgs(0));
        if (arena == null) return;
        if (SkyWarsLoot.countChests(arena) == 0) {
            player.sendMessage(CC.translate("&cThat arena must contain at least one chest before it can be used for SkyWars."));
            return;
        }

        arena.setSkyWarsArena(true);
        this.plugin.getService(ArenaService.class).saveArena(arena);
        player.sendMessage(CC.translate("&aMarked &b" + arena.getName() + " &aas a dedicated SkyWars arena."));
        player.sendMessage(CC.translate("&7Add at least 4 spawns with &f/skywars spawn add " + arena.getName()));
    }

    @CommandData(name = "skywars.arena.unset", isAdminOnly = true,
            usage = "skywars arena unset <arena>", description = "Removes the SkyWars designation from an arena.")
    public void unsetArena(CommandArgs command) {
        if (command.length() < 1) {
            command.sendUsage();
            return;
        }

        Player player = command.getPlayer();
        StandAloneArena arena = getStandaloneArena(player, command.getArgs(0));
        if (arena == null) return;
        arena.setSkyWarsArena(false);
        this.plugin.getService(ArenaService.class).saveArena(arena);
        player.sendMessage(CC.translate("&eRemoved the SkyWars designation from &b" + arena.getName() + "&e."));
    }

    @CommandData(name = "skywars.spawn.add", isAdminOnly = true,
            usage = "skywars spawn add <arena>", description = "Adds a SkyWars spawn at your current location.")
    public void addSpawn(CommandArgs command) {
        if (command.length() < 1) {
            command.sendUsage();
            return;
        }

        Player player = command.getPlayer();
        StandAloneArena arena = getConfiguredArena(player, command.getArgs(0));
        if (arena == null) return;
        if (!isInsideArena(player.getLocation(), arena)) {
            player.sendMessage(CC.translate("&cYou must stand inside the selected SkyWars arena."));
            return;
        }

        arena.getSkyWarsSpawns().add(player.getLocation().clone());
        this.plugin.getService(ArenaService.class).saveArena(arena);
        player.sendMessage(CC.translate("&aAdded SkyWars spawn &b#" + arena.getSkyWarsSpawns().size()
                + " &ato &b" + arena.getName() + "&a."));
    }

    @CommandData(name = "skywars.spawn.remove", isAdminOnly = true,
            usage = "skywars spawn remove <arena> <number>", description = "Removes a SkyWars spawn.")
    public void removeSpawn(CommandArgs command) {
        if (command.length() < 2) {
            command.sendUsage();
            return;
        }

        Player player = command.getPlayer();
        StandAloneArena arena = getConfiguredArena(player, command.getArgs(0));
        if (arena == null) return;
        int index;
        try {
            index = Integer.parseInt(command.getArgs(1)) - 1;
        } catch (NumberFormatException exception) {
            player.sendMessage(CC.translate("&cThe spawn number must be a whole number."));
            return;
        }
        if (index < 0 || index >= arena.getSkyWarsSpawns().size()) {
            player.sendMessage(CC.translate("&cThat SkyWars spawn does not exist."));
            return;
        }

        arena.getSkyWarsSpawns().remove(index);
        this.plugin.getService(ArenaService.class).saveArena(arena);
        player.sendMessage(CC.translate("&eRemoved SkyWars spawn &b#" + (index + 1) + "&e."));
    }

    @CommandData(name = "skywars.spawn.clear", isAdminOnly = true,
            usage = "skywars spawn clear <arena>", description = "Clears all SkyWars spawns for an arena.")
    public void clearSpawns(CommandArgs command) {
        if (command.length() < 1) {
            command.sendUsage();
            return;
        }

        Player player = command.getPlayer();
        StandAloneArena arena = getConfiguredArena(player, command.getArgs(0));
        if (arena == null) return;
        arena.getSkyWarsSpawns().clear();
        this.plugin.getService(ArenaService.class).saveArena(arena);
        player.sendMessage(CC.translate("&eCleared all SkyWars spawns for &b" + arena.getName() + "&e."));
    }

    @CommandData(name = "skywars.spawn.list", isAdminOnly = true,
            usage = "skywars spawn list <arena>", description = "Lists SkyWars spawns for an arena.")
    public void listSpawns(CommandArgs command) {
        if (command.length() < 1) {
            command.sendUsage();
            return;
        }

        Player player = command.getPlayer();
        StandAloneArena arena = getConfiguredArena(player, command.getArgs(0));
        if (arena == null) return;
        List<Location> spawns = arena.getSkyWarsSpawns();
        player.sendMessage(CC.translate("&b&lSkyWars Spawns &7(" + spawns.size() + ")"));
        for (int index = 0; index < spawns.size(); index++) {
            Location spawn = spawns.get(index);
            player.sendMessage(CC.translate(" &b#" + (index + 1) + " &7- &f"
                    + spawn.getBlockX() + ", " + spawn.getBlockY() + ", " + spawn.getBlockZ()));
        }
        if (spawns.size() < 4) {
            player.sendMessage(CC.translate("&cAt least 4 spawns are required before this arena can host SkyWars."));
        }
    }

    @CommandData(name = "skywars.lootkit", isAdminOnly = true,
            usage = "skywars lootkit <skywarsKit> <resourceKit>", description = "Selects the source kit used for SkyWars chests.")
    public void setLootKit(CommandArgs command) {
        if (command.length() < 2) {
            command.sendUsage();
            return;
        }

        Player player = command.getPlayer();
        KitService kitService = this.plugin.getService(KitService.class);
        Kit skyWarsKit = kitService.getKit(command.getArgs(0));
        Kit resourceKit = kitService.getKit(command.getArgs(1));
        if (skyWarsKit == null || resourceKit == null) {
            player.sendMessage(CC.translate("&cBoth the SkyWars kit and resource kit must exist."));
            return;
        }
        if (!SkyWarsLoot.isUsableResourceKit(resourceKit)) {
            player.sendMessage(CC.translate("&cThe resource kit must contain at least 7 non-air inventory items."));
            return;
        }

        skyWarsKit.setSkyWarsResourceKit(resourceKit.getName());
        kitService.saveKit(skyWarsKit);
        player.sendMessage(CC.translate("&aSkyWars resource kit for &b" + skyWarsKit.getName()
                + " &ais now &b" + resourceKit.getName() + "&a."));
    }

    private StandAloneArena getConfiguredArena(Player player, String name) {
        StandAloneArena arena = getStandaloneArena(player, name);
        if (arena != null && !arena.isSkyWarsArena()) {
            player.sendMessage(CC.translate("&cThat arena is not marked as a SkyWars arena. Use &f/skywars arena set "
                    + arena.getName() + "&c first."));
            return null;
        }
        return arena;
    }

    private StandAloneArena getStandaloneArena(Player player, String name) {
        Arena arena = this.plugin.getService(ArenaService.class).getArenaByName(name);
        if (!(arena instanceof StandAloneArena standAloneArena)) {
            player.sendMessage(CC.translate("&cSkyWars requires a standalone arena with a schematic-backed map copy."));
            return null;
        }
        return standAloneArena;
    }

    private boolean isInsideArena(Location location, StandAloneArena arena) {
        Location minimum = arena.getMinimum();
        Location maximum = arena.getMaximum();
        if (minimum == null || maximum == null || location.getWorld() == null
                || !location.getWorld().equals(minimum.getWorld())) return false;

        return location.getX() >= Math.min(minimum.getX(), maximum.getX())
                && location.getX() <= Math.max(minimum.getX(), maximum.getX())
                && location.getY() >= Math.min(minimum.getY(), maximum.getY())
                && location.getY() <= Math.max(minimum.getY(), maximum.getY())
                && location.getZ() >= Math.min(minimum.getZ(), maximum.getZ())
                && location.getZ() <= Math.max(minimum.getZ(), maximum.getZ());
    }
}
