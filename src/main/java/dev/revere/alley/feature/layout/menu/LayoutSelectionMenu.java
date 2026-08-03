package dev.revere.alley.feature.layout.menu;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.item.ItemBuilder;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.data.types.ProfileLayoutData;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.layout.data.LayoutData;
import dev.revere.alley.library.menu.Button;
import dev.revere.alley.library.menu.Menu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.*;
/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 13/07/2026
 *
 * Level 2 — Layout selection menu for a specific kit.
 * Shows 4 layout slots, each with create/edit/rename/delete buttons.
 * Slots start unused; once a layout is saved the stone sword becomes a diamond sword.
 */
public final class LayoutSelectionMenu extends Menu {
    private final Kit kit;
    private final Profile profile;

    public LayoutSelectionMenu(Kit kit, Profile profile) {
        this.kit = kit;
        this.profile = profile;
    }

    @Override
    public String getTitle(Player player) {
        return CC.translate("&6&l" + kit.getDisplayName() + " &8— Layouts");
    }

    @Override
    public int getSize() { return 54; }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        List<LayoutData> slots = profile.getProfileData().getLayoutData()
                .getLayouts().computeIfAbsent(kit.getName(), k -> {
                    List<LayoutData> l = new ArrayList<>();
                    for (int i = 0; i < ProfileLayoutData.MAX_LAYOUTS; i++) l.add(null);
                    return l;
                });

        // Normalise to 4 slots
        while (slots.size() < ProfileLayoutData.MAX_LAYOUTS) slots.add(null);

        // Layout slot rows: 1, 2, 3, 4 (slots 11, 20, 29, 38)
        int[] rowStarts = {11, 20, 29, 38};
        for (int i = 0; i < ProfileLayoutData.MAX_LAYOUTS; i++) {
            final int idx = i;
            final LayoutData layout = (i < slots.size()) ? slots.get(i) : null;
            int base = rowStarts[idx];

            // Label
            buttons.put(base, new Button() {
                @Override
                public ItemStack getButtonItem(Player player) {
                    return new ItemBuilder(Material.PAPER)
                            .name(CC.translate("&eLayout " + (idx + 1)))
                            .lore(layout != null
                                    ? Arrays.asList(CC.translate("&7" + layout.getDisplayName()))
                                    : Collections.singletonList(CC.translate("&7Unused")))
                            .hideMeta().build();
                }
            });

            // Sword (create/edit — enters Level 3)
            buttons.put(base + 1, new Button() {
                @Override
                public ItemStack getButtonItem(Player player) {
                    boolean created = layout != null;
                    return new ItemBuilder(created ? Material.DIAMOND_SWORD : Material.STONE_SWORD)
                            .name(CC.translate(created ? "&b&lEdit Layout " + (idx+1) : "&7Create Layout " + (idx+1)))
                            .lore(created
                                    ? Arrays.asList(CC.translate("&7" + layout.getDisplayName()), "", CC.translate("&aClick to edit."))
                                    : Arrays.asList(CC.translate("&7Click to create and edit"), CC.translate("&7this layout's items.")))
                            .hideMeta().build();
                }

                @Override
                public void clicked(Player player, ClickType clickType) {
                    if (clickType != ClickType.LEFT) return;
                    if (layout == null) {
                        // Create new layout in this slot
                        LayoutData newLayout = new LayoutData("layout-" + (idx+1),
                                "Layout " + (idx+1), kit.getItems());
                        profile.getProfileData().getLayoutData().setLayoutAt(kit.getName(), idx, newLayout);
                        new LayoutEditorMenu(kit, newLayout).openMenu(player);
                    } else {
                        new LayoutEditorMenu(kit, layout).openMenu(player);
                    }
                }
            });

            // Only show book/sign/wool if layout exists
            if (layout != null) {
                // Book (edit — same as sword for created layouts)
                buttons.put(base + 2, new Button() {
                    @Override
                    public ItemStack getButtonItem(Player player) {
                        return new ItemBuilder(Material.BOOK)
                                .name(CC.translate("&a&lEdit Items"))
                                .lore(CC.translate("&7Open the layout editor."))
                                .hideMeta().build();
                    }

                    @Override
                    public void clicked(Player player, ClickType clickType) {
                        if (clickType != ClickType.LEFT) return;
                        new LayoutEditorMenu(kit, layout).openMenu(player);
                    }
                });

                // Sign (rename)
                buttons.put(base + 3, new Button() {
                    @Override
                    public ItemStack getButtonItem(Player player) {
                        return new ItemBuilder(Material.OAK_SIGN)
                                .name(CC.translate("&6&lRename"))
                                .lore(CC.translate("&7Click to rename this layout."))
                                .hideMeta().build();
                    }

                    @Override
                    public void clicked(Player player, ClickType clickType) {
                        if (clickType != ClickType.LEFT) return;
                        player.closeInventory();
                        // Use the existing rename chat listener from LayoutRenameButton
                        dev.revere.alley.feature.layout.menu.button.editor.LayoutRenameButton.triggerRename(player, layout);
                    }
                });

                // Red wool (delete)
                buttons.put(base + 4, new Button() {
                    @Override
                    public ItemStack getButtonItem(Player player) {
                        return new ItemBuilder(Material.RED_WOOL)
                                .name(CC.translate("&c&lDelete"))
                                .lore(CC.translate("&7Remove this layout."))
                                .hideMeta().build();
                    }

                    @Override
                    public void clicked(Player player, ClickType clickType) {
                        if (clickType != ClickType.LEFT) return;
                        profile.getProfileData().getLayoutData().removeLayoutAt(kit.getName(), idx);
                        player.sendMessage(CC.translate("&cLayout " + (idx+1) + " deleted."));
                        new LayoutSelectionMenu(kit, profile).openMenu(player);
                    }
                });
            }
        }

        // Back button (redstone, bottom-left)
        buttons.put(45, new Button() {
            @Override
            public ItemStack getButtonItem(Player player) {
                return new ItemBuilder(Material.REDSTONE)
                        .name(CC.translate("&c&lBack"))
                        .lore(CC.translate("&7Return to kit selection."))
                        .hideMeta().build();
            }

            @Override
            public void clicked(Player player, ClickType clickType) {
                if (clickType != ClickType.LEFT) return;
                new LayoutMenu(kit.getCategory()).openMenu(player);
            }
        });

        // Glass fill
        this.addGlass(buttons, Material.BLACK_STAINED_GLASS_PANE);

        return buttons;
    }

    @Override
    public boolean isUpdateAfterClick() { return false; }
}
