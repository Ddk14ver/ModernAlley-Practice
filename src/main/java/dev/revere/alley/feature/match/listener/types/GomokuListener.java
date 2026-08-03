package dev.revere.alley.feature.match.listener.types;

import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.core.profile.enums.ProfileState;
import dev.revere.alley.feature.match.MatchState;
import dev.revere.alley.feature.match.internal.types.GomokuItems;
import dev.revere.alley.feature.match.internal.types.GomokuPlayable;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.EquipmentSlot;

public class GomokuListener implements Listener {
    private final ProfileService profileService;

    public GomokuListener(ProfileService profileService) {
        this.profileService = profileService;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlacementPearl(PlayerInteractEvent event) {
        GomokuPlayable match = getMatch(event.getPlayer());
        if (match == null || event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getItem() == null || event.getItem().getType() != Material.ENDER_PEARL) return;

        event.setCancelled(true);
        event.setUseItemInHand(Event.Result.DENY);
        event.setUseInteractedBlock(Event.Result.DENY);
        match.tryPlaceFromView(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSurrenderPotion(PlayerItemConsumeEvent event) {
        GomokuPlayable match = getMatch(event.getPlayer());
        if (match == null || !GomokuItems.isSurrenderPotion(event.getItem())) return;

        event.setCancelled(true);
        match.surrender(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof org.bukkit.entity.Player player && getMatch(player) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() instanceof org.bukkit.entity.Player player
                && getMatch(player) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBreak(BlockBreakEvent event) {
        if (getMatch(event.getPlayer()) != null) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlace(BlockPlaceEvent event) {
        if (getMatch(event.getPlayer()) != null) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrop(PlayerDropItemEvent event) {
        if (getMatch(event.getPlayer()) != null) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSwap(PlayerSwapHandItemsEvent event) {
        if (getMatch(event.getPlayer()) != null) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onToggleFlight(PlayerToggleFlightEvent event) {
        if (!event.isFlying() && getMatch(event.getPlayer()) != null) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFoodChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof org.bukkit.entity.Player player && getMatch(player) != null) {
            event.setCancelled(true);
            player.setFoodLevel(20);
        }
    }

    private GomokuPlayable getMatch(org.bukkit.entity.Player player) {
        Profile profile = this.profileService.getProfile(player.getUniqueId());
        if (profile == null || profile.getState() != ProfileState.PLAYING
                || !(profile.getMatch() instanceof GomokuPlayable gomoku)) {
            return null;
        }
        MatchState state = profile.getMatch().getState();
        return state == MatchState.STARTING || state == MatchState.RUNNING ? gomoku : null;
    }
}
