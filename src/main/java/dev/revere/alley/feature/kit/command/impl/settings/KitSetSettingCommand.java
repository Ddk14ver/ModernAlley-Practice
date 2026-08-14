package dev.revere.alley.feature.kit.command.impl.settings;

import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.locale.internal.impl.message.GlobalMessagesLocaleImpl;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.kit.KitService;
import dev.revere.alley.feature.kit.setting.KitSetting;
import dev.revere.alley.feature.kit.setting.KitSettingService;
import dev.revere.alley.feature.kit.setting.types.combat.KitSettingOldHitDelay;
import dev.revere.alley.feature.kit.setting.types.mechanic.KitSettingPearlCooldownImpl;
import dev.revere.alley.feature.knockback.KnockbackManager;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import org.bukkit.entity.Player;

/**
 * @author Remi
 * @project Alley
 * @date 5/21/2024
 */
public class KitSetSettingCommand extends BaseCommand {
    @CommandData(
            name = "kit.setsetting",
            aliases = {"kit.setting"},
            isAdminOnly = true,
            usage = "kit setsetting <kit> <setting> <true/false|value>",
            description = "Set a kit setting. oldHitDelay follows the kit's knockback profile."
    )
    @Override
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        String[] args = command.getArgs();

        if (args.length != 3) {
            command.sendUsage();
            return;
        }

        Kit kit = this.plugin.getService(KitService.class).getKit(args[0]);
        if (kit == null) {
            player.sendMessage(CC.translate(this.getString(GlobalMessagesLocaleImpl.KIT_NOT_FOUND)));
            return;
        }

        String settingName = args[1];

        if (this.plugin.getService(KitSettingService.class).getSettings().stream().noneMatch(setting -> setting.getName().equalsIgnoreCase(settingName))) {
            player.sendMessage(CC.translate("&cA setting with that name does not exist."));
            return;
        }

        KitSetting target = kit.getKitSettings().stream().filter(setting -> setting.getName().equalsIgnoreCase(settingName)).findFirst().orElse(null);
        if (target == null) {
            player.sendMessage(CC.translate("&cThis kit does not have that setting."));
            return;
        }

        if (target instanceof KitSettingOldHitDelay || target instanceof KitSettingPearlCooldownImpl) {
            if (target instanceof KitSettingOldHitDelay
                    && (args[2].equalsIgnoreCase("true") || args[2].equalsIgnoreCase("false"))) {
                boolean enabled = Boolean.parseBoolean(args[2]);
                target.setEnabled(enabled);
                if (enabled) {
                    this.plugin.getService(KnockbackManager.class).synchronizeKitHitDelay(kit);
                }
                this.plugin.getService(KitService.class).saveKit(kit);
                player.sendMessage(CC.translate(enabled
                        ? "&aEnabled oldHitDelay for &6" + kit.getName()
                                + "&a and synchronized it to the profile value &6" + target.getValue() + "&a."
                        : "&aDisabled oldHitDelay for &6" + kit.getName()
                                + "&a; this kit will use the fixed window &6"
                                + KitSettingOldHitDelay.DEFAULT_DELAY + " &a(about 10 server ticks)."));
                return;
            }
            try {
                int value = Integer.parseInt(args[2]);
                if (value < 0) {
                    player.sendMessage(CC.translate("&c" + settingName + " cannot be negative."));
                    return;
                }
                target.setValue(value);
                target.setEnabled(target instanceof KitSettingOldHitDelay || value > 0);
                boolean synchronizedToProfile = target instanceof KitSettingOldHitDelay
                        && this.plugin.getService(KnockbackManager.class).synchronizeKitHitDelay(kit);
                this.plugin.getService(KitService.class).saveKit(kit);
                String unit = target instanceof KitSettingPearlCooldownImpl
                        ? " seconds" : " NMS-window units";
                if (synchronizedToProfile) {
                    player.sendMessage(CC.translate("&cThe requested oldHitDelay &6" + value
                            + " &cdid not match the profile. The kit was synchronized to &6"
                            + target.getValue() + "&c."));
                } else {
                    player.sendMessage(CC.translate("&aSet " + settingName + " to &6" + value + unit + " &afor kit &6" + kit.getName() + "&a."));
                }
            } catch (NumberFormatException e) {
                player.sendMessage(CC.translate("&cInvalid number: " + args[2] + "."));
            }
        } else {
            boolean enabled = Boolean.parseBoolean(args[2]);
            target.setEnabled(enabled);
            this.plugin.getService(KitService.class).saveKit(kit);
            player.sendMessage(CC.translate(this.getString(GlobalMessagesLocaleImpl.KIT_SETTING_SET))
                    .replace("{setting-name}", settingName)
                    .replace("{enabled}", String.valueOf(enabled))
                    .replace("{kit-name}", kit.getName())
            );
        }
    }
}
