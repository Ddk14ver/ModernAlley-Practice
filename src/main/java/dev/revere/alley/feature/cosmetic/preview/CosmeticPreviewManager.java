package dev.revere.alley.feature.cosmetic.preview;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.core.profile.enums.ProfileState;
import dev.revere.alley.feature.cosmetic.internal.repository.impl.killeffect.BaseKillEffect;
import dev.revere.alley.feature.cosmetic.internal.repository.impl.killmessage.KillMessagePack;
import dev.revere.alley.feature.cosmetic.internal.repository.impl.mvpmusic.BaseMVPMusic;
import dev.revere.alley.feature.cosmetic.internal.repository.impl.soundeffect.BaseSoundEffect;
import dev.revere.alley.feature.cosmetic.model.BaseCosmetic;
import dev.revere.alley.feature.spawn.SpawnService;
import dev.revere.alley.library.menu.Menu;
import org.bukkit.Location;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CosmeticPreviewManager implements Listener {
    private static final long SOUND_COOLDOWN_MILLIS = 2_000L;
    private static final long MUSIC_COOLDOWN_MILLIS = 10_000L;

    private final Map<UUID, Long> soundCooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Long> musicCooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, String> previewMusic = new ConcurrentHashMap<>();
    private final Map<UUID, PreviewSession> killEffectSessions = new ConcurrentHashMap<>();

    public void preview(Player player, BaseCosmetic cosmetic, Menu returnMenu) {
        if (player == null || cosmetic == null) return;

        switch (cosmetic.getType()) {
            case SOUND_EFFECT -> previewSound(player, cosmetic);
            case KILL_MESSAGE -> previewKillMessages(player, cosmetic);
            case MVP_MUSIC -> previewMusic(player, cosmetic);
            case KILL_EFFECT -> previewKillEffect(player, cosmetic, returnMenu);
            default -> player.sendMessage(CC.translate("&cThis cosmetic cannot be previewed."));
        }
    }

    private void previewSound(Player player, BaseCosmetic cosmetic) {
        if (!(cosmetic instanceof BaseSoundEffect sound) || cosmetic.getName().equalsIgnoreCase("None")) {
            player.sendMessage(CC.translate("&cThis cosmetic has nothing to preview."));
            return;
        }
        if (!claimCooldown(player, soundCooldowns, SOUND_COOLDOWN_MILLIS)) return;

        sound.execute(player);
    }

    private void previewKillMessages(Player player, BaseCosmetic cosmetic) {
        if (!(cosmetic instanceof KillMessagePack pack) || pack.getDisplayableMessages().isEmpty()) {
            player.sendMessage(CC.translate("&cThis cosmetic has no kill messages to preview."));
            return;
        }

        player.sendMessage(CC.translate("&6&l" + cosmetic.getName() + " &fkill messages:"));
        for (String message : pack.getDisplayableMessages()) {
            player.sendMessage(CC.translate("&f" + message
                    .replace("{killer}", "&6" + player.getName() + "&f")
                    .replace("{victim}", "&cOpponent&f")));
        }
    }

    private void previewMusic(Player player, BaseCosmetic cosmetic) {
        if (!(cosmetic instanceof BaseMVPMusic music) || music.getSoundEventName() == null) {
            player.sendMessage(CC.translate("&cThis cosmetic has no music to preview."));
            return;
        }
        if (!claimCooldown(player, musicCooldowns, MUSIC_COOLDOWN_MILLIS)) return;

        String previous = previewMusic.put(player.getUniqueId(), music.getSoundEventName());
        if (previous != null && !previous.equals(music.getSoundEventName())) {
            player.stopSound(previous, SoundCategory.RECORDS);
        }
        music.play(java.util.Collections.singleton(player));
    }

    private void previewKillEffect(Player player, BaseCosmetic cosmetic, Menu returnMenu) {
        if (!(cosmetic instanceof BaseKillEffect effect) || cosmetic.getName().equalsIgnoreCase("None")) {
            player.sendMessage(CC.translate("&cThis cosmetic has no kill effect to preview."));
            return;
        }
        if (killEffectSessions.containsKey(player.getUniqueId())) {
            player.sendMessage(CC.translate("&cA kill effect preview is already running."));
            return;
        }
        if (!AlleyPlugin.getInstance().getServer().getPluginManager().isPluginEnabled("Citizens")) {
            player.sendMessage(CC.translate("&cCitizens is required to preview kill effects."));
            return;
        }

        Profile profile = AlleyPlugin.getInstance().getService(ProfileService.class).getProfile(player.getUniqueId());
        if (profile == null || (profile.getState() != ProfileState.LOBBY && profile.getState() != ProfileState.WAITING)) {
            player.sendMessage(CC.translate("&cKill effects can only be previewed in the lobby."));
            return;
        }

        SpawnService spawnService = AlleyPlugin.getInstance().getService(SpawnService.class);
        Location spawn = spawnService == null ? null : spawnService.getLocation();
        if (spawn == null || spawn.getWorld() == null) {
            player.sendMessage(CC.translate("&cThe server spawn is not configured."));
            return;
        }

        player.closeInventory();
        Location fixed = spawn.clone();
        Vector direction = fixed.getDirection().setY(0.0);
        if (direction.lengthSquared() < 0.0001) direction = new Vector(0.0, 0.0, 1.0);
        direction.normalize();

        Location dummyLocation = fixed.clone().add(direction.clone().multiply(5.0));
        dummyLocation.setDirection(fixed.toVector().subtract(dummyLocation.toVector()));
        fixed.setDirection(dummyLocation.clone().add(0.0, 1.0, 0.0).toVector()
                .subtract(fixed.clone().add(0.0, player.getEyeHeight(), 0.0).toVector()));
        player.teleport(fixed);

        PreviewSession session = new PreviewSession(fixed.clone(), returnMenu);
        killEffectSessions.put(player.getUniqueId(), session);
        boolean started = CitizensKillEffectPreview.start(player, effect, dummyLocation,
                () -> finishKillEffectPreview(player.getUniqueId(), true));
        if (!started) {
            killEffectSessions.remove(player.getUniqueId());
            player.sendMessage(CC.translate("&cThe kill effect preview could not be started."));
            if (returnMenu != null) returnMenu.openMenu(player);
        }
    }

    private boolean claimCooldown(Player player, Map<UUID, Long> cooldowns, long durationMillis) {
        long now = System.currentTimeMillis();
        long expiresAt = cooldowns.getOrDefault(player.getUniqueId(), 0L);
        if (expiresAt > now) {
            double seconds = Math.ceil((expiresAt - now) / 100.0) / 10.0;
            player.sendMessage(CC.translate("&cPlease wait " + seconds + "s before previewing again."));
            return false;
        }
        cooldowns.put(player.getUniqueId(), now + durationMillis);
        return true;
    }

    private void finishKillEffectPreview(UUID playerId, boolean reopenMenu) {
        PreviewSession session = killEffectSessions.remove(playerId);
        if (session == null) return;

        Player player = AlleyPlugin.getInstance().getServer().getPlayer(playerId);
        if (reopenMenu && player != null && player.isOnline() && session.returnMenu != null) {
            AlleyPlugin.getInstance().getServer().getScheduler().runTask(
                    AlleyPlugin.getInstance(), () -> session.returnMenu.openMenu(player));
        }
    }

    public void shutdown() {
        for (UUID playerId : killEffectSessions.keySet()) {
            CitizensKillEffectPreview.cancel(playerId);
        }
        killEffectSessions.clear();
        soundCooldowns.clear();
        musicCooldowns.clear();
        previewMusic.clear();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onMove(PlayerMoveEvent event) {
        PreviewSession session = killEffectSessions.get(event.getPlayer().getUniqueId());
        if (session != null) event.setTo(session.fixedLocation.clone());
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        if (killEffectSessions.containsKey(playerId)) {
            CitizensKillEffectPreview.cancel(playerId);
        }
        finishKillEffectPreview(playerId, false);
        soundCooldowns.remove(playerId);
        musicCooldowns.remove(playerId);
        previewMusic.remove(playerId);
    }

    private static final class PreviewSession {
        private final Location fixedLocation;
        private final Menu returnMenu;

        private PreviewSession(Location fixedLocation, Menu returnMenu) {
            this.fixedLocation = fixedLocation;
            this.returnMenu = returnMenu;
        }
    }
}
