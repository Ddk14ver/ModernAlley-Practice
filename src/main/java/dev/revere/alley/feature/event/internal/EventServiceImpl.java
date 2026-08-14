package dev.revere.alley.feature.event.internal;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.bootstrap.AlleyContext;
import dev.revere.alley.bootstrap.annotation.Service;
import dev.revere.alley.common.text.ClickableUtil;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.config.ConfigService;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.core.profile.enums.ProfileState;
import dev.revere.alley.feature.arena.Arena;
import dev.revere.alley.feature.arena.ArenaService;
import dev.revere.alley.feature.arena.internal.types.StandAloneArena;
import dev.revere.alley.feature.coin.CoinRewardService;
import dev.revere.alley.feature.event.EventMode;
import dev.revere.alley.feature.event.EventService;
import dev.revere.alley.feature.event.EventState;
import dev.revere.alley.feature.event.EventType;
import dev.revere.alley.feature.event.HostedEvent;
import dev.revere.alley.feature.event.skywars.SkyWarsLoot;
import dev.revere.alley.feature.event.skywars.SkyWarsMatch;
import dev.revere.alley.feature.hotbar.HotbarService;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.kit.KitService;
import dev.revere.alley.feature.kit.setting.types.mode.KitSettingSumo;
import dev.revere.alley.feature.match.Match;
import dev.revere.alley.feature.match.MatchService;
import dev.revere.alley.feature.match.internal.types.FFAMatch;
import dev.revere.alley.feature.match.model.GameParticipant;
import dev.revere.alley.feature.match.model.internal.MatchGamePlayer;
import dev.revere.alley.feature.spawn.SpawnService;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service(provides = EventService.class, priority = 900)
public class EventServiceImpl implements EventService {
    private final AlleyPlugin plugin;
    private final ProfileService profileService;
    private final MatchService matchService;
    private final ArenaService arenaService;
    private final SpawnService spawnService;
    private final HotbarService hotbarService;
    private final CoinRewardService coinRewardService;
    private final ConfigService configService;
    private final KitService kitService;

    private final List<HostedEvent> events = new CopyOnWriteArrayList<>();
    private final Map<Match, HostedEvent> matchOwners = new ConcurrentHashMap<>();
    private final AtomicInteger numericIds = new AtomicInteger(1);
    private boolean shuttingDown;

    public EventServiceImpl(AlleyPlugin plugin, ProfileService profileService, MatchService matchService,
                            ArenaService arenaService, SpawnService spawnService, HotbarService hotbarService,
                            CoinRewardService coinRewardService, ConfigService configService, KitService kitService) {
        this.plugin = plugin;
        this.profileService = profileService;
        this.matchService = matchService;
        this.arenaService = arenaService;
        this.spawnService = spawnService;
        this.hotbarService = hotbarService;
        this.coinRewardService = coinRewardService;
        this.configService = configService;
        this.kitService = kitService;
    }

    @Override
    public void shutdown(AlleyContext context) {
        this.shuttingDown = true;
        for (HostedEvent event : new ArrayList<>(this.events)) {
            if (event.getState() != EventState.ENDED) {
                endEvent(event, null, "Server shutdown");
            }
        }
        this.matchOwners.clear();
    }

    @Override
    public HostedEvent hostEvent(Player host, EventType type, EventMode mode, Kit kit) {
        if (host == null || type == null || mode == null || kit == null || !kit.isEnabled()) {
            return null;
        }

        Profile profile = this.profileService.getProfile(host.getUniqueId());
        if (profile.getState() != ProfileState.LOBBY) {
            host.sendMessage(CC.translate("&cYou must be in the lobby to host an event."));
            return null;
        }
        if (type == EventType.SUMO && !kit.isSettingEnabled(KitSettingSumo.class)) {
            host.sendMessage(CC.translate("&cSumo events require a kit with the Sumo setting enabled."));
            return null;
        }
        Kit skyWarsResourceKit = null;
        if (type == EventType.SKYWARS) {
            skyWarsResourceKit = this.kitService.getKit(kit.getSkyWarsResourceKit());
            if (!SkyWarsLoot.isUsableResourceKit(skyWarsResourceKit)) {
                host.sendMessage(CC.translate("&cSkyWars requires a selected resource kit with at least 7 non-air items."));
                return null;
            }
            if (!this.arenaService.hasSkyWarsArena(kit)) {
                host.sendMessage(CC.translate("&cSkyWars requires an enabled dedicated arena with this kit, "
                        + "at least one chest, and at least 4 spawns."));
                return null;
            }
        }
        if (getEvents().stream().anyMatch(event -> event.getHostUuid().equals(host.getUniqueId()))) {
            host.sendMessage(CC.translate("&cYou already have an active or queued event."));
            return null;
        }

        int queueLimit = Math.max(1, this.configService.getSettingsConfig().getInt("events.queue-limit", 9));
        if (getEvents().size() >= queueLimit) {
            host.sendMessage(CC.translate("&cThe event queue is currently full."));
            return null;
        }

        int maxPlayers = Math.max(2, this.configService.getSettingsConfig().getInt("events.max-players", 32));
        HostedEvent event = new HostedEvent(this.numericIds.getAndIncrement(), host, type, mode, kit,
                skyWarsResourceKit, maxPlayers);
        this.events.add(event);
        host.sendMessage(CC.translate("&aQueued a &6" + event.getDisplayName() + " &aevent with the &6"
                + kit.getDisplayName() + " &akit."));
        activateNextEvent();
        return event;
    }

