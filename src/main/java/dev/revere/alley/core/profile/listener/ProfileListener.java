package dev.revere.alley.core.profile.listener;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.adapter.core.CoreAdapter;
import dev.revere.alley.common.PlayerUtil;

import dev.revere.alley.common.reflect.ReflectionService;

import dev.revere.alley.common.reflect.internal.types.TitleReflectionServiceImpl;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.locale.LocaleService;
import dev.revere.alley.core.locale.internal.impl.VisualsLocaleImpl;
import dev.revere.alley.core.locale.internal.impl.message.GlobalMessagesLocaleImpl;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.core.profile.enums.ProfileState;
import dev.revere.alley.feature.hotbar.HotbarService;
import dev.revere.alley.feature.music.MusicService;
import dev.revere.alley.feature.spawn.SpawnService;
import dev.revere.alley.feature.tournament.model.Tournament;
import dev.revere.alley.feature.tournament.model.TournamentState;
import dev.revere.alley.feature.visibility.VisibilityService;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.InventoryHolder;

import java.util.List;

/**
 * @author Emmy
 * @project Alley
 * @since 19/04/2024
 * 玩家档案监听器，处理玩家登录、加入、退出、受伤和交互等事件。
 * Profile listener, handling player login, join, quit, damage, and interaction events.
 */
