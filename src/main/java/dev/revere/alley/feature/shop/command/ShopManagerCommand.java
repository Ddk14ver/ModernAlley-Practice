package dev.revere.alley.feature.shop.command;

import dev.revere.alley.feature.shop.menu.ShopManagementMenu;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import org.bukkit.entity.Player;

/**
 * @author Alley
 * @project Alley
 * @since 03/07/2025
 *
 * Admin command to open the Shop Manager GUI.
 * 打开商店管理器 GUI 的管理员命令。
 */
public class ShopManagerCommand extends BaseCommand {

    @CommandData(
            name = "shopmanager",
            isAdminOnly = true,
            usage = "shopmanager",
            description = "Open the Shop Manager GUI to edit item prices and categories."
    )
    @Override
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        new ShopManagementMenu().openMenu(player);
    }
}
