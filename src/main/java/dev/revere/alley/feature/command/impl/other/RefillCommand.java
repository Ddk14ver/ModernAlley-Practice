package dev.revere.alley.feature.command.impl.other;

import dev.revere.alley.common.PotionUtil;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import dev.revere.alley.common.text.CC;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

/**
 * @author Emmy
 * @project Alley
 * @date 28/10/2024 - 08:47
 */
public class RefillCommand extends BaseCommand {
    @CommandData(
            name = "refill",
            isAdminOnly = true,
            usage = "refill",
            description = "Refill your inventory with health potions."
            // 用药水重新填充你的背包。
    )
    @Override
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();

        Arrays.stream(player.getInventory().getContents()).forEach(item -> {
            if (item == null) {
                player.getInventory().addItem(PotionUtil.createSplashHealthPotion());
            }
        });

        player.sendMessage(CC.translate("&aYou've refilled &6your inventory &awith &6health &apotions."));
    }
}