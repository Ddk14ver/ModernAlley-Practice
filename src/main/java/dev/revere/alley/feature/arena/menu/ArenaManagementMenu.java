package dev.revere.alley.feature.arena.menu;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.item.ItemBuilder;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.feature.arena.Arena;
import dev.revere.alley.feature.arena.ArenaService;
import dev.revere.alley.feature.arena.ArenaType;
import dev.revere.alley.library.menu.Button;
import dev.revere.alley.library.menu.pagination.PaginatedMenu;
import lombok.AllArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 16/06/2026
 *
 * Main arena management GUI - lists all arenas with pagination.
 * 主要竞技场管理 GUI - 通过分页列出所有竞技场。
 */
public class ArenaManagementMenu extends PaginatedMenu {

    @Override
    public String getPrePaginatedTitle(Player player) {
        return "&6&lArena Manager";
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

        buttons.put(47, new ArenaWandButton());
        buttons.put(48, new CreateArenaButton());
        buttons.put(49, new InfoButton());
        buttons.put(50, new SaveAllArenasButton());

        return buttons;
    }

    @Override
    public Map<Integer, Button> getAllPagesButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        List<Arena> arenas = this.plugin.getService(ArenaService.class).getArenas();
        if (arenas.isEmpty()) {
            buttons.put(22, new EmptyInfoButton());
            return buttons;
        }

        int index = 0;
        for (Arena arena : arenas) {
            buttons.put(index++, new ArenaDisplayButton(arena));
        }

