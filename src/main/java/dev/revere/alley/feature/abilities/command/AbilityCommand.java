package dev.revere.alley.feature.abilities.command;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import dev.revere.alley.feature.abilities.AbilityService;
import dev.revere.alley.common.text.CC;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
/**
 * @author remi
 * @project Alley
 * @since 26/06/2025
 */
@Getter
@Setter
public class AbilityCommand extends BaseCommand {

    private AlleyPlugin plugin = AlleyPlugin.getInstance();

    @CommandData(
            name = "ability",
            permission = "hypractice.command.ability",
            usage = "ability",
            description = "Manage abilities."
    )
    @Override
    public void onCommand(CommandArgs commandArgs) {
        Player player = commandArgs.getPlayer();
        String[] args = commandArgs.getArgs();
        if (args.length < 1) {
            this.getUsage(player, "ability");
            return;
        }

        switch (args[0].toLowerCase()) {
            case "give":
                if (args.length < 4) {
                    commandArgs.sendUsage();
                    return;
                }

                Player target = Bukkit.getPlayer(args[1]);

                if (target == null) {
                    CC.sender(player, "&cPlayer '" + args[1] + "' not found.");
                    return;
                }

                Integer amount;
                try {
                    amount = Integer.parseInt(args[3]);
                } catch (NumberFormatException e) {
                    amount = null;
                }

                if (amount == null) {
                    CC.sender(player, "&cAmount must be a number.");
                    return;
                }
                if (amount <= 0) {
                    CC.sender(player, "&cAmount must be positive.");
                    return;
                }

                AbilityService abilityService = plugin.getService(AbilityService.class);
                int finalAmount = amount;
                if (args[2].equalsIgnoreCase("all")) {
                    abilityService.getAbilityKeys().forEach(ability ->
                            abilityService.giveAbility(player, target, ability, abilityService.getDisplayName(ability), finalAmount));
                    return;
                }

                String ability = abilityService.getAbilityKeys().stream()
                        .filter(key -> key.equalsIgnoreCase(args[2]))
                        .findFirst()
                        .orElse(null);
                if (ability == null) {
                    CC.sender(player, "&cAbility '" + args[2] + "' not found.");
                    return;
                }

                abilityService.giveAbility(player, target, ability, abilityService.getDisplayName(ability), finalAmount);
                break;
            case "list":
                CC.sender(player, "&7&m-----------------------------");
                CC.sender(player, "&c&lAbilities List &7(" + this.plugin.getService(AbilityService.class).getAbilityKeys().size() + ")");
                CC.sender(player, "");
                plugin.getService(AbilityService.class).getAbilityKeys().forEach(
                        abilityKey -> CC.sender(player, " &7- &4" + abilityKey));
                CC.sender(player, "&7&m-----------------------------");
                break;
        }
        return;
    }

    private void getUsage(CommandSender sender, String label) {
        CC.sender(sender, "&7&m-----------------------------");
        CC.sender(sender, "&c&lAbility Help");
        CC.sender(sender, "");
        CC.sender(sender, "&4/" + label + " give <player> <ability|all> <amount>");
        CC.sender(sender, "&4/" + label + " list");
        CC.sender(sender, "&7&m-----------------------------");
    }
}
