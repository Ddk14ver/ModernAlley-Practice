package dev.revere.alley.feature.layout.menu;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.core.profile.enums.ProfileState;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.layout.data.LayoutData;
import dev.revere.alley.feature.layout.menu.button.editor.LayoutCancelButton;
import dev.revere.alley.feature.layout.menu.button.editor.LayoutDeleteButton;
import dev.revere.alley.feature.layout.menu.button.editor.LayoutEditorExtraItemButton;
import dev.revere.alley.feature.layout.menu.button.editor.LayoutRenameButton;
import dev.revere.alley.feature.layout.menu.button.editor.LayoutResetItemsButton;
import dev.revere.alley.feature.layout.menu.button.editor.LayoutSaveButton;
import dev.revere.alley.library.menu.Button;
import dev.revere.alley.library.menu.Menu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Menu for editing a specific kit layout.
 *
 * @author Emmy
 * @project Alley
 * @since 03/05/2025
 */
public class LayoutEditorMenu extends Menu {
    private static final int EXTRA_ITEMS_START_SLOT = 37;
    private static final int EXTRA_ITEMS_PER_PAGE = 8;
    private static final int EXTRA_ITEMS_BORDER_START_SLOT = 45;

    protected final AlleyPlugin plugin = AlleyPlugin.getInstance();
    private final Kit kit;
    private final LayoutData layout;
    private int extraItemsPage;

    public LayoutEditorMenu(Kit kit, LayoutData layout) {
        this.kit = kit;
        this.layout = layout;
    }

    @Override
    public void onOpen(Player player) {
        AlleyPlugin.getInstance().getService(ProfileService.class).getProfile(player.getUniqueId()).setState(ProfileState.EDITING);
        player.getInventory().setContents(this.layout.getItems());
    }

    @Override
    public void onClose(Player player) {
        AlleyPlugin.getInstance().getService(ProfileService.class).getProfile(player.getUniqueId()).setState(ProfileState.LOBBY);
        super.onClose(player);
    }

    @Override
    public String getTitle(Player player) {
        return "&6&lEditing " + this.layout.getDisplayName();
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        buttons.put(11, new LayoutSaveButton(this.kit, this.layout));
        buttons.put(13, new LayoutResetItemsButton(this.kit));
        buttons.put(15, new LayoutCancelButton());
        buttons.put(21, new LayoutDeleteButton(this.layout));
        buttons.put(23, new LayoutRenameButton(this.layout));

        this.addExtraItems(buttons);
        this.addExtraItemsBorder(buttons);
        this.addGlass(buttons, Material.BLACK_STAINED_GLASS_PANE);

        return buttons;
    }

    @Override
    public int getSize() {
        int extraItemCount = this.getExtraItems().size();
        if (extraItemCount == 0) {
            return 9 * 4;
        }

        return 9 * 6;
    }

    @Override
    public boolean isUpdateAfterClick() {
        return false;
    }

    private void addExtraItems(Map<Integer, Button> buttons) {
        List<ItemStack> extraItems = this.getExtraItems();
        if (extraItems.isEmpty()) {
            return;
        }

        int firstItemIndex = this.extraItemsPage * EXTRA_ITEMS_PER_PAGE;
        if (firstItemIndex >= extraItems.size()) {
            this.extraItemsPage = 0;
            firstItemIndex = 0;
        }

        int displayedItems = Math.min(EXTRA_ITEMS_PER_PAGE, extraItems.size() - firstItemIndex);
        for (int index = 0; index < displayedItems; index++) {
            buttons.put(EXTRA_ITEMS_START_SLOT + index,
                    new LayoutEditorExtraItemButton(extraItems.get(firstItemIndex + index)));
        }

        if (extraItems.size() > EXTRA_ITEMS_PER_PAGE) {
            if (this.extraItemsPage > 0) {
                buttons.put(48, new ExtraItemsPageButton(this, -1));
            }
            if (firstItemIndex + displayedItems < extraItems.size()) {
                buttons.put(50, new ExtraItemsPageButton(this, 1));
            }
        }
    }

    private void addExtraItemsBorder(Map<Integer, Button> buttons) {
        if (this.getExtraItems().isEmpty()) {
            return;
        }

        for (int slot = EXTRA_ITEMS_BORDER_START_SLOT; slot < EXTRA_ITEMS_BORDER_START_SLOT + 9; slot++) {
            buttons.putIfAbsent(slot, Button.placeholder(Material.BLACK_STAINED_GLASS_PANE, ""));
        }
    }

    private List<ItemStack> getExtraItems() {
        ItemStack[] editorItems = this.kit.getEditorItems();
        if (editorItems == null || editorItems.length == 0) {
            return Collections.emptyList();
        }

        List<ItemStack> extraItems = new ArrayList<>();
        for (ItemStack item : editorItems) {
            if (item != null && item.getType() != Material.AIR) {
                extraItems.add(item);
            }
        }
        return extraItems;
    }

    private void changeExtraItemsPage(Player player, int change) {
        int pageCount = (int) Math.ceil(this.getExtraItems().size() / (double) EXTRA_ITEMS_PER_PAGE);
        int nextPage = this.extraItemsPage + change;
        if (nextPage < 0 || nextPage >= pageCount) {
            return;
        }

        this.extraItemsPage = nextPage;
        Map<Integer, Button> buttons = this.getButtons(player);
        this.setButtons(buttons);

        Inventory inventory = player.getOpenInventory().getTopInventory();
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            Button button = buttons.get(slot);
            inventory.setItem(slot, button == null ? null : this.createItemStack(player, button));
        }
    }

    private static class ExtraItemsPageButton extends Button {
        private final LayoutEditorMenu menu;
        private final int change;

        private ExtraItemsPageButton(LayoutEditorMenu menu, int change) {
            this.menu = menu;
            this.change = change;
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            boolean nextPage = this.change > 0;
            return new dev.revere.alley.common.item.ItemBuilder(nextPage ? Material.ARROW : Material.SPECTRAL_ARROW)
                    .name(nextPage ? "&6&lNext Page" : "&6&lPrevious Page")
                    .lore(nextPage ? "&7View more extra items." : "&7View previous extra items.")
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, org.bukkit.event.inventory.ClickType clickType) {
            if (clickType == org.bukkit.event.inventory.ClickType.LEFT) {
                this.menu.changeExtraItemsPage(player, this.change);
            }
        }
    }
}
