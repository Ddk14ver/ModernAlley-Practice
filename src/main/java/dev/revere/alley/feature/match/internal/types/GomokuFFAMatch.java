package dev.revere.alley.feature.match.internal.types;

import dev.revere.alley.feature.arena.Arena;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.match.MatchState;
import dev.revere.alley.feature.match.model.GameParticipant;
import dev.revere.alley.feature.match.model.internal.MatchGamePlayer;
import dev.revere.alley.feature.queue.Queue;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.List;
import java.util.UUID;

public class GomokuFFAMatch extends FFAMatch implements GomokuPlayable {
    private final GomokuGame game;

    public GomokuFFAMatch(Queue queue, Kit kit, Arena arena,
                          List<GameParticipant<MatchGamePlayer>> participants) {
        super(queue, kit, arena, participants);
        this.game = new GomokuGame(this, this::finishGame);
    }

    @Override
    public void setupPlayer(Player player) {
        super.setupPlayer(player);
        this.game.setupPlayer(player);
    }

    @Override
    public void handleRoundStart() {
        super.handleRoundStart();
        this.game.startFreeForAll();
    }

    @Override
    public void handleDisconnect(Player player) {
        boolean wasCurrentPlayer = this.game.isCurrentPlayer(player);
        super.handleDisconnect(player);
        if (wasCurrentPlayer) this.game.handleUnavailablePlayer(player);
    }

    private void finishGame(GameParticipant<MatchGamePlayer> winningParticipant) {
        getParticipants().stream()
                .filter(participant -> participant != winningParticipant)
                .flatMap(participant -> participant.getPlayers().stream())
                .forEach(gamePlayer -> {
                    gamePlayer.setDead(true);
                    gamePlayer.setEliminated(true);
                });

        Player winner = firstOnlinePlayer(winningParticipant);
        Player loser = getParticipants().stream()
                .filter(participant -> participant != winningParticipant)
                .map(this::firstOnlinePlayer)
                .filter(player -> player != null)
                .findFirst()
                .orElse(null);
        if (winner != null && loser != null) {
            checkForConclusion(loser, winner);
            return;
        }

        setState(MatchState.ENDING_ROUND);
        if (getRunnable() != null) getRunnable().setStage(4);
        handleRoundEnd();
        setState(MatchState.ENDING_MATCH);
    }

    private Player firstOnlinePlayer(GameParticipant<MatchGamePlayer> participant) {
        return participant.getPlayers().stream()
                .map(MatchGamePlayer::getTeamPlayer)
                .filter(player -> player != null && player.isOnline())
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean tryPlaceFromView(Player player) {
        return this.game.tryPlaceFromView(player);
    }

    @Override
    public void surrender(Player player) {
        MatchGamePlayer gamePlayer = getFromAllGamePlayers(player);
        if (getState() != MatchState.RUNNING || gamePlayer == null || gamePlayer.isDead()) return;

        handleDeath(player, EntityDamageEvent.DamageCause.CUSTOM);
        this.game.handleUnavailablePlayer(player);
    }

    @Override
    public boolean isCurrentPlayer(Player player) {
        return this.game.isCurrentPlayer(player);
    }

    @Override
    public String getCurrentPlayerName() {
        return this.game.getCurrentPlayerName();
    }

    @Override
    public String getCurrentColorName() {
        return this.game.getCurrentColorName();
    }

    @Override
    public String getPlayerColorName(UUID playerId) {
        return this.game.getPlayerColorName(playerId);
    }

    @Override
    public int getRemainingTurnSeconds() {
        return this.game.getRemainingTurnSeconds();
    }

    @Override
    public int getPlacedStones() {
        return this.game.getPlacedStones();
    }

    @Override
    public void cleanupGomoku() {
        this.game.shutdown();
    }

    @Override
    public void endMatch() {
        cleanupGomoku();
        super.endMatch();
    }
}
