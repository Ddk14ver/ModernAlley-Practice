package dev.revere.alley.feature.kit.command.impl.data.inventory;

import dev.revere.alley.common.InventoryUtil;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.locale.internal.impl.message.GlobalMessagesLocaleImpl;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.kit.KitService;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Admin command to set optional extra items for a kit.
 * The admin's current inventory becomes the kit's editorItems.
 */
public class KitExtraItemsCommand extends BaseCommand {
    @CommandData(
            name = "kit.extraitems",
            isAdminOnly = true,
            usage = "kit extraitems <kitName>",
            description = "Set the optional extra items for a kit from your current inventory."
    )
    @Override
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        String[] args = command.getArgs();

        if (args.length < 1) {
            command.sendUsage();
            return;
        }

        KitService kitService = this.plugin.getService(KitService.class);
        Kit kit = kitService.getKit(args[0]);
        if (kit == null) {
            player.sendMessage(CC.translate(this.getString(GlobalMessagesLocaleImpl.KIT_NOT_FOUND)));
            return;
        }

        ItemStack[] contents = InventoryUtil.cloneItemStackArray(player.getInventory().getContents());
        // Only save non-null, non-air items
        int count = 0;
        for (ItemStack item : contents) {
            if (item != null && item.getType() != org.bukkit.Material.AIR) count++;
        }
        kit.setEditorItems(contents);
        kitService.saveKit(kit);

        player.sendMessage(CC.translate("&aSaved " + count + " extra items for kit &6" + kit.getDisplayName() + "&a."));
    }
}
