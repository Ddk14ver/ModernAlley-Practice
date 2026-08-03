package dev.revere.alley.feature.kit.command.impl.settings;

import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.locale.internal.impl.message.GlobalMessagesLocaleImpl;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.kit.KitService;
import dev.revere.alley.feature.kit.setting.KitSetting;
import dev.revere.alley.feature.kit.setting.KitSettingService;
import dev.revere.alley.feature.kit.setting.types.combat.KitSettingOldHitDelay;
import dev.revere.alley.feature.kit.setting.types.mechanic.KitSettingPearlCooldownImpl;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import org.bukkit.entity.Player;

/**
 * @author Remi
 * 作者: Remi
 * @project Alley
 * 项目: Alley
 * @date 5/21/2024
 * 日期: 5/21/2024
 */
public class KitSetSettingCommand extends BaseCommand {
    @CommandData(
            name = "kit.setsetting",
            aliases = {"kit.setting"},
            isAdminOnly = true,
            usage = "kit setsetting <kit> <setting> <true/false|value>",
            description = "Set a kit setting. PearlCooldown uses seconds; 0 disables it."
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
            try {
                int value = Integer.parseInt(args[2]);
                if (value < 0) {
                    player.sendMessage(CC.translate("&c" + settingName + " cannot be negative."));
                    return;
                }
                target.setValue(value);
                target.setEnabled(target instanceof KitSettingOldHitDelay || value > 0);
                this.plugin.getService(KitService.class).saveKit(kit);
                String unit = target instanceof KitSettingPearlCooldownImpl ? " seconds" : " ticks";
                player.sendMessage(CC.translate("&aSet " + settingName + " to &6" + value + unit + " &afor kit &6" + kit.getName() + "&a."));
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
