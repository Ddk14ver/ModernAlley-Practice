package dev.revere.alley.feature.spawn.listener;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.enums.ProfileState;
import dev.revere.alley.feature.tournament.model.Tournament;
import dev.revere.alley.library.menu.Menu;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

/**
 * @author Emmy
 * @project Alley
 * @date 25/05/2024 - 16:20
 */
public class SpawnListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        Profile profile = AlleyPlugin.getInstance().getService(ProfileService.class)
                .getProfile(player.getUniqueId());
        if (player.getGameMode() != GameMode.CREATIVE && isProtectedLobbyState(profile)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);
        Profile profile = profileService.getProfile(player.getUniqueId());

        if (isProtectedBlockState(profile)) {
            if (player.getGameMode() == GameMode.CREATIVE) {
                return;
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);
        Profile profile = profileService.getProfile(player.getUniqueId());

        if (player.getGameMode() == GameMode.SURVIVAL
                && (profile.getState().equals(ProfileState.LOBBY)
                || validateTournament(profile)
                || profile.getState().equals(ProfileState.EDITING)
                || profile.getState().equals(ProfileState.WAITING))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemPickUp(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);
        Profile profile = profileService.getProfile(player.getUniqueId());

        if (player.getGameMode() == GameMode.SURVIVAL
                && (profile.getState().equals(ProfileState.LOBBY)
                || validateTournament(profile)
                || profile.getState().equals(ProfileState.EDITING)
                || profile.getState().equals(ProfileState.WAITING))) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);
        Profile profile = profileService.getProfile(player.getUniqueId());
        if (isProtectedBlockState(profile)) {
            if (player.getGameMode() == GameMode.CREATIVE) {
                return;
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onHunger(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);
            Profile profile = profileService.getProfile(player.getUniqueId());

            if (profile.getState().equals(ProfileState.LOBBY)
                    || profile.getState().equals(ProfileState.EDITING)
                    || profile.getState().equals(ProfileState.WAITING)
                    || validateTournament(profile)
                    || profile.getState().equals(ProfileState.SPECTATING)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onMoveItem(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);
            Profile profile = profileService.getProfile(player.getUniqueId());

            // Menu clicks (incl. shift-clicks) are handled by the MenuListener (HIGHEST, ignoreCancelled).
            // Cancelling them here would swallow every shift-click button (delete confirms, etc.).
            // 菜单点击（包括shift点击）由MenuListener处理（HIGHEST优先级，ignoreCancelled）。
            // 如果在这里取消，所有shift点击按钮（删除确认等）都会失效。
            if (Menu.currentlyOpenedMenus.containsKey(player.getName())
                    && event.getClickedInventory() != null
                    && event.getClickedInventory() != player.getInventory()) {
                return;
            }

            if (profile.getState() == ProfileState.EDITING) {
                return;
            }

            if (player.getGameMode() == GameMode.SURVIVAL && (profile.getState().equals(ProfileState.LOBBY) || validateTournament(profile) || profile.getState().equals(ProfileState.WAITING))) {
                if (event.getClickedInventory() != null && event.getClickedInventory().equals(player.getInventory())) {
                    event.setCancelled(true);
                }

                if (event.getSlotType() == InventoryType.SlotType.CRAFTING || event.isShiftClick() || event.getClick().isKeyboardClick()) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerItemDamageEvent(PlayerItemDamageEvent event) {
        Profile profile = AlleyPlugin.getInstance().getService(ProfileService.class).getProfile(event.getPlayer().getUniqueId());

        if (profile.getState().equals(ProfileState.LOBBY)
                || profile.getState().equals(ProfileState.EDITING)
                || validateTournament(profile)
                || profile.getState().equals(ProfileState.WAITING)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onHangingPlace(HangingPlaceEvent event) {
        Player player = event.getPlayer();
        ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);
        Profile profile = profileService.getProfile(player.getUniqueId());

        if (profile.getState().equals(ProfileState.LOBBY) || validateTournament(profile) || profile.getState().equals(ProfileState.EDITING) || profile.getState().equals(ProfileState.WAITING)) {
            if (player.getGameMode() == GameMode.CREATIVE) {
                return;
            }
            event.setCancelled(true);
        }
    }

    private boolean validateTournament(Profile profile) {
        Tournament tournament = profile.getTournament();

        return tournament != null && profile.getState().equals(ProfileState.TOURNAMENT_LOBBY);
    }

    private boolean isProtectedLobbyState(Profile profile) {
        return profile != null && (profile.getState() == ProfileState.LOBBY
                || profile.getState() == ProfileState.WAITING
                || profile.getState() == ProfileState.EDITING
                || profile.getState() == ProfileState.SPECTATING
                || validateTournament(profile)
                // Event participants stay in PLAYING_EVENT while queued in the
                // lobby. Once their event match begins, Match assigns a context
                // and normal kit-specific block rules take over.
                || (profile.getState() == ProfileState.PLAYING_EVENT
                && profile.getMatch() == null));
    }

    private boolean isProtectedBlockState(Profile profile) {
        return profile != null && (profile.getState() == ProfileState.LOBBY
                || profile.getState() == ProfileState.WAITING
                || profile.getState() == ProfileState.EDITING
                || validateTournament(profile)
                || (profile.getState() == ProfileState.PLAYING_EVENT
                && profile.getMatch() == null));
    }
}
