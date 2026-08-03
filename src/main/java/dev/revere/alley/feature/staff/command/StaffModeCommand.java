package dev.revere.alley.feature.staff.command;

import dev.revere.alley.common.text.CC;
import dev.revere.alley.feature.staff.StaffModeManager;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import org.bukkit.entity.Player;
/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 10/07/2026
 */
public class StaffModeCommand extends BaseCommand {
    @CommandData(name = "staffmode", aliases = "staff", isAdminOnly = true, usage = "staffmode", description = "Enter Staff Mode.")
    @Override
    public void onCommand(CommandArgs cmd) {
        Player p = cmd.getPlayer();
        StaffModeManager mgr = this.plugin.getService(StaffModeManager.class);
        if (mgr.isStaff(p)) { p.sendMessage(CC.translate("&cYou are already in Staff Mode.")); return; }
        mgr.enterStaff(p);
    }
}
