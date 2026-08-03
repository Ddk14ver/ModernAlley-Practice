package dev.revere.alley.feature.arena.menu;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.item.ItemBuilder;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.feature.arena.Arena;
import dev.revere.alley.feature.arena.ArenaService;
import dev.revere.alley.library.menu.Button;
import dev.revere.alley.library.menu.Menu;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 16/06/2026
 * Detailed arena editing menu - edit all properties of a single arena.
 * 详细的竞技场编辑菜单 - 编辑单个竞技场的所有属性。
 */
public class ArenaEditMenu extends Menu {
    private final Arena arena;

    public ArenaEditMenu(Arena arena) {
        this.arena = arena;
        setPlaceholder(true);
    }

    @Override
    public String getTitle(Player player) {
        return "&6&lEditing: " + this.arena.getDisplayName();
    }

    @Override
    public int getSize() {
        return 9 * 6;
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        // Row 1: Arena Name / ID (center)
        // 第1行：竞技场名称 / ID（居中）
        buttons.put(4, new ArenaNameButton(this.arena));

        // Row 2-3: Positions
        // 第2-3行：位置
        buttons.put(19, new Pos1Button(this.arena));
        buttons.put(22, new CenterButton(this.arena));
        buttons.put(25, new Pos2Button(this.arena));

        // Row 3: Teleport buttons
        // 第3行：传送按钮
        buttons.put(28, new TeleportToPos1Button(this.arena));
        buttons.put(31, new TeleportToCenterButton(this.arena));
        buttons.put(34, new TeleportToPos2Button(this.arena));

        // Row 4: Display Name, Type, Enabled
        // 第4行：显示名称、类型、启用
        buttons.put(37, new DisplayNameButton(this.arena));
        buttons.put(40, new ToggleEnabledButton(this.arena));
        buttons.put(43, new ArenaTypeButton(this.arena));

        // Row 5: Kit Management
        // 第5行：套件管理
        buttons.put(47, new AssignedKitsButton(this.arena));
        buttons.put(49, new SaveArenaButton(this.arena));
        buttons.put(51, new SetSpawnButton(this.arena));

        // Row 6: Navigation
        // 第6行：导航
        buttons.put(45, new SetCuboidButton(this.arena));
        buttons.put(48, new RefreshButton(this.arena));
        buttons.put(50, new DeleteArenaButton(this.arena));
        buttons.put(53, new BackButton());

        this.addBorder(buttons, Material.BLACK_STAINED_GLASS_PANE, 6);

        return buttons;
    }

    // ========================
    // Button Inner Classes
    // 按钮内部类
    // ========================

    /**
     * Displays the arena's internal name/ID.
     * 显示竞技场的内部名称/ID。
     */
    private static class ArenaNameButton extends Button {
        private final Arena arena;

