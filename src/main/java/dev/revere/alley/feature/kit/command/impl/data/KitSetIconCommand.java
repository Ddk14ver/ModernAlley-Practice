package dev.revere.alley.feature.kit.command.impl.data;

import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.locale.internal.impl.message.GlobalMessagesLocaleImpl;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.kit.KitService;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

public class KitSetIconCommand extends BaseCommand {
    @CommandData(
            name = "kit.seticon",
            isAdminOnly = true,
            usage = "kit seticon <kitName>",
            description = "Set the icon of a kit to the item in your hand."
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

        ItemStack heldItem = player.getInventory().getItemInMainHand();
        if (heldItem.getType() == Material.AIR) {
            player.sendMessage(CC.translate("&cYou must hold an item to set a kit icon."));
            return;
        }

        ItemStack iconItem = heldItem.clone();
        kit.setIcon(iconItem.getType());
        kit.setIconItem(iconItem);
        kit.setDurability(iconItem.getItemMeta() instanceof Damageable damageable ? damageable.getDamage() : 0);
        kitService.saveKit(kit);

        player.sendMessage(this.getString(GlobalMessagesLocaleImpl.KIT_ICON_SET)
                .replace("{kit-name}", kit.getName())
                .replace("{icon}", iconItem.getType().name().toUpperCase())
        );
    }
}
