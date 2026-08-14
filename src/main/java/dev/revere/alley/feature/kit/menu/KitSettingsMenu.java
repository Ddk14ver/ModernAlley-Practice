package dev.revere.alley.feature.kit.menu;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.item.ItemBuilder;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.feature.bot.menu.BotAiModeMenu;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.kit.KitService;
import dev.revere.alley.feature.kit.setting.KitSetting;
import dev.revere.alley.feature.kit.setting.types.combat.KitSettingOldHitDelay;
import dev.revere.alley.feature.kit.setting.types.mechanic.KitSettingPearlCooldownImpl;
import dev.revere.alley.feature.kit.setting.types.mode.KitSettingBotQueue;
import dev.revere.alley.feature.knockback.KnockbackManager;
import dev.revere.alley.library.menu.Button;
import dev.revere.alley.library.menu.pagination.PaginatedMenu;
import lombok.AllArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * @author Alley
 * @project Alley
 * @since 02/07/2025
 *
 * Kit settings toggle menu - toggle each kit setting on/off.
 * 工具包设置切换菜单 - 打开/关闭每个工具包设置。
 */
public class KitSettingsMenu extends PaginatedMenu {
    private final Kit kit;

    public KitSettingsMenu(Kit kit) {
        this.kit = kit;
    }

    @Override
    public String getPrePaginatedTitle(Player player) {
        long active = this.kit.getKitSettings().stream().filter(KitSetting::isEnabled).count();
        return "&6&lSettings: " + this.kit.getDisplayName() + " &7(" + active + ")";
    }

    @Override
    public int getMaxItemsPerPage() {
        return 28;
    }

    @Override
    public int getSize() {
        return 9 * 6;
    }

    @Override
    public Map<Integer, Button> getGlobalButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        this.addGlassHeader(buttons, Material.BLACK_STAINED_GLASS_PANE);

        buttons.put(48, new EnableAllButton(this.kit));
        buttons.put(49, new DisableAllButton(this.kit));
        buttons.put(53, new BackButton(this.kit));

