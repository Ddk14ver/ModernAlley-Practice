package dev.revere.alley.feature.match.internal.types;

import dev.revere.alley.common.ListenerUtil;
import dev.revere.alley.common.PlayerUtil;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.locale.LocaleService;
import dev.revere.alley.core.locale.internal.impl.VisualsLocaleImpl;
import dev.revere.alley.core.locale.internal.impl.message.GameMessagesLocaleImpl;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.feature.arena.Arena;
import dev.revere.alley.feature.arena.ArenaService;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.kit.KitService;
import dev.revere.alley.feature.match.model.GameParticipant;
import dev.revere.alley.feature.match.model.GamePlayer;
import dev.revere.alley.feature.match.model.TeamGameParticipant;
import dev.revere.alley.feature.match.model.internal.MatchGamePlayer;
import dev.revere.alley.feature.queue.Queue;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 27/06/2026
 */
@Getter
public class HideAndSeekMatch extends DefaultMatch {
    private final GameParticipant<MatchGamePlayer> participantA;
    private final GameParticipant<MatchGamePlayer> participantB;

    private BukkitTask seekerReleaseTask;
    private BukkitTask gameEndTask;
    private BukkitTask bossBarUpdateTask;
    private boolean timeExpired = false;

    private final int hidingTimeSeconds = 180;
    private final int gameTimeSeconds = 600;
    private final int hiderHealthHearts = 3;

    private BossBar countdownBar;

    private final ArenaService arenaService = plugin.getService(ArenaService.class);
    private final Arena intermissionArena = arenaService.getArenaByName("IntermissionArena");

    private Location getIntermissionSpawn() {
        if (intermissionArena != null && intermissionArena.getPos1() != null) {
            return intermissionArena.getPos1();
        }
        return getArena().getPos1() != null ? getArena().getPos1() : getArena().getCenter();
    }

    private Location getHiderSpawn() {
        if (getArena().getPos2() != null) {
            return getArena().getPos2();
        }
        return getArena().getPos1() != null ? getArena().getPos1() : getArena().getCenter();
    }

    public HideAndSeekMatch(Queue queue, Kit kit, Arena arena, boolean ranked, GameParticipant<MatchGamePlayer> participantA, GameParticipant<MatchGamePlayer> participantB) {
        super(queue, kit, arena, ranked, participantA, participantB);

        List<MatchGamePlayer> allPlayers = new ArrayList<>();
        allPlayers.addAll(participantA.getPlayers());
        allPlayers.addAll(participantB.getPlayers());
        Collections.shuffle(allPlayers);

        List<MatchGamePlayer> seekers = new ArrayList<>();
        List<MatchGamePlayer> hiders = new ArrayList<>();

        int seekerCount = allPlayers.size() > 2
                ? Math.max(1, (int) Math.ceil(allPlayers.size() * 0.2))
                : 1;
        for (int i = 0; i < allPlayers.size(); i++) {
            if (i < seekerCount) seekers.add(allPlayers.get(i));
            else hiders.add(allPlayers.get(i));
        }

        this.participantA = new TeamGameParticipant<>(seekers.get(0));
        this.participantB = new TeamGameParticipant<>(hiders.get(0));
        for (int i = 1; i < seekers.size(); i++) this.participantA.addPlayer(seekers.get(i));
        for (int i = 1; i < hiders.size(); i++) this.participantB.addPlayer(hiders.get(i));
    }

