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
public class LeaveStaffCommand extends BaseCommand {
    @CommandData(name = "leavestaff", aliases = {"leavestaffmode", "staffoff"}, isAdminOnly = true, usage = "leavestaff", description = "Leave Staff Mode.")
    @Override
    public void onCommand(CommandArgs cmd) {
        Player p = cmd.getPlayer();
        StaffModeManager mgr = this.plugin.getService(StaffModeManager.class);
        mgr.leaveStaff(p);
    }
}