        return buttons;
    }

    /**
     * Button displaying an arena entry in the management menu.
     * 在管理菜单中显示竞技场条目的按钮。
     */
    @AllArgsConstructor
    private static class ArenaDisplayButton extends Button {
        private final Arena arena;

        @Override
        public ItemStack getButtonItem(Player player) {
            List<String> lore = new ArrayList<>();
            lore.add(CC.MENU_BAR);
            lore.add("&7ID: &f" + this.arena.getName());
            lore.add("&7Display: &f" + this.arena.getDisplayName());
            lore.add("&7Type: &f" + this.arena.getType().name());

            // Show assigned kit count
            // 显示已分配的套件数量
            int kitCount = this.arena.getKits().size();
            lore.add("&7Assigned Kits: &f" + kitCount);
            if (kitCount > 0) {
                for (String kitName : this.arena.getKits()) {
                    lore.add("  &8- &7" + kitName);
                }
            }

            lore.add("&7Enabled: " + (this.arena.isEnabled() ? "&aYes" : "&cNo"));

            // Status indicators
            // 状态指示器
            if (this.arena.getCenter() != null) {
                lore.add("&7Center: &aSet");
            } else {
                lore.add("&7Center: &cNot Set");
            }
            if (this.arena.getPos1() != null) {
                lore.add("&7Pos1: &aSet");
            } else {
                lore.add("&7Pos1: &cNot Set");
            }
            if (this.arena.getPos2() != null) {
                lore.add("&7Pos2: &aSet");
            } else {
                lore.add("&7Pos2: &cNot Set");
            }

            lore.add("");
            lore.add("&eLeft-Click &7to edit this arena.");
            lore.add("&eRight-Click &7to toggle enabled.");
            lore.add("&eMiddle-Click &7to teleport to center.");
            lore.add("&eShift-Click &7to delete.");
            lore.add(CC.MENU_BAR);

            // Choose material based on arena type
            // 根据竞技场类型选择材质
            Material material = switch (this.arena.getType()) {
                case STANDALONE -> Material.NETHERITE_BLOCK;
                case FFA -> Material.GOLD_BLOCK;
                default -> Material.GRASS_BLOCK;
            };

            return new ItemBuilder(material)
                    .name("&6" + this.arena.getDisplayName())
                    .lore(lore)
                    .glow(this.arena.isEnabled())
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType == ClickType.LEFT) {
                new ArenaEditMenu(this.arena).openMenu(player);
                this.playSuccess(player);
            } else if (clickType == ClickType.RIGHT) {
                this.arena.setEnabled(!this.arena.isEnabled());
                AlleyPlugin.getInstance().getService(ArenaService.class).saveArena(this.arena);
                player.sendMessage(CC.translate("&aArena &6" + this.arena.getName() + " &ais now " + (this.arena.isEnabled() ? "&aenabled" : "&cdisabled") + "&a."));
                new ArenaManagementMenu().openMenu(player);
                this.playNeutral(player);
            } else if (clickType == ClickType.MIDDLE) {
                if (this.arena.getCenter() != null) {
                    player.teleport(this.arena.getCenter());
                    player.sendMessage(CC.translate("&aTeleported to arena &6" + this.arena.getName() + "&a."));
                    this.playSuccess(player);
                } else {
                    player.sendMessage(CC.translate("&cThis arena has no center set! Teleporting to pos1..."));
                    if (this.arena.getPos1() != null) {
                        player.teleport(this.arena.getPos1());
                    } else {
                        player.sendMessage(CC.translate("&cNo position available to teleport to!"));
                    }
                    this.playFail(player);
                }
            } else if (clickType == ClickType.SHIFT_LEFT || clickType == ClickType.SHIFT_RIGHT) {
                String arenaName = this.arena.getName();
                AlleyPlugin.getInstance().getService(ArenaService.class).deleteArena(this.arena);
                player.sendMessage(CC.translate("&aArena &6" + arenaName + " &ahas been deleted."));
                new ArenaManagementMenu().openMenu(player);
                this.playNeutral(player);
            }
        }
    }

    /**
     * Button for creating a new arena.
     * 创建新竞技场的按钮。
     */
    private static class CreateArenaButton extends Button {
        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.EMERALD)
                    .name("&a&lCreate Arena")
                    .lore(
                            CC.MENU_BAR,
                            "&7Click for instructions on",
                            "&7how to create a new arena.",
                            "",
                            "&e/arena create <name> <type>",
                            "&7Types: SHARED, STANDALONE, FFA",
                            CC.MENU_BAR
                    )
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType != ClickType.LEFT) return;
            player.closeInventory();
            player.sendMessage(CC.translate("&eUse &6/arena create <name> <type> &eto create an arena."));
            player.sendMessage(CC.translate("&7Types: &fSHARED&7, &fSTANDALONE&7, &fFFA"));
            this.playNeutral(player);
        }
    }

    /**
     * Info/Help button.
     * 信息/帮助按钮。
     */
    private static class InfoButton extends Button {
        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.KNOWLEDGE_BOOK)
                    .name("&e&lArena Info")
                    .lore(
                            CC.MENU_BAR,
                            "&7Total Arenas: &f" + AlleyPlugin.getInstance().getService(ArenaService.class).getArenas().size(),
                            "&7Temporary Arenas: &f" + AlleyPlugin.getInstance().getService(ArenaService.class).getTemporaryArenas().size(),
                            "",
                            "&7Click to view arena commands.",
                            CC.MENU_BAR
                    )
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType != ClickType.LEFT) return;
            player.closeInventory();
            player.chat("/arena help");
            this.playNeutral(player);
        }
    }

    /**
     * Button for saving all arenas.
     * 保存所有竞技场的按钮。
     */
    private static class SaveAllArenasButton extends Button {
        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.BOOK)
                    .name("&e&lSave All Arenas")
                    .lore(
                            CC.MENU_BAR,
                            "&7Click to save all arenas",
                            "&7to the configuration file.",
                            CC.MENU_BAR
                    )
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType != ClickType.LEFT) return;
            player.closeInventory();
            player.chat("/arena saveall");
            this.playSuccess(player);
        }
    }

    /**
     * Button displayed when there are no arenas.
     * 当没有竞技场时显示的按钮。
     */
    private static class EmptyInfoButton extends Button {
        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.BARRIER)
                    .name("&c&lNo Arenas Found")
                    .lore(
                            CC.MENU_BAR,
                            "&7There are no arenas yet.",
                            "&7Use &e/arena create <name> <type>",
                            "&7to create your first arena.",
                            CC.MENU_BAR
                    )
                    .hideMeta()
                    .build();
        }
    }

    private static class ArenaWandButton extends Button {
        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.FEATHER)
                    .name("&6&lArena Wand")
                    .lore(
                            CC.MENU_BAR,
                            "&7Get the arena selection wand.",
                            "&7Use it to select regions for new arenas.",
                            "",
                            "&aClick to receive.",
                            CC.MENU_BAR
                    )
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType != ClickType.LEFT) return;
            player.closeInventory();
            player.performCommand("arena tool");
        }
    }
}