        return buttons;
    }

    @Override
    public Map<Integer, Button> getAllPagesButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        List<KitSetting> settings = this.kit.getKitSettings();
        if (settings.isEmpty()) {
            buttons.put(22, new NoSettingsButton());
            return buttons;
        }

        int index = 0;
        for (KitSetting setting : settings) {
            buttons.put(index++, new KitSettingButton(this.kit, setting));
        }

        return buttons;
    }

    /**
     * Button for an individual kit setting.
     * 单个工具包设置的按钮。
     */
    @AllArgsConstructor
    private static class KitSettingButton extends Button {
        private final Kit kit;
        private final KitSetting setting;

        @Override
        public ItemStack getButtonItem(Player player) {
            boolean enabled = this.setting.isEnabled();
            Material material = enabled ? Material.LIME_DYE : Material.GRAY_DYE;

            List<String> lore = new ArrayList<>();
            lore.add(CC.MENU_BAR);
            lore.add("&7Description: &f" + this.setting.getDescription());
            lore.add("&7Status: " + (enabled ? "&aEnabled" : "&cDisabled"));
            if (this.setting instanceof KitSettingOldHitDelay) {
                int window = enabled ? this.setting.getValue() : KitSettingOldHitDelay.DEFAULT_DELAY;
                lore.add("&7NMS Window: &f" + window + " &8(~" + ((window + 1) / 2) + " server ticks)");
            } else if (this.setting instanceof KitSettingPearlCooldownImpl) {
                lore.add("&7Value: &f" + this.setting.getValue() + " seconds");
            } else if (this.setting instanceof KitSettingBotQueue) {
                lore.add("&7AI Mode: &f" + this.kit.getBotAiMode().name());
            } else if (this.setting.getValue() > 0) {
                lore.add("&7Value: &f" + this.setting.getValue());
            }
            lore.add("");
            if (!(this.setting instanceof KitSettingPearlCooldownImpl)) {
                lore.add("&eLeft-Click &7to toggle.");
            }
            if (this.setting instanceof KitSettingOldHitDelay
                    || this.setting instanceof KitSettingPearlCooldownImpl) {
                lore.add("&eRight-Click &7to adjust value.");
            } else if (this.setting instanceof KitSettingBotQueue) {
                lore.add("&eRight-Click &7to select the AI mode.");
            }
            lore.add(CC.MENU_BAR);

            return new ItemBuilder(material)
                    .name((enabled ? "&a" : "&c") + this.setting.getName())
                    .lore(lore)
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (this.setting instanceof KitSettingBotQueue && clickType == ClickType.RIGHT) {
                new BotAiModeMenu(this.kit).openMenu(player);
                this.playNeutral(player);
                return;
            }
            if (this.setting instanceof KitSettingPearlCooldownImpl
                    && (clickType == ClickType.LEFT || clickType == ClickType.RIGHT)) {
                player.closeInventory();
                player.sendMessage(CC.translate("&eUse &6/kit setsetting " + this.kit.getName() + " " + this.setting.getName() + " <seconds> &eto adjust. &60 &edisables it."));
                this.playNeutral(player);
                return;
            }
            if (clickType == ClickType.LEFT) {
                this.setting.setEnabled(!this.setting.isEnabled());
                if (this.setting instanceof KitSettingOldHitDelay && this.setting.isEnabled()) {
                    AlleyPlugin.getInstance().getService(KnockbackManager.class)
                            .synchronizeKitHitDelay(this.kit);
                }
                AlleyPlugin.getInstance().getService(KitService.class).saveKit(this.kit);
                player.sendMessage(CC.translate("&aSetting &6" + this.setting.getName() + " &aset to &6" + this.setting.isEnabled() + " &afor kit &6" + this.kit.getName() + "&a."));
                new KitSettingsMenu(this.kit).openMenu(player);
                this.playSuccess(player);
            } else if (clickType == ClickType.RIGHT) {
                player.closeInventory();
                String valueName = this.setting instanceof KitSettingOldHitDelay
                        ? "<NMS-window-value>" : "<value>";
                player.sendMessage(CC.translate("&eUse &6/kit setsetting " + this.kit.getName()
                        + " " + this.setting.getName() + " " + valueName + " &eto adjust."));
                this.playNeutral(player);
            }
        }
    }

    /**
     * Button to enable all settings at once.
     * 一次性启用所有设置的按钮。
     */
    @AllArgsConstructor
    private static class EnableAllButton extends Button {
        private final Kit kit;

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.LIME_DYE)
                    .name("&a&lEnable All")
                    .lore(
                            CC.MENU_BAR,
                            "&7Click to enable",
                            "&7all kit settings.",
                            CC.MENU_BAR
                    )
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType != ClickType.LEFT) return;
            for (KitSetting setting : this.kit.getKitSettings()) {
                if (setting instanceof KitSettingPearlCooldownImpl && setting.getValue() <= 0) {
                    setting.setValue(KitSettingPearlCooldownImpl.DEFAULT_SECONDS);
                }
                setting.setEnabled(true);
            }
            AlleyPlugin.getInstance().getService(KnockbackManager.class)
                    .synchronizeKitHitDelay(this.kit);
            AlleyPlugin.getInstance().getService(KitService.class).saveKit(this.kit);
            player.sendMessage(CC.translate("&aAll settings enabled for kit &6" + this.kit.getName() + "&a."));
            new KitSettingsMenu(this.kit).openMenu(player);
            this.playSuccess(player);
        }
    }

    /**
     * Button to disable all settings at once.
     * 一次性禁用所有设置的按钮。
     */
    @AllArgsConstructor
    private static class DisableAllButton extends Button {
        private final Kit kit;

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.GRAY_DYE)
                    .name("&c&lDisable All")
                    .lore(
                            CC.MENU_BAR,
                            "&7Click to disable",
                            "&7all kit settings.",
                            CC.MENU_BAR
                    )
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType != ClickType.LEFT) return;
            for (KitSetting setting : this.kit.getKitSettings()) {
                if (setting instanceof KitSettingPearlCooldownImpl) {
                    setting.setValue(0);
                }
                setting.setEnabled(false);
            }
            AlleyPlugin.getInstance().getService(KitService.class).saveKit(this.kit);
            player.sendMessage(CC.translate("&aAll settings disabled for kit &6" + this.kit.getName() + "&a."));
            new KitSettingsMenu(this.kit).openMenu(player);
            this.playSuccess(player);
        }
    }

    /**
     * Return to kit edit menu.
     * 返回工具包编辑菜单。
     */
    @AllArgsConstructor
    private static class BackButton extends Button {
        private final Kit kit;

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.ARROW)
                    .name("&e&lBack")
                    .lore(
                            CC.MENU_BAR,
                            "&7Click to return to",
                            "&7the kit editor.",
                            CC.MENU_BAR
                    )
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType != ClickType.LEFT) return;
            new KitEditMenu(this.kit).openMenu(player);
            this.playNeutral(player);
        }
    }

    /**
     * Button shown when there are no settings.
     * 当没有设置时显示的按钮。
     */
    private static class NoSettingsButton extends Button {
        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.BARRIER)
                    .name("&c&lNo Settings")
                    .lore(
                            CC.MENU_BAR,
                            "&7This kit has no settings.",
                            CC.MENU_BAR
                    )
                    .hideMeta()
                    .build();
        }
    }
}
