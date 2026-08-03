package dev.revere.alley.feature.combat.listener;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.feature.combat.CombatAttribution;
import dev.revere.alley.feature.combat.CombatService;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * @author Emmy
 * @project Alley
 * @since 14/03/2025
 */
public class CombatListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    private void onEntityPlace(EntityPlaceEvent event) {
        if (event.getEntity() instanceof EnderCrystal) {
            CombatAttribution.setEndCrystalOwner((EnderCrystal) event.getEntity(), event.getPlayer());
        }
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        this.removeFromCombatMap(player);
    }

    @EventHandler
    private void onKick(PlayerKickEvent event) {
        Player player = event.getPlayer();
        this.removeFromCombatMap(player);
    }

    /**
     * Removes the player from the combat map.
     *
     * @param player The player to remove.
     */
    private void removeFromCombatMap(Player player) {
        CombatService combatService = AlleyPlugin.getInstance().getService(CombatService.class);
        if (combatService.getCombatMap().containsKey(player.getUniqueId())) {
            combatService.removeLastAttacker(player, true);
        }
    }
}