public class ProfileListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onLogin(PlayerLoginEvent event) {
        if (!AlleyPlugin.getInstance().isEnabled()) {
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, CC.translate("&cThe server is still loading, please try again in a few seconds."));
            return;
        }

        Player player = event.getPlayer();

        if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) {
            return;
        }

        Profile profile = new Profile(player.getUniqueId(), player.getName());
        profile.load();

        ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);
        profileService.getProfile(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onJoin(PlayerJoinEvent event) {
        if (!AlleyPlugin.getInstance().isEnabled()) {
            event.getPlayer().kickPlayer(CC.translate("&cThe server is still loading, please try again in a few seconds."));
            return;
        }

        event.setJoinMessage(null);

        Player player = event.getPlayer();
        ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);
        Profile profile = profileService.getProfile(player.getUniqueId());

        this.handlePlayerJoin(profile, player);
        this.sendJoinMessage(player);
        this.sendJoinMessageTitle(player);
    }

    @EventHandler
    private void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);
        Profile profile = profileService.getProfile(player.getUniqueId());

        if (profile.getState() == ProfileState.LOBBY
                || validateTournament(profile)
                || profile.getState() == ProfileState.SPECTATING
                || profile.getState() == ProfileState.EDITING
                || profile.getState() == ProfileState.WAITING) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onPlayerQuitEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);
        MusicService musicService = AlleyPlugin.getInstance().getService(MusicService.class);

        Profile profile = profileService.getProfile(player.getUniqueId());

        event.setQuitMessage(null);

        musicService.stopMusic(player);

        profile.updatePlayTime();
        profile.setOnline(false);
        profile.save();

        // profileService.removeProfile(player.getUniqueId()); todo: figure out why i did this again..? dont remember its been too long
        // profileService.removeProfile(player.getUniqueId()); todo: 弄清楚为什么我又这样做了..? 不记得了，太久了
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);
        Profile profile = profileService.getProfile(player.getUniqueId());

        if (isProtectedLobbyState(profile)) {
            if (player.getGameMode() == GameMode.CREATIVE) return;
            event.setCancelled(true);

            Block block = event.getClickedBlock();
            if (block != null && block.getState() instanceof InventoryHolder) {
                if (block.getType() == Material.CHEST || block.getType() == Material.DISPENSER || block.getType() == Material.FURNACE || block.getType() == Material.BREWING_STAND) {
                    event.setCancelled(true);
                }
            }
        }
    }

    /**
     * Handles the player joining the server.
     * This method sets the player's profile state to LOBBY, updates their name
     * and online status, including other profile-related data.
     * Also teleports the player to the spawn and applies the lobby hotbar items.
     * 处理玩家加入服务器。
     * 此方法将玩家的档案状态设置为 LOBBY，更新其名称和在线状态，
     * 以及其他与档案相关的数据。
     * 同时将玩家传送到出生点并应用大厅快捷栏物品。
     *
     * @param profile The profile of the player.
     *                玩家的档案。
     * @param player  The player who joined.
     *                加入的玩家。
     */
    private void handlePlayerJoin(Profile profile, Player player) {
        CoreAdapter coreAdapter = AlleyPlugin.getInstance().getService(CoreAdapter.class);
        SpawnService spawnService = AlleyPlugin.getInstance().getService(SpawnService.class);
        HotbarService hotbarService = AlleyPlugin.getInstance().getService(HotbarService.class);
        VisibilityService visibilityService = AlleyPlugin.getInstance().getService(VisibilityService.class);
        MusicService musicService = AlleyPlugin.getInstance().getService(MusicService.class);

        profile.setState(ProfileState.LOBBY);
        profile.setName(player.getName());
        profile.setOnline(true);
        profile.setMatch(null);
        profile.setParty(null);
        profile.setFfaMatch(null);
        profile.setTournament(null);

        // Purge any lingering legacy combat effects from crashed/incomplete matches
        try {
            dev.revere.alley.feature.match.internal.MatchServiceImpl ms =
                    (dev.revere.alley.feature.match.internal.MatchServiceImpl)
                    AlleyPlugin.getInstance().getService(dev.revere.alley.feature.match.MatchService.class);
            if (ms.getLegacyCombatService() != null) ms.getLegacyCombatService().removeAll(player);
        } catch (Exception ignored) {}
        // Force-reset attack speed to vanilla default (may persist in NBT after crash)
        try {
            org.bukkit.attribute.Attribute atk = org.bukkit.Registry.ATTRIBUTE.get(
                    org.bukkit.NamespacedKey.minecraft("attack_speed"));
            if (atk != null) {
                org.bukkit.attribute.AttributeInstance ai = player.getAttribute(atk);
                if (ai != null && ai.getBaseValue() > 10) ai.setBaseValue(4.0);
            }
        } catch (Exception ignored) {}

        profile.setNameColor(coreAdapter.getCore().getPlayerColor(player));
        profile.getProfileData().getSettingData().setTimeBasedOnProfileSetting(player);
        profile.getProfileData().getPlayTimeData().setLastLogin(System.currentTimeMillis());
        profile.getProfileData().ensureKitData();
        profile.getProfileData().determineLevel();

        player.setFlySpeed(1 * 0.1F);
        player.setWalkSpeed(2 * 0.1F);
        player.getInventory().setHeldItemSlot(0);

        PlayerUtil.reset(player, false, true);

        spawnService.teleportToSpawn(player);
        hotbarService.applyHotbarItems(player);
        visibilityService.updateVisibility(player);
        musicService.startMusic(player);

        // Sync purchased cosmetics from profile to session permissions
        java.util.Set<String> purchased = profile.getProfileData().getCosmeticData().getPurchasedCosmetics();
        if (!purchased.isEmpty()) {
            org.bukkit.permissions.PermissionAttachment att = player.addAttachment(AlleyPlugin.getInstance());
            dev.revere.alley.feature.cosmetic.CosmeticService cs = AlleyPlugin.getInstance().getService(dev.revere.alley.feature.cosmetic.CosmeticService.class);
            for (dev.revere.alley.feature.cosmetic.model.CosmeticType type : dev.revere.alley.feature.cosmetic.model.CosmeticType.values()) {
                var repo = cs.getRepository(type);
                if (repo != null) {
                    repo.getCosmetics().stream()
                            .filter(c -> purchased.contains(c.getName().toLowerCase()))
                            .forEach(c -> att.setPermission(c.getPermission(), true));
                }
            }
        }

        player.updateInventory();
    }

    private void sendJoinMessageTitle(Player player) {
        TitleReflectionServiceImpl titleService = AlleyPlugin.getInstance().getService(ReflectionService.class).getReflectionService(TitleReflectionServiceImpl.class);

        boolean enabled = AlleyPlugin.getInstance().getService(LocaleService.class).getBoolean(VisualsLocaleImpl.TITLE_JOIN_MESSAGE_ENABLED);
        if (!enabled) return;

        String header = AlleyPlugin.getInstance().getService(LocaleService.class).getString(VisualsLocaleImpl.TITLE_JOIN_MESSAGE_HEADER);
        String subHeader = AlleyPlugin.getInstance().getService(LocaleService.class).getString(VisualsLocaleImpl.TITLE_JOIN_MESSAGE_SUBHEADER);
        int fadeIn = AlleyPlugin.getInstance().getService(LocaleService.class).getInt(VisualsLocaleImpl.TITLE_JOIN_MESSAGE_FADE_IN);
        int stay = AlleyPlugin.getInstance().getService(LocaleService.class).getInt(VisualsLocaleImpl.TITLE_JOIN_MESSAGE_STAY);
        int fadeOut = AlleyPlugin.getInstance().getService(LocaleService.class).getInt(VisualsLocaleImpl.TITLE_JOIN_MESSAGE_FADE_OUT);

        titleService.sendTitle(player, header, subHeader, fadeIn, stay, fadeOut);
    }

    /**
     * Sends a welcome message to the player when they join the server.
     * The message is configured in the global-messages.yml file.
     * 当玩家加入服务器时向其发送欢迎消息。
     * 该消息在 global-messages.yml 文件中配置。
     *
     * @param player The player who joined.
     *               加入的玩家。
     */
    private void sendJoinMessage(Player player) {
        boolean enabled = AlleyPlugin.getInstance().getService(LocaleService.class).getBoolean(GlobalMessagesLocaleImpl.JOIN_MESSAGE_CHAT_ENABLED);
        if (!enabled) return;

        List<String> message = AlleyPlugin.getInstance().getService(LocaleService.class).getStringList(GlobalMessagesLocaleImpl.JOIN_MESSAGE_CHAT_MESSAGE_LIST);
        message.replaceAll(line -> line
                .replace("{player}", player.getName())
                .replace("{version}", AlleyPlugin.getInstance().getDescription().getVersion())
                .replace("{author}", AlleyPlugin.getInstance().getDescription().getAuthors().toString().replace("[", "").replace("]", ""))
        );

        message.forEach(line -> player.sendMessage(CC.translate(line)));
    }

    /**
     * Validates whether the profile is in a tournament that is starting or waiting.
     * 验证玩家档案是否处于正在开始或等待中的锦标赛中。
     *
     * @param profile The profile to validate.
     *                要验证的玩家档案。
     * @return True if the profile is in a valid tournament state, otherwise false.
     *         如果玩家档案处于有效的锦标赛状态则返回 true，否则返回 false。
     */
    private boolean validateTournament(Profile profile) {
        Tournament tournament = profile.getTournament();

        return tournament != null &&
                profile.getState().equals(ProfileState.TOURNAMENT_LOBBY) &&
                (tournament.getState() == TournamentState.STARTING || tournament.getState() == TournamentState.WAITING);
    }

    private boolean isProtectedLobbyState(Profile profile) {
        return profile != null && (profile.getState() == ProfileState.LOBBY
                || profile.getState() == ProfileState.EDITING
                || profile.getState() == ProfileState.SPECTATING
                || profile.getState() == ProfileState.WAITING
                || validateTournament(profile)
                || (profile.getState() == ProfileState.PLAYING_EVENT
                && profile.getMatch() == null));
    }
}