        public ArenaNameButton(Arena arena) {
            this.arena = arena;
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.NAME_TAG)
                    .name("&6&lArena: " + this.arena.getDisplayName())
                    .lore(
                            CC.MENU_BAR,
                            "&7ID: &f" + this.arena.getName(),
                            "&7Display Name: &f" + this.arena.getDisplayName(),
                            "&7Type: &f" + this.arena.getType().name(),
                            "&7Enabled: " + (this.arena.isEnabled() ? "&aYes" : "&cNo"),
                            "&7Kits: &f" + this.arena.getKits().size(),
                            "",
                            "&cID cannot be changed.",
                            CC.MENU_BAR
                    )
                    .hideMeta()
                    .build();
        }
    }

    /**
     * Shows Pos1 (spawn 1) information.
     * 显示 Pos1（出生点1）信息。
     */
    private static class Pos1Button extends Button {
        private final Arena arena;

        public Pos1Button(Arena arena) {
            this.arena = arena;
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            Location pos = this.arena.getPos1();
            String posStr = pos != null
                    ? pos.getBlockX() + ", " + pos.getBlockY() + ", " + pos.getBlockZ()
                    : "&cNot Set";

            return new ItemBuilder(Material.RED_WOOL)
                    .name("&c&lPos1 (Spawn 1)")
                    .lore(
                            CC.MENU_BAR,
                            "&7Position: &f" + posStr,
                            pos != null ? "&7World: &f" + pos.getWorld().getName() : "",
                            "",
                            "&eClick &7to set to your location.",
                            "&7Or use &e/arena setspawn " + arena.getName() + " pos1",
                            CC.MENU_BAR
                    )
                    .glow(pos != null)
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType != ClickType.LEFT) return;
            this.arena.setPos1(player.getLocation());
            AlleyPlugin.getInstance().getService(ArenaService.class).saveArena(this.arena);
            player.sendMessage(CC.translate("&aPos1 set for arena &6" + this.arena.getName() + "&a!"));
            new ArenaEditMenu(this.arena).openMenu(player);
            this.playSuccess(player);
        }
    }

    /**
     * Shows Center information.
     * 显示中心点信息。
     */
    private static class CenterButton extends Button {
        private final Arena arena;

        public CenterButton(Arena arena) {
            this.arena = arena;
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            Location pos = this.arena.getCenter();
            String posStr = pos != null
                    ? pos.getBlockX() + ", " + pos.getBlockY() + ", " + pos.getBlockZ()
                    : "&cNot Set";

            return new ItemBuilder(Material.BEACON)
                    .name("&e&lCenter")
                    .lore(
                            CC.MENU_BAR,
                            "&7Position: &f" + posStr,
                            pos != null ? "&7World: &f" + pos.getWorld().getName() : "",
                            "",
                            "&eClick &7to set to your location.",
                            "&7Or use &e/arena setcenter " + arena.getName(),
                            CC.MENU_BAR
                    )
                    .glow(pos != null)
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType != ClickType.LEFT) return;
            this.arena.setCenter(player.getLocation());
            AlleyPlugin.getInstance().getService(ArenaService.class).saveArena(this.arena);
            player.sendMessage(CC.translate("&aCenter set for arena &6" + this.arena.getName() + "&a!"));
            new ArenaEditMenu(this.arena).openMenu(player);
            this.playSuccess(player);
        }
    }

    /**
     * Shows Pos2 (spawn 2) information.
     * 显示 Pos2（出生点2）信息。
     */
    private static class Pos2Button extends Button {
        private final Arena arena;

        public Pos2Button(Arena arena) {
            this.arena = arena;
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            Location pos = this.arena.getPos2();
            String posStr = pos != null
                    ? pos.getBlockX() + ", " + pos.getBlockY() + ", " + pos.getBlockZ()
                    : "&cNot Set";

            return new ItemBuilder(Material.BLUE_WOOL)
                    .name("&9&lPos2 (Spawn 2)")
                    .lore(
                            CC.MENU_BAR,
                            "&7Position: &f" + posStr,
                            pos != null ? "&7World: &f" + pos.getWorld().getName() : "",
                            "",
                            "&eClick &7to set to your location.",
                            "&7Or use &e/arena setspawn " + arena.getName() + " pos2",
                            CC.MENU_BAR
                    )
                    .glow(pos != null)
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType != ClickType.LEFT) return;
            this.arena.setPos2(player.getLocation());
            AlleyPlugin.getInstance().getService(ArenaService.class).saveArena(this.arena);
            player.sendMessage(CC.translate("&aPos2 set for arena &6" + this.arena.getName() + "&a!"));
            new ArenaEditMenu(this.arena).openMenu(player);
            this.playSuccess(player);
        }
    }

    /**
     * Teleport to Pos1.
     * 传送到 Pos1。
     */
    private static class TeleportToPos1Button extends Button {
        private final Arena arena;

        public TeleportToPos1Button(Arena arena) {
            this.arena = arena;
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.ENDER_PEARL)
                    .name("&cTeleport to Pos1")
                    .lore(
                            CC.MENU_BAR,
                            "&7Click to teleport",
                            "&7to spawn position 1.",
                            CC.MENU_BAR
                    )
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType != ClickType.LEFT) return;
            if (this.arena.getPos1() != null) {
                player.teleport(this.arena.getPos1());
                player.sendMessage(CC.translate("&aTeleported to Pos1 of &6" + this.arena.getName() + "&a."));
                this.playSuccess(player);
            } else {
                player.sendMessage(CC.translate("&cPos1 is not set!"));
                this.playFail(player);
            }
        }
    }

    /**
     * Teleport to Center.
     * 传送到中心点。
     */
    private static class TeleportToCenterButton extends Button {
        private final Arena arena;

        public TeleportToCenterButton(Arena arena) {
            this.arena = arena;
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.ENDER_PEARL)
                    .name("&eTeleport to Center")
                    .lore(
                            CC.MENU_BAR,
                            "&7Click to teleport",
                            "&7to the arena center.",
                            CC.MENU_BAR
                    )
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType != ClickType.LEFT) return;
            if (this.arena.getCenter() != null) {
                player.teleport(this.arena.getCenter());
                player.sendMessage(CC.translate("&aTeleported to center of &6" + this.arena.getName() + "&a."));
                this.playSuccess(player);
            } else {
                player.sendMessage(CC.translate("&cCenter is not set!"));
                this.playFail(player);
            }
        }
    }

    /**
     * Teleport to Pos2.
     * 传送到 Pos2。
     */
    private static class TeleportToPos2Button extends Button {
        private final Arena arena;

        public TeleportToPos2Button(Arena arena) {
            this.arena = arena;
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.ENDER_PEARL)
                    .name("&9Teleport to Pos2")
                    .lore(
                            CC.MENU_BAR,
                            "&7Click to teleport",
                            "&7to spawn position 2.",
                            CC.MENU_BAR
                    )
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType != ClickType.LEFT) return;
            if (this.arena.getPos2() != null) {
                player.teleport(this.arena.getPos2());
                player.sendMessage(CC.translate("&aTeleported to Pos2 of &6" + this.arena.getName() + "&a."));
                this.playSuccess(player);
            } else {
                player.sendMessage(CC.translate("&cPos2 is not set!"));
                this.playFail(player);
            }
        }
    }

    /**
     * Edit display name button.
     * 编辑显示名称按钮。
     */
    private static class DisplayNameButton extends Button {
        private final Arena arena;

        public DisplayNameButton(Arena arena) {
            this.arena = arena;
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.OAK_SIGN)
                    .name("&6&lDisplay Name")
                    .lore(
                            CC.MENU_BAR,
                            "&7Current: &f" + this.arena.getDisplayName(),
                            "",
                            "&eClick &7to change via chat.",
                            "&7Use &e/arena setdisplayname " + this.arena.getName() + " <name>",
                            CC.MENU_BAR
                    )
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType != ClickType.LEFT) return;
            player.closeInventory();
            player.sendMessage(CC.translate("&eUse &6/arena setdisplayname " + this.arena.getName() + " <name> &eto change."));
            this.playNeutral(player);
        }
    }

    /**
     * Toggle arena enabled/disabled.
     * 切换竞技场启用/禁用。
     */
    private static class ToggleEnabledButton extends Button {
        private final Arena arena;

        public ToggleEnabledButton(Arena arena) {
            this.arena = arena;
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            boolean enabled = this.arena.isEnabled();
            return new ItemBuilder(enabled ? Material.LIME_DYE : Material.GRAY_DYE)
                    .name((enabled ? "&a" : "&c") + "&lEnabled: " + (enabled ? "Yes" : "No"))
                    .lore(
                            CC.MENU_BAR,
                            "&7The arena is currently " + (enabled ? "&aenabled" : "&cdisabled") + "&7.",
                            "",
                            "&eClick &7to " + (enabled ? "disable" : "enable") + ".",
                            CC.MENU_BAR
                    )
                    .glow(enabled)
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType != ClickType.LEFT) return;
            this.arena.setEnabled(!this.arena.isEnabled());
            AlleyPlugin.getInstance().getService(ArenaService.class).saveArena(this.arena);
            new ArenaEditMenu(this.arena).openMenu(player);
            this.playSuccess(player);
        }
    }

    /**
     * Show arena type (informational - ID-based, cannot change).
     * 显示竞技场类型（信息性 - 基于ID，无法更改）。
     */
    private static class ArenaTypeButton extends Button {
        private final Arena arena;

        public ArenaTypeButton(Arena arena) {
            this.arena = arena;
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            Material icon = switch (this.arena.getType()) {
                case STANDALONE -> Material.NETHERITE_BLOCK;
                case FFA -> Material.GOLD_BLOCK;
                default -> Material.GRASS_BLOCK;
            };

            return new ItemBuilder(icon)
                    .name("&6&lArena Type: &e" + this.arena.getType().name())
                    .lore(
                            CC.MENU_BAR,
                            "&7Type: &f" + this.arena.getType().name(),
                            "",
                            "&7" + getTypeDescription(this.arena.getType()),
                            "",
                            "&cType cannot be changed.",
                            "&7Delete and recreate to change.",
                            CC.MENU_BAR
                    )
                    .hideMeta()
                    .build();
        }

        private String getTypeDescription(dev.revere.alley.feature.arena.ArenaType type) {
            return switch (type) {
                case SHARED -> "Shared arenas run on the main world.";
                case STANDALONE -> "Standalone arenas use copied schematic worlds.";
                case FFA -> "FFA arenas support free-for-all with safe zones.";
            };
        }
    }

    /**
     * Opens the kit assignment sub-menu.
     * 打开套件分配子菜单。
     */
    private static class AssignedKitsButton extends Button {
        private final Arena arena;

        public AssignedKitsButton(Arena arena) {
            this.arena = arena;
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            List<String> lore = new ArrayList<>();
            lore.add(CC.MENU_BAR);
            lore.add("&7Assigned Kits: &f" + this.arena.getKits().size());
            if (!this.arena.getKits().isEmpty()) {
                for (String kitName : this.arena.getKits()) {
                    lore.add("  &8- &7" + kitName);
                }
            } else {
                lore.add("  &7(None assigned)");
            }
            lore.add("");
            lore.add("&eClick &7to manage assigned kits.");
            lore.add(CC.MENU_BAR);

            return new ItemBuilder(Material.CHEST)
                    .name("&6&lAssigned Kits")
                    .lore(lore)
                    .glow(!this.arena.getKits().isEmpty())
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType != ClickType.LEFT) return;
            new ArenaKitMenu(this.arena).openMenu(player);
            this.playNeutral(player);
        }
    }

    /**
     * Save arena button.
     * 保存竞技场按钮。
     */
    private static class SaveArenaButton extends Button {
        private final Arena arena;

        public SaveArenaButton(Arena arena) {
            this.arena = arena;
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.WRITABLE_BOOK)
                    .name("&a&lSave Arena")
                    .lore(
                            CC.MENU_BAR,
                            "&7Click to save this arena",
                            "&7to the configuration file.",
                            CC.MENU_BAR
                    )
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType != ClickType.LEFT) return;
            AlleyPlugin.getInstance().getService(ArenaService.class).saveArena(this.arena);
            player.sendMessage(CC.translate("&aArena &6" + this.arena.getName() + " &asaved!"));
            this.playSuccess(player);
        }
    }

    /**
     * Set spawn button - quick set for pos1/pos2.
     * 设置出生点按钮 - 快速设置 pos1/pos2。
     */
    private static class SetSpawnButton extends Button {
        private final Arena arena;

        public SetSpawnButton(Arena arena) {
            this.arena = arena;
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.NETHER_STAR)
                    .name("&6&lSet Spawns")
                    .lore(
                            CC.MENU_BAR,
                            "&cLeft-Click &7to set &cPos1 &7here.",
                            "&9Right-Click &7to set &9Pos2 &7here.",
                            "&eShift-Click &7to set &eCenter &7here.",
                            "",
                            "&7Stand at the location first!",
                            CC.MENU_BAR
                    )
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            Location loc = player.getLocation();
            ArenaService arenaService = AlleyPlugin.getInstance().getService(ArenaService.class);

            if (clickType == ClickType.LEFT) {
                this.arena.setPos1(loc);
                player.sendMessage(CC.translate("&cPos1 &aset for arena &6" + this.arena.getName() + "&a!"));
            } else if (clickType == ClickType.RIGHT) {
                this.arena.setPos2(loc);
                player.sendMessage(CC.translate("&9Pos2 &aset for arena &6" + this.arena.getName() + "&a!"));
            } else if (clickType == ClickType.SHIFT_LEFT || clickType == ClickType.SHIFT_RIGHT) {
                this.arena.setCenter(loc);
                player.sendMessage(CC.translate("&eCenter &aset for arena &6" + this.arena.getName() + "&a!"));
            }

            arenaService.saveArena(this.arena);
            new ArenaEditMenu(this.arena).openMenu(player);
            this.playSuccess(player);
        }
    }

    /**
     * Set cuboid button.
     * 设置立方体区域按钮。
     */
    private static class SetCuboidButton extends Button {
        private final Arena arena;

        public SetCuboidButton(Arena arena) {
            this.arena = arena;
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            boolean hasMin = this.arena.getMinimum() != null;
            boolean hasMax = this.arena.getMaximum() != null;

            return new ItemBuilder(Material.WOODEN_AXE)
                    .name("&6&lSet Cuboid (Min/Max)")
                    .lore(
                            CC.MENU_BAR,
                            "&7Min: " + (hasMin ? "&aSet" : "&cNot Set"),
                            "&7Max: " + (hasMax ? "&aSet" : "&cNot Set"),
                            "",
                            "&eClick &7for instructions.",
                            "&7Use &e/arena setcuboid " + arena.getName(),
                            "&7or the arena selection tool.",
                            CC.MENU_BAR
                    )
                    .glow(hasMin && hasMax)
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType != ClickType.LEFT) return;
            player.closeInventory();
            player.sendMessage(CC.translate("&eUse &6/arena setcuboid " + this.arena.getName() + " &eto set the cuboid area."));
            player.sendMessage(CC.translate("&7Or use &6/arena tool &7to get the selection wand."));
            this.playNeutral(player);
        }
    }

    /**
     * Refresh the menu button.
     * 刷新菜单按钮。
     */
    private static class RefreshButton extends Button {
        private final Arena arena;

        public RefreshButton(Arena arena) {
            this.arena = arena;
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.CLOCK)
                    .name("&e&lRefresh")
                    .lore(
                            CC.MENU_BAR,
                            "&7Click to refresh the menu.",
                            CC.MENU_BAR
                    )
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType != ClickType.LEFT) return;
            new ArenaEditMenu(this.arena).openMenu(player);
            this.playNeutral(player);
        }
    }

    /**
     * Delete the arena button.
     * 删除竞技场按钮。
     */
    private static class DeleteArenaButton extends Button {
        private final Arena arena;

        public DeleteArenaButton(Arena arena) {
            this.arena = arena;
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.LAVA_BUCKET)
                    .name("&c&lDelete Arena")
                    .lore(
                            CC.MENU_BAR,
                            "&cWarning: This cannot be undone!",
                            "",
                            "&eShift-Click &7to confirm delete.",
                            CC.MENU_BAR
                    )
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType != ClickType.SHIFT_LEFT && clickType != ClickType.SHIFT_RIGHT) {
                player.sendMessage(CC.translate("&cShift-click to confirm deletion!"));
                this.playFail(player);
                return;
            }
            String arenaName = this.arena.getName();
            AlleyPlugin.getInstance().getService(ArenaService.class).deleteArena(this.arena);
            player.sendMessage(CC.translate("&aArena &6" + arenaName + " &ahas been deleted."));
            new ArenaManagementMenu().openMenu(player);
            this.playSuccess(player);
        }
    }

    /**
     * Return to arena management menu.
     * 返回竞技场管理菜单。
     */
    private static class BackButton extends Button {
        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.ARROW)
                    .name("&e&lBack")
                    .lore(
                            CC.MENU_BAR,
                            "&7Click to return to",
                            "&7the arena management menu.",
                            CC.MENU_BAR
                    )
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType != ClickType.LEFT) return;
            new ArenaManagementMenu().openMenu(player);
            this.playNeutral(player);
        }
    }
}