    @Override
    public boolean joinEvent(Player player, HostedEvent event) {
        if (player == null || event == null || event.getState() != EventState.STARTING) {
            if (player != null) player.sendMessage(CC.translate("&cThat event is not accepting players."));
            return false;
        }

        Profile profile = this.profileService.getProfile(player.getUniqueId());
        if (profile.getState() != ProfileState.LOBBY && profile.getGameEvent() != event) {
            player.sendMessage(CC.translate("&cYou cannot join an event right now."));
            return false;
        }
        if (event.isParticipant(player.getUniqueId())) {
            player.sendMessage(CC.translate("&cYou have already joined this event."));
            return false;
        }
        if (event.getParticipants().size() >= event.getMaxPlayers()) {
            player.sendMessage(CC.translate("&cThat event is full."));
            return false;
        }

        event.getParticipants().add(player.getUniqueId());
        profile.setGameEvent(event);
        profile.setState(ProfileState.PLAYING_EVENT);
        event.getOnlinePlayers().forEach(online -> online.sendMessage(CC.translate(
                "&6" + player.getName() + " &fjoined the event. &7(&6" + event.getParticipants().size()
                        + "&7/&6" + event.getMaxPlayers() + "&7)")));
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1F, 1.4F);

        if (event.getParticipants().size() >= event.getMaxPlayers()) {
            forceStart(event);
        }
        return true;
    }

    @Override
    public void leaveEvent(Player player) {
        HostedEvent event = getPlayerEvent(player);
        if (event == null) {
            player.sendMessage(CC.translate("&cYou are not in an event."));
            return;
        }

        event.getParticipants().remove(player.getUniqueId());
        event.getRemainingPlayers().remove(player.getUniqueId());
        event.getRoundWinners().remove(player.getUniqueId());

        Profile profile = this.profileService.getProfile(player.getUniqueId());
        Match match = profile.getMatch();
        profile.setGameEvent(null);
        if (match != null && event.getState() == EventState.RUNNING) {
            match.handleDisconnect(player);
        } else {
            releasePlayer(player.getUniqueId());
            checkAfterDeparture(event);
        }
        player.sendMessage(CC.translate("&cYou left the event."));
    }

    @Override
    public void forceStart(HostedEvent event) {
        if (event == null || event.getState() != EventState.STARTING) return;
        cancelCountdown(event);
        startEvent(event);
    }

    @Override
    public void cancelEvent(HostedEvent event, String reason) {
        if (event == null || event.getState() == EventState.ENDED) return;
        if (event.getState() == EventState.RUNNING && !event.getActiveMatches().isEmpty()) {
            return;
        }
        endEvent(event, null, reason == null ? "Cancelled" : reason);
    }

    @Override
    public void handleMatchEnd(Match match) {
        HostedEvent event = this.matchOwners.remove(match);
        if (event == null || event.getState() == EventState.ENDED) return;

        event.getActiveMatches().remove(match);
        UUID winner = resolveWinner(match);

        if (event.getMode() == EventMode.LAST_MAN_STANDING) {
            endEvent(event, winner, winner == null ? "No winner" : null);
            return;
        }

        for (GameParticipant<MatchGamePlayer> participant : match.getParticipants()) {
            for (MatchGamePlayer gamePlayer : participant.getAllPlayers()) {
                UUID uuid = gamePlayer.getUuid();
                if (uuid.equals(winner)) {
                    event.getRoundWinners().add(uuid);
                    Profile profile = this.profileService.getProfile(uuid);
                    profile.setGameEvent(event);
                    profile.setState(ProfileState.PLAYING_EVENT);
                } else {
                    event.getRemainingPlayers().remove(uuid);
                    releasePlayer(uuid);
                }
            }
        }

        if (!event.getActiveMatches().isEmpty()) return;

        event.getRemainingPlayers().clear();
        event.getRemainingPlayers().addAll(event.getRoundWinners());
        if (event.getRemainingPlayers().size() <= 1) {
            UUID finalWinner = event.getRemainingPlayers().isEmpty() ? null : event.getRemainingPlayers().get(0);
            endEvent(event, finalWinner, finalWinner == null ? "No winner" : null);
            return;
        }

        Bukkit.getScheduler().runTaskLater(this.plugin, () -> startBracketRound(event), 60L);
    }

    @Override
    public void handleDisconnect(Player player) {
        HostedEvent event = getPlayerEvent(player);
        if (event == null) return;
        Profile profile = this.profileService.getProfile(player.getUniqueId());
        if (profile.getMatch() != null) return;

        event.getParticipants().remove(player.getUniqueId());
        event.getRemainingPlayers().remove(player.getUniqueId());
        event.getRoundWinners().remove(player.getUniqueId());
        profile.setGameEvent(null);
        profile.setState(ProfileState.LOBBY);
        checkAfterDeparture(event);
    }

    @Override
    public HostedEvent getEvent(int numericId) {
        return getEvents().stream().filter(event -> event.getNumericId() == numericId).findFirst().orElse(null);
    }

    @Override
    public HostedEvent getPlayerEvent(Player player) {
        if (player == null) return null;
        Profile profile = this.profileService.getProfile(player.getUniqueId());
        if (profile.getGameEvent() != null && profile.getGameEvent().getState() != EventState.ENDED) {
            return profile.getGameEvent();
        }
        return getEvents().stream().filter(event -> event.isParticipant(player.getUniqueId())).findFirst().orElse(null);
    }

    @Override
    public List<HostedEvent> getEvents() {
        return this.events.stream()
                .filter(event -> event.getState() != EventState.ENDED)
                .sorted(Comparator.comparingInt(HostedEvent::getNumericId))
                .collect(Collectors.toUnmodifiableList());
    }

    private void activateNextEvent() {
        boolean busy = this.events.stream().anyMatch(event ->
                event.getState() == EventState.STARTING || event.getState() == EventState.RUNNING);
        if (busy) return;

        HostedEvent next = this.events.stream()
                .filter(event -> event.getState() == EventState.QUEUED)
                .min(Comparator.comparingInt(HostedEvent::getNumericId))
                .orElse(null);
        if (next == null) return;

        Player host = Bukkit.getPlayer(next.getHostUuid());
        if (host == null || !host.isOnline()) {
            endEvent(next, null, "Host went offline");
            return;
        }

        next.setState(EventState.STARTING);
        next.setCountdown(Math.max(5,
                this.configService.getSettingsConfig().getInt("events.start-countdown-seconds", 60)));
        joinEvent(host, next);
        broadcastEventInvitation(next);
        startCountdown(next);
    }

    private void startCountdown(HostedEvent event) {
        event.setCountdownTask(Bukkit.getScheduler().runTaskTimer(this.plugin, () -> {
            if (event.getState() != EventState.STARTING) {
                cancelCountdown(event);
                return;
            }

            int seconds = event.getCountdown();
            if (seconds <= 0) {
                cancelCountdown(event);
                startEvent(event);
                return;
            }
            if (seconds <= 5 || seconds % 20 == 0) {
                Bukkit.broadcastMessage(CC.translate("&6" + event.getDisplayName() + " &fevent starts in &6"
                        + seconds + "&fs. &7(/event join " + event.getNumericId() + ")"));
            }
            event.setCountdown(seconds - 1);
        }, 20L, 20L));
    }

    private void startEvent(HostedEvent event) {
        List<UUID> online = event.getParticipants().stream()
                .filter(uuid -> Bukkit.getPlayer(uuid) != null)
                .collect(Collectors.toCollection(ArrayList::new));
        event.getParticipants().retainAll(online);
        if (online.size() < 2) {
            endEvent(event, null, "Not enough players");
            return;
        }

        event.setState(EventState.RUNNING);
        event.getRemainingPlayers().clear();
        event.getRemainingPlayers().addAll(online);
        Bukkit.broadcastMessage(CC.translate("&6&l" + event.getDisplayName() + " Event &fhas started with &6"
                + online.size() + " &fplayers!"));

        if (event.getMode() == EventMode.BRACKETS) {
            startBracketRound(event);
        } else {
            startLastManStanding(event);
        }
    }

    private void startBracketRound(HostedEvent event) {
        if (event.getState() != EventState.RUNNING) return;

        List<UUID> players = event.getRemainingPlayers().stream()
                .filter(uuid -> Bukkit.getPlayer(uuid) != null)
                .collect(Collectors.toCollection(ArrayList::new));
        if (players.size() <= 1) {
            endEvent(event, players.isEmpty() ? null : players.get(0), players.isEmpty() ? "No winner" : null);
            return;
        }

        Collections.shuffle(players);
        event.setRound(event.getRound() + 1);
        event.getRoundWinners().clear();
        event.getActiveMatches().clear();
        if ((players.size() & 1) == 1) {
            UUID bye = players.remove(players.size() - 1);
            event.getRoundWinners().add(bye);
            Player byePlayer = Bukkit.getPlayer(bye);
            if (byePlayer != null) byePlayer.sendMessage(CC.translate("&aYou received a bye for this round."));
        }

        Bukkit.broadcastMessage(CC.translate("&6" + event.getDisplayName() + " &fround &6" + event.getRound()
                + " &fis starting. &7(" + event.getRemainingPlayers().size() + " players left)"));

        for (int index = 0; index < players.size(); index += 2) {
            Player first = Bukkit.getPlayer(players.get(index));
            Player second = Bukkit.getPlayer(players.get(index + 1));
            if (first == null || second == null) {
                if (first != null) event.getRoundWinners().add(first.getUniqueId());
                if (second != null) event.getRoundWinners().add(second.getUniqueId());
                continue;
            }

            Arena arena = this.arenaService.getRandomArena(event.getKit());
            if (arena == null) {
                endEvent(event, null, "No compatible arena for " + event.getKit().getName());
                return;
            }

            GameParticipant<MatchGamePlayer> participantA = participant(first);
            GameParticipant<MatchGamePlayer> participantB = participant(second);
            this.matchService.createAndStartMatch(event.getKit(), arena, participantA, participantB,
                    false, false, false);
            Match match = this.profileService.getProfile(first.getUniqueId()).getMatch();
            if (match != null) {
                event.getActiveMatches().add(match);
                this.matchOwners.put(match, event);
            }
        }

        if (event.getActiveMatches().isEmpty()) {
            event.getRemainingPlayers().clear();
            event.getRemainingPlayers().addAll(event.getRoundWinners());
            Bukkit.getScheduler().runTaskLater(this.plugin, () -> startBracketRound(event), 20L);
        }
    }

    private void startLastManStanding(HostedEvent event) {
        if (event.getType() == EventType.SKYWARS) {
            startSkyWars(event);
            return;
        }

        Arena arena = this.arenaService.getRandomArena(event.getKit());
        if (arena == null) {
            endEvent(event, null, "No compatible arena for " + event.getKit().getName());
            return;
        }

        List<GameParticipant<MatchGamePlayer>> participants = event.getRemainingPlayers().stream()
                .map(Bukkit::getPlayer)
                .filter(player -> player != null && player.isOnline())
                .map(this::participant)
                .collect(Collectors.toCollection(ArrayList::new));
        this.matchService.createAndStartMatch(event.getKit(), arena, participants);

        Player first = Bukkit.getPlayer(event.getRemainingPlayers().get(0));
        Match match = first == null ? null : this.profileService.getProfile(first.getUniqueId()).getMatch();
        if (match == null) {
            endEvent(event, null, "Failed to create event match");
            return;
        }
        event.getActiveMatches().add(match);
        this.matchOwners.put(match, event);
    }

    private void startSkyWars(HostedEvent event) {
        Arena arena = this.arenaService.getRandomSkyWarsArena(event.getKit());
        if (arena == null) {
            endEvent(event, null, "No dedicated SkyWars arena for " + event.getKit().getName());
            return;
        }
        if (SkyWarsLoot.countChests(arena) == 0) {
            discardTemporaryArena(arena);
            endEvent(event, null, "The selected SkyWars arena has no chests");
            return;
        }

        List<GameParticipant<MatchGamePlayer>> participants = event.getRemainingPlayers().stream()
                .map(Bukkit::getPlayer)
                .filter(player -> player != null && player.isOnline())
                .map(this::participant)
                .collect(Collectors.toCollection(ArrayList::new));
        if (participants.size() < 2) {
            discardTemporaryArena(arena);
            endEvent(event, null, "Not enough players");
            return;
        }

        SkyWarsMatch match = this.matchService.createAndStartSkyWarsMatch(
                event.getKit(), arena, participants, event.getSkyWarsResourceKit());
        if (match == null) {
            discardTemporaryArena(arena);
            endEvent(event, null, "Failed to create SkyWars match");
            return;
        }

        event.getActiveMatches().add(match);
        this.matchOwners.put(match, event);
    }

    private void discardTemporaryArena(Arena arena) {
        if (arena instanceof StandAloneArena standAloneArena && standAloneArena.isTemporaryCopy()) {
            this.arenaService.deleteTemporaryArena(standAloneArena);
        }
    }

    private GameParticipant<MatchGamePlayer> participant(Player player) {
        return new GameParticipant<>(new MatchGamePlayer(player.getUniqueId(), player.getName()));
    }

    private UUID resolveWinner(Match match) {
        if (match instanceof FFAMatch ffaMatch && ffaMatch.getWinningParticipant() != null) {
            return ffaMatch.getWinningParticipant().getLeader().getUuid();
        }
        if (!match.getPlayerWinners().isEmpty()) {
            return match.getPlayerWinners().iterator().next();
        }
        return match.getParticipants().stream()
                .filter(participant -> !participant.isAllDead() && !participant.isAllEliminated())
                .map(participant -> participant.getLeader().getUuid())
                .findFirst()
                .orElse(null);
    }

    private void endEvent(HostedEvent event, UUID winner, String reason) {
        if (event.getState() == EventState.ENDED) return;
        cancelCountdown(event);
        event.setState(EventState.ENDED);
        event.getActiveMatches().forEach(this.matchOwners::remove);

        if (winner != null) {
            Player winnerPlayer = Bukkit.getPlayer(winner);
            if (winnerPlayer != null) {
                Profile winnerProfile = this.profileService.getProfile(winner);
                winnerProfile.getProfileData().setTournamentWins(
                        winnerProfile.getProfileData().getTournamentWins() + 1);
                this.coinRewardService.rewardTournamentWin(winnerPlayer);
                winnerProfile.save();
                Bukkit.broadcastMessage("");
                Bukkit.broadcastMessage(CC.translate("&6&l" + event.getDisplayName() + " EVENT WINNER"));
                Bukkit.broadcastMessage(CC.translate(" &f" + winnerPlayer.getName() + " &7has won the event!"));
                Bukkit.broadcastMessage("");
            }
        } else if (reason != null) {
            Bukkit.broadcastMessage(CC.translate("&cThe &f" + event.getDisplayName() + " &cevent ended: &f" + reason));
        }

        for (UUID uuid : new ArrayList<>(event.getParticipants())) {
            releasePlayer(uuid);
        }
        event.getParticipants().clear();
        event.getRemainingPlayers().clear();
        event.getRoundWinners().clear();
        event.getActiveMatches().clear();
        this.events.remove(event);
        if (!this.shuttingDown && this.plugin.isEnabled()) {
            Bukkit.getScheduler().runTask(this.plugin, this::activateNextEvent);
        }
    }

    private void releasePlayer(UUID uuid) {
        Profile profile = this.profileService.getProfile(uuid);
        if (profile.getGameEvent() != null) profile.setGameEvent(null);
        if (profile.getMatch() != null) return;

        profile.setState(ProfileState.LOBBY);
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            this.spawnService.teleportToSpawn(player);
            this.hotbarService.applyHotbarItems(player);
        }
    }

    private void checkAfterDeparture(HostedEvent event) {
        if (event.getState() == EventState.STARTING && event.getParticipants().isEmpty()) {
            endEvent(event, null, "All players left");
            return;
        }
        if (event.getState() == EventState.RUNNING && event.getActiveMatches().isEmpty()
                && event.getRemainingPlayers().size() <= 1) {
            UUID winner = event.getRemainingPlayers().isEmpty() ? null : event.getRemainingPlayers().get(0);
            endEvent(event, winner, winner == null ? "All players left" : null);
        }
    }

    private void cancelCountdown(HostedEvent event) {
        if (event.getCountdownTask() != null) {
            event.getCountdownTask().cancel();
            event.setCountdownTask(null);
        }
    }

    private void broadcastEventInvitation(HostedEvent event) {
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(CC.translate("&6&l" + event.getDisplayName() + " EVENT"));
        Bukkit.broadcastMessage(CC.translate(" &fHosted by: &6" + event.getHostName()));
        Bukkit.broadcastMessage(CC.translate(" &fKit: &6" + event.getKit().getDisplayName()));
        TextComponent joinLine = new TextComponent(CC.translate(" &a/event join " + event.getNumericId() + " "));
        joinLine.addExtra(ClickableUtil.createComponent(
                "&a&l(Click to join)",
                "/event join " + event.getNumericId(),
                "&aClick to join this event."
        ));
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.spigot().sendMessage(joinLine);
        }
        Bukkit.broadcastMessage("");
    }
}
