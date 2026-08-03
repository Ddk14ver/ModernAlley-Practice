package dev.revere.alley.feature.kit.menu;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.item.ItemBuilder;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.kit.KitService;
import dev.revere.alley.library.menu.Button;
import dev.revere.alley.library.menu.pagination.PaginatedMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class HideAndSeekKitSelectionMenu extends PaginatedMenu {
    private final Kit matchKit;
    private final boolean seeker;

    public HideAndSeekKitSelectionMenu(Kit matchKit, boolean seeker) {
        this.matchKit = matchKit;
        this.seeker = seeker;
    }

    @Override
    public String getPrePaginatedTitle(Player player) {
        return "&6&lSelect " + (seeker ? "Seeker" : "Hider") + " Kit";
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
        addGlassHeader(buttons, Material.BLACK_STAINED_GLASS_PANE);
        buttons.put(48, new ClearButton());
        buttons.put(53, new BackButton());
        return buttons;
    }

    @Override
    public Map<Integer, Button> getAllPagesButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        int index = 0;
        for (Kit candidate : AlleyPlugin.getInstance().getService(KitService.class).getKits()) {
            if (!candidate.getName().equalsIgnoreCase(matchKit.getName())) {
                buttons.put(index++, new KitButton(candidate));
            }
        }
        return buttons;
    }

    private String getSelectedKitName() {
        return seeker ? matchKit.getHideAndSeekSeekerKit() : matchKit.getHideAndSeekHiderKit();
    }

    private void setSelectedKitName(String kitName) {
        if (seeker) {
            matchKit.setHideAndSeekSeekerKit(kitName);
        } else {
            matchKit.setHideAndSeekHiderKit(kitName);
        }
        AlleyPlugin.getInstance().getService(KitService.class).saveKit(matchKit);
    }

    private class KitButton extends Button {
        private final Kit candidate;

        private KitButton(Kit candidate) {
            this.candidate = candidate;
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            boolean selected = candidate.getName().equalsIgnoreCase(getSelectedKitName());
            return new ItemBuilder(candidate.getIconItemOrDefault())
                    .name((selected ? "&a" : "&6") + candidate.getDisplayName())
                    .lore(
                            CC.MENU_BAR,
                            "&7Kit ID: &f" + candidate.getName(),
                            "&7Selected: " + (selected ? "&aYes" : "&cNo"),
                            "",
                            "&eClick &7to assign this kit.",
                            CC.MENU_BAR
                    )
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType != ClickType.LEFT) return;
            setSelectedKitName(candidate.getName());
            player.sendMessage(CC.translate("&a" + (seeker ? "Seeker" : "Hider") + " kit set to &6" + candidate.getName() + "&a."));
            new KitEditMenu(matchKit).openMenu(player);
            this.playSuccess(player);
        }
    }

    private class ClearButton extends Button {
        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.BARRIER)
                    .name("&c&lUse Main Kit")
                    .lore(
                            CC.MENU_BAR,
                            "&7Clear the role-specific kit.",
                            CC.MENU_BAR
                    )
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType != ClickType.LEFT) return;
            setSelectedKitName("");
            new KitEditMenu(matchKit).openMenu(player);
            this.playSuccess(player);
        }
    }

    private class BackButton extends Button {
        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.ARROW)
                    .name("&e&lBack")
                    .lore(CC.MENU_BAR, "&7Return to the kit editor.", CC.MENU_BAR)
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType != ClickType.LEFT) return;
            new KitEditMenu(matchKit).openMenu(player);
            this.playNeutral(player);
        }
    }
}
