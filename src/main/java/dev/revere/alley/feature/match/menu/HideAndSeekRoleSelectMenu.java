package dev.revere.alley.feature.match.menu;

import dev.revere.alley.common.item.ItemBuilder;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.feature.match.internal.types.HideAndSeekMatch;
import dev.revere.alley.library.menu.Button;
import dev.revere.alley.library.menu.Menu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class HideAndSeekRoleSelectMenu extends Menu {
    private final HideAndSeekMatch match;

    public HideAndSeekRoleSelectMenu(HideAndSeekMatch match) {
        this.match = match;
        this.setUpdateAfterClick(false);
    }

    @Override
    public String getTitle(Player player) {
        return "&6&lChoose Your Role";
    }

    @Override
    public int getSize() {
        return 27;
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        addBorder(buttons, Material.BLACK_STAINED_GLASS_PANE, 3);
        buttons.put(11, new RoleButton(true));
        buttons.put(15, new RoleButton(false));
        return buttons;
    }

    @Override
    public void onClose(Player player) {
        super.onClose(player);
        if (this.match.isSelectingRoles() && !this.match.hasChosenRole(player.getUniqueId())) {
            this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
                if (player.isOnline() && this.match.isSelectingRoles()
                        && !this.match.hasChosenRole(player.getUniqueId())) {
                    new HideAndSeekRoleSelectMenu(this.match).openMenu(player);
                }
            }, 1L);
        }
    }

    private final class RoleButton extends Button {
        private final boolean seeker;

        private RoleButton(boolean seeker) {
            this.seeker = seeker;
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            int remaining = this.seeker
                    ? match.getRemainingSeekerSlots()
                    : match.getRemainingHiderSlots();
            if (this.seeker) {
                return new ItemBuilder(Material.IRON_SWORD)
                        .name("&c&lSeeker")
                        .lore(
                                CC.MENU_BAR,
                                "&7Hunt the hiders after they hide.",
                                "&7Slots left: &c" + remaining,
                                "",
                                remaining > 0 ? "&eClick to become a seeker." : "&cNo seeker slots left.",
                                CC.MENU_BAR
                        )
                        .hideMeta()
                        .build();
            }
            return new ItemBuilder(Material.LEATHER_CHESTPLATE)
                    .name("&a&lHider")
                    .lore(
                            CC.MENU_BAR,
                            "&7Hide before the seekers are released.",
                            "&7Slots left: &a" + remaining,
                            "",
                            remaining > 0 ? "&eClick to become a hider." : "&cNo hider slots left.",
                            CC.MENU_BAR
                    )
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType != ClickType.LEFT) return;
            if (match.selectRole(player, this.seeker)) {
                this.playSuccess(player);
                player.closeInventory();
                return;
            }
            this.playFail(player);
        }
    }
}
