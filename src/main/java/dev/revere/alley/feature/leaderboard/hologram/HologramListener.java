package dev.revere.alley.feature.leaderboard.hologram;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * @author Alley
 * @project Alley
 * @since 02/07/2025
 *
 * Protects hologram TextDisplay entities from damage and chunk unloading.
 * 保护全息图TextDisplay实体免受伤害和区块卸载的影响。
 */
public class HologramListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (TextDisplayFactory.isHologramEntity(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (TextDisplayFactory.isHologramEntity(event.getEntity())) {
            event.setCancelled(true);
        }
    }

}
