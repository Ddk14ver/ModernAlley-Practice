package dev.revere.alley.feature.staff.listener;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.core.profile.enums.ProfileState;
import dev.revere.alley.feature.staff.StaffModeManager;
import dev.revere.alley.feature.staff.menu.TrollMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 10/07/2026
 */
public class StaffModeListener implements Listener {
    private final StaffModeManager manager;

    public StaffModeListener(StaffModeManager manager) {
        this.manager = manager;
    }

    // Prevent match joining
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQueue(PlayerJoinEvent e) {
        if (manager.isStaff(e.getPlayer())) {
            Profile p = AlleyPlugin.getInstance().getService(ProfileService.class).getProfile(e.getPlayer().getUniqueId());
            if (p != null && p.getState() != ProfileState.LOBBY) {
                p.setState(ProfileState.LOBBY);
                p.setMatch(null);
            }
        }
    }

    // Prevent taking damage
    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player p && manager.isStaff(p)) e.setCancelled(true);
    }

    // Prevent dealing damage
    @EventHandler
    public void onAttack(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player p && manager.isStaff(p)) e.setCancelled(true);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (manager.isStaff(e.getPlayer())) e.setCancelled(true);
    }

    @EventHandler
    public void onPickup(PlayerAttemptPickupItemEvent e) {
        if (manager.isStaff(e.getPlayer())) e.setCancelled(true);
    }

    // Restore invisibility + flight on respawn/world change
    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        if (manager.isStaff(e.getPlayer())) {
            e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, PotionEffect.INFINITE_DURATION, 0, false, false));
            e.getPlayer().setAllowFlight(true); e.getPlayer().setFlying(true);
        }
    }

    // Hotbar click handling — also handles admin tools outside staff mode
    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = p.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;
        String name = item.getItemMeta().getDisplayName();

        // Admin tools work regardless of staff mode
        if (name.contains("Arena Manager")) { p.performCommand("arenamanager"); return; }
        if (name.contains("Kit Manager")) { p.performCommand("kitmanager"); return; }
        if (name.contains("Title Manager")) { p.performCommand("titlemanager"); return; }
        if (name.contains("Shop Manager")) { p.performCommand("shopmanager"); return; }

        if (!manager.isStaff(p)) return;

        if (name.contains("Teleport Menu")) new StaffModeManager.TpMenu().openMenu(p);
        else if (name.contains("Current Matches")) p.performCommand("currentmatches");
        else if (name.contains("See Inventory")) p.sendMessage(CC.translate("&eRight-click a player with this book to see their inventory."));
        else if (name.contains("Troll Menu")) p.sendMessage(CC.translate("&eRight-click a player with this stick to open the troll menu."));
        else if (name.contains("Leave Staff Mode")) manager.leaveStaff(p);
    }

    // Right-click on player with SeeInv book
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
        Player p = e.getPlayer();
        if (!manager.isStaff(p)) return;
        if (!(e.getRightClicked() instanceof Player target)) return;
        ItemStack item = p.getInventory().getItemInMainHand();
        if (item.getType() == Material.BOOK) p.performCommand("invsee " + target.getName());
        if (item.getType() == Material.STICK) new TrollMenu(target).openMenu(p);
    }
}
