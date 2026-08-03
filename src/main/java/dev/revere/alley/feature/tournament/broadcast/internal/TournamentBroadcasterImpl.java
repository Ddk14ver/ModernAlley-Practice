package dev.revere.alley.feature.tournament.broadcast.internal;

import dev.revere.alley.bootstrap.annotation.Service;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.feature.tournament.broadcast.BroadcastEvent;
import dev.revere.alley.feature.tournament.broadcast.TournamentBroadcaster;
import dev.revere.alley.feature.tournament.broadcast.TournamentPlacements;
import dev.revere.alley.feature.tournament.internal.helpers.TournamentMessageBuilder;
import dev.revere.alley.feature.tournament.model.Tournament;
import dev.revere.alley.feature.tournament.model.TournamentParticipant;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * @author Remi
 * @project alley-practice
 * @date 8/08/2025
 */
@Service(provides = TournamentBroadcaster.class, priority = 1100)
public class TournamentBroadcasterImpl implements TournamentBroadcaster {
    @Override
    public void broadcast(BroadcastEvent event) {
        if (event instanceof BroadcastEvent.TournamentHosted) {
            handleTournamentHosted((BroadcastEvent.TournamentHosted) event);
        } else if (event instanceof BroadcastEvent.ParticipantJoined) {
            handleParticipantJoined((BroadcastEvent.ParticipantJoined) event);
        } else if (event instanceof BroadcastEvent.RoundStarting) {
            handleRoundStarting((BroadcastEvent.RoundStarting) event);
        } else if (event instanceof BroadcastEvent.ParticipantEliminated) {
            handleParticipantEliminated((BroadcastEvent.ParticipantEliminated) event);
        }
    }

    @Override
    public void sendTargetedMessage(Tournament tournament, Iterable<TournamentParticipant> participants, String message) {
        String translated = CC.translate(message);
        for (TournamentParticipant participant : participants) {
            for (Player player : participant.getOnlinePlayers()) {
                player.sendMessage(translated);
            }
        }
    }

    @Override
    public void broadcastResults(Tournament tournament, TournamentPlacements placements) {
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(CC.translate("&6&lTournament &7(" + tournament.getDisplayName() + ")"));
        Bukkit.broadcastMessage(
                CC.translate(
                        " &6│ &f"
                                + tournament.getKit().getDisplayName()
                                + " &7(&6"
                                + tournament.getInitialPlayerCount()
                                + "&7)"));

        String[] prefixes = {"&6&l✫1", "&7&l✫2", "&c&l✫3"};

        int limit = Math.min(3, placements.getOrderedPlacements().size());
        for (int i = 0; i < limit; i++) {
            TournamentParticipant participant = placements.getPlacement(i + 1);
            if (participant != null) {
                String teamNameString = TournamentMessageBuilder.getNaturalTeamNameListWithProfileColors(participant);
                Bukkit.broadcastMessage(CC.translate("   " + prefixes[i] + " " + teamNameString));
            }
        }
        Bukkit.broadcastMessage("");

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0f, 1.0f);
        }
    }

    /**
     * Handles global broadcast for a newly hosted tournament.
     * 处理新举办锦标赛的全局广播。
     *
     * @param event The tournament hosted event.
     *              锦标赛举办事件。
     */
    private void handleTournamentHosted(BroadcastEvent.TournamentHosted event) {
        Tournament tournament = event.getTournament();
        String message =
                CC.translate(
                        "&6"
                                + tournament.getHostName()
                                + " &fhosted a &6"
                                + tournament.getDisplayName()
                                + " "
                                + tournament.getKit().getDisplayName()
                                + " &ftournament! &7(/tournament join "
                                + tournament.getNumericId()
                                + ")");
        Bukkit.broadcastMessage(message);
    }

    /**
     * Handles waiting-pool broadcast when a participant joins.
     * 处理当参与者加入时的等待池广播。
     *
     * @param event The participant joined event.
     *              参与者加入事件。
     */
    private void handleParticipantJoined(BroadcastEvent.ParticipantJoined event) {
        String joinMessage = TournamentMessageBuilder.generateParticipantBroadcast(
                event.getParticipant(), "has joined", "have joined");

        sendTargetedMessage(event.getTournament(), event.getTournament().getWaitingPool(), joinMessage);

        int currentPlayers = event.getTournament().getWaitingPool().stream()
                .mapToInt(TournamentParticipant::getSize)
                .sum();
        int maxPlayers = event.getTournament().getMaxTeams() * event.getTournament().getTeamSize();
        if (currentPlayers >= maxPlayers) {
            sendTargetedMessage(
                    event.getTournament(),
                    event.getTournament().getWaitingPool(),
                    "&aThe tournament is now full and will begin shortly!");
        }
    }

    /**
     * Handles per-round broadcast when a round starts.
     * 处理每轮开始时的广播。
     *
     * @param event The round starting event.
     *              回合开始事件。
     */
    private void handleRoundStarting(BroadcastEvent.RoundStarting event) {
        String message = "&6Round " + event.getRoundNumber() + " &fhas started!";
        sendTargetedMessage(event.getTournament(), event.getTournament().getParticipants(), message);

        event.getTournament().getAllPlayers()
                .forEach(player -> player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f));
    }

    /**
     * Handles broadcast when a participant is eliminated.
     * 处理参与者被淘汰时的广播。
     *
     * @param event The eliminated event.
     *              淘汰事件。
     */
    private void handleParticipantEliminated(BroadcastEvent.ParticipantEliminated event) {
        Tournament tournament = event.getTournament();
        TournamentParticipant loser = event.getEliminated();
        TournamentParticipant winner = event.getWinner();

        String loserNames = TournamentMessageBuilder.getNaturalTeamNameListWithProfileColors(loser);
        String verb = loser.getSize() > 1 ? "were" : "was";

        int remainingPlayers = tournament.getRoundParticipants().stream().mapToInt(TournamentParticipant::getSize).sum();
        int initial = tournament.getInitialPlayerCount();

        String message;
        if (winner != null) {
            String winnerNames = TournamentMessageBuilder.getNaturalTeamNameListWithProfileColors(winner);
            message = String.format(
                    "&c%s &f%s eliminated by %s&f. (&6%d&f/&6%d&f)",
                    loserNames, verb, winnerNames, remainingPlayers, initial
            );
        } else {
            message = String.format(
                    "&c%s &f%s eliminated. (&6%d&f/&6%d&f)",
                    loserNames, verb, remainingPlayers, initial
            );
        }

        sendTournamentMessage(tournament, message);
    }

    private void sendTournamentMessage(Tournament tournament, String message) {
        String translated = CC.translate(message);
        tournament.getAllPlayers().forEach(player -> player.sendMessage(translated));
    }
}