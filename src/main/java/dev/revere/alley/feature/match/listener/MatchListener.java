package dev.revere.alley.feature.match.listener;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.ListenerUtil;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.locale.LocaleService;
import dev.revere.alley.core.locale.internal.impl.message.GameMessagesLocaleImpl;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.core.profile.enums.ProfileState;
import dev.revere.alley.feature.arena.ArenaType;
import dev.revere.alley.feature.arena.internal.types.StandAloneArena;
import dev.revere.alley.feature.bot.BotService;
import dev.revere.alley.feature.bot.match.BotMatchSession;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.kit.setting.types.mechanic.KitSettingDenyMovementImpl;
import dev.revere.alley.feature.kit.setting.types.mechanic.KitSettingNoHungerImpl;
import dev.revere.alley.feature.kit.setting.types.mechanic.KitSettingVoidDeathImpl;
import dev.revere.alley.feature.kit.setting.types.mode.*;
import dev.revere.alley.feature.match.Match;
import dev.revere.alley.feature.match.MatchState;
import dev.revere.alley.feature.match.internal.types.RoundsMatch;
import dev.revere.alley.feature.match.model.GameParticipant;
import dev.revere.alley.feature.match.model.internal.MatchGamePlayer;
import dev.revere.alley.feature.match.utility.MatchUtility;
import dev.revere.alley.library.menu.Menu;
import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Match listener for handling match-related events.
 * 比赛监听器，用于处理比赛相关的事件。
 * @author Remi
 * @project Alley
 * @date 5/21/2024
 */