    @Override
    public void setupPlayer(Player player) {
        MatchGamePlayer gamePlayer = getGamePlayer(player);
        if (gamePlayer == null) return;

        gamePlayer.setDead(false);
        PlayerUtil.reset(player, true, true);

        boolean isHider = getParticipantB().containsPlayer(player.getUniqueId());
        giveLoadout(player, getRoleKit(player));

        if (isHider) {
            Location hiderSpawn = getHiderSpawn();
            if (hiderSpawn != null) {
                player.teleport(hiderSpawn);
            }
            player.setMaxHealth(hiderHealthHearts * 2.0);
            player.setHealth(player.getMaxHealth());
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, false));
            player.sendTitle(CC.translate("&a&lYOU ARE A HIDER"), CC.translate("&7Hide before the seekers arrive!"), 10, 60, 10);
            player.sendMessage(CC.translate("&a&lYou are a &6&lHIDER&a&l! &7Hide now — seekers will be released in &e3 minutes&7."));
        } else {
            ListenerUtil.teleportAndClearSpawn(player, getIntermissionSpawn());
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 9, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false));
            player.sendTitle(CC.translate("&c&lYOU ARE A SEEKER"), CC.translate("&7You'll be released in 3 minutes!"), 10, 60, 10);
            player.sendMessage(CC.translate("&c&lYou are a &6&lSEEKER&c&l! &7The hiders are hiding. You will be released in &e3 minutes&7."));
        }
    }

    @Override
    public void denyPlayerMovement(List<GameParticipant<MatchGamePlayer>> participants) {
        GameParticipant<?> hiders = this.getParticipantB();
        Location hidersSpawn = this.getArena().getPos2();
        if (hiders == null || hidersSpawn == null) return;
        for (GamePlayer gamePlayer : hiders.getPlayers()) {
            Player pp = gamePlayer.getTeamPlayer();
            if (pp != null) this.teleportBackIfMoved(pp, hidersSpawn);
        }
    }

    @Override
    public void startMatch() {
        super.startMatch();

        int preMatchCountdownSeconds = 5;
        int totalSeconds = hidingTimeSeconds + preMatchCountdownSeconds;

        // --- BossBar for hiding phase countdown ---
        countdownBar = Bukkit.createBossBar(
                CC.translate("&6&lHIDE & SEEK &8| &7Seekers released in &e" + formatTime(totalSeconds)),
                BarColor.YELLOW, BarStyle.SOLID);
        addAllPlayersToBar();

        // Use a tick counter instead of getElapsedTime() for precise timing
        bossBarUpdateTask = plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {
            int tick = 0;
            @Override public void run() {
                tick++;
                int remaining = totalSeconds - tick / 20;
                if (remaining <= 0) {
                    if (countdownBar != null) {
                        countdownBar.setTitle(CC.translate("&c&lSEEKERS RELEASED!"));
                        countdownBar.setColor(BarColor.RED);
                    }
                    bossBarUpdateTask.cancel();
                    return;
                }

                // Last 10s countdown
                if (remaining <= 10 && tick % 20 == 0) {
                    playPlingToAll();
                    broadcastToAll(CC.translate("&e&l" + remaining + " &7second" + (remaining == 1 ? "" : "s") + " until seekers are released!"));
                }

                if (countdownBar != null) {
                    countdownBar.setProgress(Math.max(0, Math.min(1, (double) remaining / totalSeconds)));
                    countdownBar.setTitle(CC.translate("&6&lHIDE & SEEK &8| &7Seekers released in &e" + formatTime(remaining)));
                }
            }
        }, 0L, 1L);

        long totalDelayTicks = totalSeconds * 20L;
        this.seekerReleaseTask = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            // Cancel hiding phase timer
            if (bossBarUpdateTask != null) bossBarUpdateTask.cancel();

            LocaleService localeService = plugin.getService(LocaleService.class);
            if (localeService.getBoolean(VisualsLocaleImpl.TITLE_MATCH_SEEKERS_RELEASED_ENABLED_BOOLEAN)) {
                this.sendTitle(
                        localeService.getString(VisualsLocaleImpl.TITLE_MATCH_SEEKERS_RELEASED_HEADER),
                        localeService.getString(VisualsLocaleImpl.TITLE_MATCH_SEEKERS_RELEASED_FOOTER),
                        localeService.getInt(VisualsLocaleImpl.TITLE_MATCH_SEEKERS_RELEASED_FADE_IN),
                        localeService.getInt(VisualsLocaleImpl.TITLE_MATCH_SEEKERS_RELEASED_STAY),
                        localeService.getInt(VisualsLocaleImpl.TITLE_MATCH_SEEKERS_RELEASED_FADEOUT),
                        true);
            }
            playSound(Sound.ENTITY_ENDER_DRAGON_GROWL);

            if (countdownBar != null) {
                countdownBar.setTitle(CC.translate("&c&lSEEKING PHASE &8| &7Time left: &e" + formatTime(gameTimeSeconds)));
                countdownBar.setColor(BarColor.RED);
                countdownBar.setProgress(1.0);
            }

            getParticipantA().getPlayers().forEach(seeker -> {
                Player p = plugin.getServer().getPlayer(seeker.getUuid());
                if (p != null) p.teleport(getArena().getPos2());
            });

            broadcastToAll(CC.translate("&c&lSEEKERS HAVE BEEN RELEASED! &7They have &e" + formatTime(gameTimeSeconds) + " &7to find all hiders."));

            // Seeking phase countdown
            BukkitTask seekingCountdown = plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {
                int sec = gameTimeSeconds;
                @Override public void run() {
                    sec--;
                    if (sec <= 0 || countdownBar == null) return;
                    countdownBar.setProgress(Math.max(0, (double) sec / gameTimeSeconds));
                    countdownBar.setTitle(CC.translate("&c&lSEEKING PHASE &8| &7Time left: &e" + formatTime(sec)));
                    if (sec <= 10 && sec >= 1) {
                        playPlingToAll();
                        broadcastToAll(CC.translate("&e&l" + sec + " &7second" + (sec == 1 ? "" : "s") + " remaining!"));
                    }
                }
            }, 20L, 20L);

            this.gameEndTask = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                seekingCountdown.cancel();
                this.timeExpired = true;
                broadcastToAll(CC.translate("&c&lTIME IS UP! &7Hiders win!"));
                new ArrayList<>(getParticipantA().getPlayers()).forEach(seeker -> {
                    Player p = Bukkit.getPlayer(seeker.getUuid());
                    if (p != null && !seeker.isDead()) handleDeath(p, EntityDamageEvent.DamageCause.CUSTOM);
                });
            }, gameTimeSeconds * 20L);

        }, totalDelayTicks);
    }

    @Override
    public void handleDeath(Player player, EntityDamageEvent.DamageCause cause) {
        GameParticipant<MatchGamePlayer> participant = getParticipant(player);
        if (participant == getParticipantA()) {
            if (this.timeExpired) {
                super.handleDeath(player, cause);
            } else {
                if (gameEndTask != null) {
                    this.sendMessage(this.plugin.getService(LocaleService.class).getString(GameMessagesLocaleImpl.MATCH_SEEKER_RESPAWNED)
                            .replace("{player}", player.getName())
                            .replace("{name-color}", String.valueOf(this.plugin.getService(ProfileService.class).getProfile(player.getUniqueId()).getNameColor())));
                }
                this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> handleRespawn(player), 1L);
            }
        } else if (participant == getParticipantB()) {
            super.handleDeath(player, cause);
        }
    }

    @Override
    public void handleRespawn(Player player) {
        player.spigot().respawn();
        PlayerUtil.reset(player, true, false);
        if (gameEndTask == null) ListenerUtil.teleportAndClearSpawn(player, getIntermissionSpawn());
        else ListenerUtil.teleportAndClearSpawn(player, getArena().getPos2());
        giveLoadout(player, getRoleKit(player));
        if (getParticipantA().containsPlayer(player.getUniqueId())) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 9, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 0, false, false));
        }
    }

    @Override
    public void handleDisconnect(Player player) {
        MatchGamePlayer gamePlayer = getFromAllGamePlayers(player);
        if (gamePlayer == null || gamePlayer.isDead()) return;
        // Mark dead before super call so DefaultMatch.handleDisconnect skips respawn logic
        gamePlayer.setDead(true);
        gamePlayer.setEliminated(true);
        gamePlayer.setDisconnected(true);
        String team = getParticipant(player) == getParticipantA() ? "Seeker" : "Hider";
        sendMessage(CC.translate("&c&lDISCONNECT! &f" + team + " &c" + player.getName() + " &fhas disconnected."));

        // Let base class clean up (finalizePlayer, etc.)
        super.handleDisconnect(player);
        checkForConclusion(player, null);
    }

    @Override
    public void endMatch() {
        if (this.seekerReleaseTask != null) this.seekerReleaseTask.cancel();
        if (this.gameEndTask != null) this.gameEndTask.cancel();
        if (this.bossBarUpdateTask != null) this.bossBarUpdateTask.cancel();
        if (this.countdownBar != null) { countdownBar.removeAll(); countdownBar = null; }
        super.endMatch();
    }

    @Override public boolean canEndRound() { return super.canEndRound() || this.timeExpired; }
    @Override public List<GameParticipant<MatchGamePlayer>> getParticipants() { return Arrays.asList(this.participantA, this.participantB); }
    @Override public GameParticipant<MatchGamePlayer> getParticipantA() { return participantA; }
    @Override public GameParticipant<MatchGamePlayer> getParticipantB() { return participantB; }

    // --- Helpers ---
    private void addAllPlayersToBar() {
        getParticipants().forEach(p -> p.getPlayers().forEach(mp -> {
            Player pl = Bukkit.getPlayer(mp.getUuid());
            if (pl != null) countdownBar.addPlayer(pl);
        }));
    }

    private void playPlingToAll() {
        getParticipants().forEach(p -> p.getPlayers().forEach(mp -> {
            Player pl = Bukkit.getPlayer(mp.getUuid());
            if (pl != null) pl.playSound(pl.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
        }));
    }

    private void broadcastToAll(String msg) {
        getParticipants().forEach(p -> p.getPlayers().forEach(mp -> {
            Player pl = Bukkit.getPlayer(mp.getUuid());
            if (pl != null) pl.sendMessage(msg);
        }));
    }

    private static String formatTime(int totalSeconds) {
        int min = totalSeconds / 60;
        int sec = totalSeconds % 60;
        return min + ":" + (sec < 10 ? "0" : "") + sec;
    }

    private Kit getRoleKit(Player player) {
        boolean isSeeker = getParticipantA().containsPlayer(player.getUniqueId());
        String roleKitName = isSeeker ? getKit().getHideAndSeekSeekerKit() : getKit().getHideAndSeekHiderKit();
        if (roleKitName == null || roleKitName.isEmpty()) return getKit();

        Kit roleKit = plugin.getService(KitService.class).getKit(roleKitName);
        return roleKit != null ? roleKit : getKit();
    }
}
