package dev.revere.alley.feature.staff;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.bootstrap.AlleyContext;
import dev.revere.alley.bootstrap.annotation.Service;
import dev.revere.alley.common.item.ItemBuilder;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.feature.arena.menu.ArenaManagementMenu;
import dev.revere.alley.feature.kit.menu.KitManagementMenu;
import dev.revere.alley.feature.shop.menu.ShopManagementMenu;
import dev.revere.alley.feature.staff.listener.StaffModeListener;
import dev.revere.alley.feature.title.menu.TitleManagementMenu;
import dev.revere.alley.library.menu.Button;
import dev.revere.alley.library.menu.Menu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.stream.Collectors;
/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 11/07/2026
 */
@Service(provides = StaffModeManager.class, priority = 310)
public class StaffModeManager implements dev.revere.alley.bootstrap.lifecycle.Service {
    private final Set<UUID> staffPlayers = new HashSet<>();

    @Override public void initialize(AlleyContext c) {
        AlleyPlugin.getInstance().getServer().getPluginManager()
                .registerEvents(new StaffModeListener(this), AlleyPlugin.getInstance());
    }
    @Override public void shutdown(AlleyContext c) {}

    public boolean isStaff(Player p) { return staffPlayers.contains(p.getUniqueId()); }
    public boolean isStaff(UUID u) { return staffPlayers.contains(u); }

    public void enterStaff(Player p) {
        staffPlayers.add(p.getUniqueId());
        // Leave any active queue or match
        p.performCommand("leavequeue");
        p.performCommand("leavematch");
        // Force-clear all legacy combat effects
        try {
            dev.revere.alley.feature.match.internal.MatchServiceImpl ms =
                    (dev.revere.alley.feature.match.internal.MatchServiceImpl)
                    AlleyPlugin.getInstance().getService(dev.revere.alley.feature.match.MatchService.class);
            if (ms.getLegacyCombatService() != null) ms.getLegacyCombatService().removeAll(p);
        } catch (Exception ignored) {}
        p.getInventory().clear();
        applyHotbar(p);
        p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, PotionEffect.INFINITE_DURATION, 0, false, false));
        p.setAllowFlight(true); p.setFlying(true);
        p.sendMessage(CC.translate("&aYou are now in &6Staff Mode&a."));
    }

    public void leaveStaff(Player p) {
        staffPlayers.remove(p.getUniqueId());
        p.getInventory().clear();
        p.removePotionEffect(PotionEffectType.INVISIBILITY);
        p.setAllowFlight(false); p.setFlying(false);
        p.sendMessage(CC.translate("&cYou have left Staff Mode."));
        // Reapply lobby hotbar
        AlleyPlugin.getInstance().getService(dev.revere.alley.feature.hotbar.HotbarService.class).applyHotbarItems(p);
    }

    public void applyHotbar(Player p) {
        p.getInventory().clear();
        p.getInventory().setItem(0, item(Material.CARROT_ON_A_STICK, "&a&lTeleport Menu", "&7Right-click to teleport to any player."));
        p.getInventory().setItem(1, item(Material.COMPASS, "&e&lCurrent Matches", "&7Right-click to view ongoing matches."));
        p.getInventory().setItem(2, item(Material.BOOK, "&6&lSee Inventory", "&7Right-click a player to view their inventory."));
        p.getInventory().setItem(3, item(Material.STICK, "&c&lTroll Menu", "&7Right-click a player to troll them."));
        p.getInventory().setItem(4, item(Material.NETHERITE_HOE, "&d&lArena Manager", "&7Right-click to open."));
        p.getInventory().setItem(5, item(Material.DIAMOND_AXE, "&b&lKit Manager", "&7Right-click to open."));
        p.getInventory().setItem(6, item(Material.NAME_TAG, "&e&lTitle Manager", "&7Right-click to open."));
        p.getInventory().setItem(7, item(Material.EMERALD, "&a&lShop Manager", "&7Right-click to open."));
        p.getInventory().setItem(8, item(Material.REDSTONE, "&c&lLeave Staff Mode", "&7Right-click to exit."));
    }

    private ItemStack item(Material m, String name, String lore) {
        return new ItemBuilder(m).name(name).lore(CC.MENU_BAR, lore, CC.MENU_BAR).hideMeta().build();
    }

    // TP Menu
    public static class TpMenu extends Menu {
        @Override public String getTitle(Player p) { return "&6&lTeleport to Player"; }
        @Override public int getSize() { return 54; }
        @Override public Map<Integer, Button> getButtons(Player viewer) {
            Map<Integer, Button> b = new HashMap<>();
            int slot = 0;
            for (Player t : Bukkit.getOnlinePlayers()) {
                if (t.equals(viewer)) continue;
                if (slot >= 54) break;
                b.put(slot++, new Button() {
                    public ItemStack getButtonItem(Player p) { return new ItemBuilder(Material.PLAYER_HEAD).name("&6" + t.getName()).setSkull(t.getName()).lore("&7Click to teleport.").hideMeta().build(); }
                    public void clicked(Player p, ClickType c) { if (c == ClickType.LEFT && t.isOnline()) { p.teleport(t); p.sendMessage(CC.translate("&aTeleported to &6" + t.getName() + "&a.")); } }
                });
            }
            return b;
        }
    }
}