public class MatchListener implements Listener {
    @EventHandler
    private void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (CitizensAPI.getNPCRegistry().isNPC(player)) return;
        ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);
        Profile profile = profileService.getProfile(player.getUniqueId());
        if (profile.getState() == ProfileState.SPECTATING || profile.getState() == ProfileState.PLAYING) {
            if (event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
                if (MatchUtility.isBeyondBounds(event.getTo(), profile)) {
                    event.setCancelled(true);
                    player.sendMessage(CC.translate("&cYou cannot leave the arena."));
                }
            }
        }
    }

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);
        Profile profile = profileService.getProfile(player.getUniqueId());
        Match match = profile.getMatch();
        if (match == null) return;

        Kit matchKit = match.getKit();
        if (profile.getState() == ProfileState.PLAYING && profile.getMatch().getState() == MatchState.RUNNING) {
            if (matchKit.isSettingEnabled(KitSettingSumo.class) || matchKit.isSettingEnabled(KitSettingSpleef.class)) {
                if (player.getLocation().getBlock().getType() == Material.WATER || player.getLocation().getBlock().getType() == Material.WATER) {
                    if (!handlePartyElimination(player, match, EntityDamageEvent.DamageCause.CUSTOM)) {
                        player.setHealth(0);
                    }
                }
            }

            if (match.getArena() instanceof StandAloneArena) {
                StandAloneArena arena = (StandAloneArena) match.getArena();
                if (player.getLocation().getY() <= arena.getVoidLevel() && matchKit.isSettingEnabled(KitSettingVoidDeathImpl.class)) {
                    if (player.getGameMode() == GameMode.SPECTATOR) return;
                    if (player.getGameMode() == GameMode.CREATIVE) return;
                    if (match.getArena().getType() != ArenaType.STANDALONE) return;
                    if (profile.getState() != ProfileState.PLAYING) return;

                    if (match.getKit().isSettingEnabled(KitSettingStickFight.class)) {
                        RoundsMatch roundsMatch = (RoundsMatch) match;
                        roundsMatch.handleDeath(player, EntityDamageEvent.DamageCause.VOID);
                        return;
                    }

                    profile.getMatch().handleDeath(player, EntityDamageEvent.DamageCause.VOID);
                }
            }
        }

        if (profile.getState() == ProfileState.PLAYING) {
            if (match.getState() == MatchState.STARTING || match.getState() == MatchState.ENDING_ROUND || match.getState() == MatchState.RESTARTING_ROUND) {
                boolean humanInBotMatch = AlleyPlugin.getInstance().getService(BotService.class)
                        .getSession(player) != null;
                if (!humanInBotMatch && matchKit.isSettingEnabled(KitSettingDenyMovementImpl.class)) {
                    List<GameParticipant<MatchGamePlayer>> participants = match.getParticipants();
                    match.denyPlayerMovement(participants);
                }
            }
        }

        if (profile.getState() == ProfileState.PLAYING) {
            if (profile.getMatch() == null) {
                return;
            }

            if (MatchUtility.isBeyondBounds(event.getTo(), profile)) {
                // player.teleport(event.getFrom());
                // player.sendMessage(CC.translate("&cYou cannot leave the arena."));
            }
        }
    }

    @EventHandler
    private void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);
        Profile profile = profileService.getProfile(player.getUniqueId());
        if (profile.getState() == ProfileState.PLAYING) {
            event.setRespawnLocation(player.getLocation());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onPartyPlayerLethalDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        Profile profile = AlleyPlugin.getInstance().getService(ProfileService.class).getProfile(player.getUniqueId());
        if (profile == null || profile.getState() != ProfileState.PLAYING || profile.getMatch() == null) return;
        if (event.getFinalDamage() < player.getHealth()) return;
        if (player.getInventory().getItemInMainHand().getType() == Material.TOTEM_OF_UNDYING
                || player.getInventory().getItemInOffHand().getType() == Material.TOTEM_OF_UNDYING) return;

        if (handlePartyElimination(player, profile.getMatch(), event.getCause())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);
        Profile profile = profileService.getProfile(player.getUniqueId());
        if (profile == null || profile.getState() != ProfileState.PLAYING || profile.getMatch() == null) return;

        event.setDeathMessage(null);

        profile.getMatch().handleDeathItemDrop(player, event);

        EntityDamageEvent.DamageCause cause = player.getLastDamageCause() != null ? player.getLastDamageCause().getCause() : EntityDamageEvent.DamageCause.CUSTOM;
        BotMatchSession botSession = AlleyPlugin.getInstance().getService(BotService.class).getSession(player);
        if (botSession != null) {
            botSession.handleNaturalDeath(player);
            if (player.getUniqueId().equals(botSession.getPlayerId())) {
                AlleyPlugin.getInstance().getServer().getScheduler().runTaskLater(
                        AlleyPlugin.getInstance(), player.spigot()::respawn, 1L);
                AlleyPlugin.getInstance().getServer().getScheduler().runTaskLater(
                        AlleyPlugin.getInstance(), () -> {
                            if (player.isDead()) player.spigot().respawn();
                        }, 2L);
            }
            return;
        }

        profile.getMatch().handleDeath(player, cause);

        // Apply the match spectator state before sending the respawn packet. This
        // prevents non-final FFA/split deaths from remaining on the death screen.
        AlleyPlugin.getInstance().getServer().getScheduler().runTaskLater(AlleyPlugin.getInstance(), () -> {
            player.spigot().respawn();
        }, 1L);
        AlleyPlugin.getInstance().getServer().getScheduler().runTaskLater(AlleyPlugin.getInstance(), () -> {
            if (player.isDead()) player.spigot().respawn();
        }, 2L);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (event.getClickedInventory() == null) return;

        ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);
        Profile profile = profileService.getProfile(player.getUniqueId());
        if (profile.getState() == ProfileState.SPECTATING) {
            if (!Menu.currentlyOpenedMenus.containsKey(player.getName())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    private void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);
        Profile profile = profileService.getProfile(player.getUniqueId());

        if (profile.getState() == ProfileState.SPECTATING) {
            event.setCancelled(true);
            return;
        }

        if (profile.getState() == ProfileState.PLAYING) {
            if (ListenerUtil.isSword(event.getItemDrop().getItemStack().getType())) {
                event.setCancelled(true);
                player.sendMessage(AlleyPlugin.getInstance().getService(LocaleService.class).getString(GameMessagesLocaleImpl.GAME_CANNOT_DROP_SWORD));
                return;
            }
        }
        ListenerUtil.clearDroppedItemsOnRegularItemDrop(event.getItemDrop());
    }

    @EventHandler
    private void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);
        Profile profile = profileService.getProfile(player.getUniqueId());
        if (profile.getState() == ProfileState.SPECTATING) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);
        Profile profile = profileService.getProfile(player.getUniqueId());
        if (profile.getState() != ProfileState.PLAYING) {
            return;
        }

        ItemStack item = event.getItem();
        if (item.getType() == Material.POTION) {
            AlleyPlugin.getInstance().getServer().getScheduler().runTaskLater(AlleyPlugin.getInstance(), () -> {
                player.getInventory().removeItem(new ItemStack(Material.GLASS_BOTTLE, 1));
                player.updateInventory();
            }, 1L);
        }
    }

    @EventHandler
    private void onHunger(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);
            Profile profile = profileService.getProfile(player.getUniqueId());
            if (profile.getState() != ProfileState.PLAYING) return;

            if (profile.getMatch().getKit().isSettingEnabled(KitSettingNoHungerImpl.class)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPortal(PlayerPortalEvent event) {
        Player player = event.getPlayer();
        ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);
        Profile profile = profileService.getProfile(player.getUniqueId());
        if (profile.getState() == ProfileState.PLAYING) {
            RoundsMatch match = (RoundsMatch) profile.getMatch();
            if (match.getKit().isSettingEnabled(KitSettingRounds.class) /*|| profile.getMatch().getKit().isSettingEnabled(KitSettingBridgesImpl.class)*/) {
                if (player.getGameMode() == GameMode.CREATIVE) return;
                if (player.getGameMode() == GameMode.SPECTATOR) return;
                if (player.getLocation().getBlock().getType() == Material.END_PORTAL || player.getLocation().getBlock().getType() == Material.END_PORTAL_FRAME) {
                    StandAloneArena arena = (StandAloneArena) match.getArena();
                    GameParticipant<MatchGamePlayer> playerTeam = match.getParticipantA().containsPlayer(player.getUniqueId())
                            ? match.getParticipantA()
                            : match.getParticipantB();

                    if (!arena.isEnemyPortal(match, player.getLocation(), playerTeam)) {
                        player.sendMessage(CC.translate("&cYou cannot enter your own portal!"));

                        if (match.getKit().isSettingEnabled(KitSettingRespawnTimer.class)) {
                            if (!handlePartyElimination(player, match, EntityDamageEvent.DamageCause.CUSTOM)) {
                                player.setHealth(0);
                            }
                            player.setAllowFlight(true);
                            player.setFlying(true);
                            player.setGameMode(GameMode.SPECTATOR);
                        } else {
                            Location spawnLocation = match.getParticipantA().containsPlayer(player.getUniqueId()) ? match.getArena().getPos1() : match.getArena().getPos2();
                            player.teleport(spawnLocation);
                        }
                        return;
                    }

                    if (match.getState() == MatchState.ENDING_ROUND || match.getState() == MatchState.ENDING_MATCH || match.getState() == MatchState.RESTARTING_ROUND) {
                        return;
                    }

                    GameParticipant<MatchGamePlayer> opponent = match.getParticipantA().containsPlayer(player.getUniqueId()) ? match.getParticipantB() : match.getParticipantA();
                    opponent.getPlayers().forEach(matchGamePlayer -> matchGamePlayer.setDead(true));

                    if (match.canEndRound()) {
                        match.setScorer(player.getName());
                        match.handleRoundEnd();

                        if (match.canEndMatch()) {
                            Location spawnLocation = match.getParticipantA().containsPlayer(player.getUniqueId()) ? match.getArena().getPos1() : match.getArena().getPos2();
                            player.teleport(spawnLocation);

                            match.setEndTime(System.currentTimeMillis());
                            match.setState(MatchState.ENDING_MATCH);
                            match.getRunnable().setStage(4);
                        }
                    }
                }
            }
        }
    }

    private boolean handlePartyElimination(Player player, Match match, EntityDamageEvent.DamageCause cause) {
        if (!match.isPartyMultiplayerMatch()) return false;
        if (match.getState() != MatchState.STARTING && match.getState() != MatchState.RUNNING) return false;

        MatchGamePlayer gamePlayer = match.getFromAllGamePlayers(player);
        if (gamePlayer == null || gamePlayer.isDead() || gamePlayer.isEliminated()) return true;

        player.setHealth(player.getMaxHealth());
        player.setVelocity(new org.bukkit.util.Vector());
        match.handleDeath(player, cause);
        return true;
    }
}
