package dev.revere.alley.feature.staff.command;

import dev.revere.alley.feature.arena.selection.ArenaSelection;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import dev.revere.alley.common.item.ItemBuilder;
/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 11/07/2026
 */
public class ToolsCommand extends BaseCommand {
    @CommandData(name = "tools", isAdminOnly = true, usage = "tools", description = "Get admin tools: arena wand, arena manager, kit manager.")
    @Override
    public void onCommand(CommandArgs cmd) {
        Player p = cmd.getPlayer();
        p.getInventory().addItem(ArenaSelection.SELECTION_TOOL);
        p.getInventory().addItem(new ItemBuilder(Material.NETHERITE_HOE).name("&d&lArena Manager").hideMeta().build());
        p.getInventory().addItem(new ItemBuilder(Material.DIAMOND_AXE).name("&b&lKit Manager").hideMeta().build());
    }
}
