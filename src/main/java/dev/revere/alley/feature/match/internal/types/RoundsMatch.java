package dev.revere.alley.feature.match.internal.types;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.ListenerUtil;
import dev.revere.alley.common.PlayerUtil;
import dev.revere.alley.core.locale.LocaleService;
import dev.revere.alley.core.locale.internal.impl.VisualsLocaleImpl;
import dev.revere.alley.core.locale.internal.impl.message.GameMessagesLocaleImpl;
import dev.revere.alley.feature.arena.Arena;
import dev.revere.alley.feature.combat.CombatService;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.kit.setting.types.mode.KitSettingBridges;
import dev.revere.alley.feature.kit.setting.types.mode.KitSettingStickFight;
import dev.revere.alley.feature.match.MatchState;
import dev.revere.alley.feature.match.model.GameParticipant;
import dev.revere.alley.feature.match.model.TeamGameParticipant;
import dev.revere.alley.feature.match.model.internal.MatchGamePlayer;
import dev.revere.alley.feature.queue.Queue;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * @author Emmy
 * @project Alley
 * @since 08/02/2025
 */
@Getter
public class RoundsMatch extends DefaultMatch {
    private GameParticipant<MatchGamePlayer> winner;
    private GameParticipant<MatchGamePlayer> loser;

    private final int rounds;
    private int currentRound;

    @Setter
    private String scorer;
    private Player fallenPlayer;

    /**
     * Constructor for the MatchRoundsImpl class.
     * MatchRoundsImpl类的构造函数。
     *
     * @param queue        The queue of the match.
     *                     比赛的队列。
     * @param kit          The kit of the match.
     *                     比赛的工具包。
     * @param arena        The arena of the match.
     *                     比赛的竞技场。
     * @param ranked       Whether the match is ranked or not.
     *                     比赛是否为排位赛。
     * @param participantA The first participant.
     *                     第一个参赛方。
     * @param participantB The second participant.
     *                     第二个参赛方。
     * @param rounds       The amount of rounds the match will have.
     *                     比赛的回合总数。
     */
    public RoundsMatch(Queue queue, Kit kit, Arena arena, boolean ranked, GameParticipant<MatchGamePlayer> participantA, GameParticipant<MatchGamePlayer> participantB, int rounds) {
        super(queue, kit, arena, ranked, participantA, participantB);
        this.rounds = rounds;
        this.scorer = "Unknown";

        if (this.currentRound == 0) {
            this.currentRound = 1;
        }
    }

    @Override
    public void handleRoundEnd() {
        this.winner = this.getParticipantA().isAllDead() ? this.getParticipantB() : this.getParticipantA();
        this.winner.getLeader().getData().incrementScore();
        this.loser = this.getParticipantA().isAllDead() ? this.getParticipantA() : this.getParticipantB();

        this.currentRound++;

        this.broadcastPlayerScoreMessage(this.winner, this.loser, this.scorer);

        if (this.getKit().isSettingEnabled(KitSettingStickFight.class)) {
            if (this.canEndMatch()) {
                this.removePlacedBlocks();
                this.setEndTime(System.currentTimeMillis());
                this.setState(MatchState.ENDING_MATCH);
                this.getRunnable().setStage(4);
                super.handleRoundEnd();
            } else {
                this.removePlacedBlocks();
                this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> handleRespawn(this.fallenPlayer), 1L);
                this.setState(MatchState.ENDING_ROUND);

                this.getParticipants().forEach(participant -> participant.getPlayers().forEach(playerParticipant -> {
                    Player player1 = playerParticipant.getTeamPlayer();
                    player1.setVelocity(new Vector(0, 0, 0));
                    playerParticipant.setDead(false);

                    super.setupPlayer(player1);
                }));
            }
        } else {
            if (this.canEndMatch()) {
                super.handleRoundEnd();
            } else {
                if (!getKit().isSettingEnabled(KitSettingBridges.class)) {
                    this.removePlacedBlocks();
                }
                this.setState(MatchState.ENDING_ROUND);

                this.getParticipants().forEach(participant -> participant.getPlayers().forEach(playerParticipant -> {
                    Player player = playerParticipant.getTeamPlayer();
                    if (player == null) return;
                    player.setVelocity(new Vector(0, 0, 0));
                    playerParticipant.setDead(false);
                    playerParticipant.setEliminated(false);

                    // Respawn on next tick to avoid conflict with death processing chain
                    this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> handleRespawn(player), 1L);
                }));
            }
        }
    }

    @Override
    public void handleDeath(Player player, EntityDamageEvent.DamageCause cause) {
        GameParticipant<MatchGamePlayer> participant = this.getParticipantA().containsPlayer(player.getUniqueId())
                ? this.getParticipantA()
                : this.getParticipantB();
        participant.getLeader().getData().incrementDeaths();

        this.fallenPlayer = player;

        if (this.getKit().isSettingEnabled(KitSettingStickFight.class)) {
            Player lastAttacker = AlleyPlugin.getInstance().getService(CombatService.class).getLastAttacker(player);
            if (lastAttacker == null) {
                GameParticipant<MatchGamePlayer> opponent = this.getParticipantA().containsPlayer(player.getUniqueId())
                        ? this.getParticipantB()
                        : this.getParticipantA();

                this.setScorer(opponent.getLeader().getUsername());
            } else {
                this.setScorer(lastAttacker.getName());
            }

            if (this.getParticipantA().containsPlayer(player.getUniqueId())) {
                participant = this.getParticipantA();
            } else {
                participant = this.getParticipantB();
            }

            if (participant instanceof TeamGameParticipant<?>) {
                TeamGameParticipant<MatchGamePlayer> team = (TeamGameParticipant<MatchGamePlayer>) participant;
                MatchGamePlayer gamePlayer = team.getPlayers().stream()
                        .filter(gamePlayer1 -> gamePlayer1.getUuid().equals(player.getUniqueId()))
                        .findFirst()
                        .orElse(null);

                if (gamePlayer != null) {
                    team.getPlayers().forEach(matchGamePlayer -> {
                        matchGamePlayer.getData().incrementDeaths();
                        matchGamePlayer.setDead(true);
                    });
                    this.handleRoundEnd();
                }
            } else {
                MatchGamePlayer gamePlayer = participant.getLeader();
                gamePlayer.getData().incrementDeaths();
                gamePlayer.setDead(true);
                this.handleRoundEnd();
            }
            return;
        }

        super.handleDeath(player, cause);
    }

    @Override
    public void handleParticipant(Player player, MatchGamePlayer gamePlayer) {
        super.handleParticipant(player, gamePlayer); // must call base to setDead(true)

        GameParticipant<MatchGamePlayer> participant = this.getParticipantA().containsPlayer(player.getUniqueId())
                ? this.getParticipantA()
                : this.getParticipantB();
        if (participant.getLeader().getData().getScore() == this.rounds) {
            GameParticipant<MatchGamePlayer> opponent = participant == this.getParticipantA() ? this.getParticipantB() : this.getParticipantA();
            opponent.getLeader().setEliminated(true);
        }
    }

    @Override
    public void handleRespawn(Player player) {
        player.spigot().respawn();
        PlayerUtil.reset(player, false, true);

        Location spawnLocation = getParticipants().get(0).containsPlayer(player.getUniqueId()) ? this.getArena().getPos1() : this.getArena().getPos2();
        ListenerUtil.teleportAndClearSpawn(player, spawnLocation);

        this.giveLoadout(player, this.getKit());
        this.applyColorKit(player);
    }

    @Override
    public boolean canStartRound() {
        return this.getParticipantA().getLeader().getData().getScore() < this.rounds && this.getParticipantB().getLeader().getData().getScore() < this.rounds;
    }

    @Override
    public boolean canEndRound() {
        return (this.getParticipantA().isAllDead() || this.getParticipantB().isAllDead())
                || (this.getParticipantA().getAllPlayers().stream().allMatch(MatchGamePlayer::isDisconnected)
                || this.getParticipantB().getAllPlayers().stream().allMatch(MatchGamePlayer::isDisconnected));
    }

    @Override
    public boolean canEndMatch() {
        return (this.getParticipantA().getLeader().getData().getScore() == this.rounds || this.getParticipantB().getLeader().getData().getScore() == this.rounds)
                || (this.getParticipantA().getAllPlayers().stream().allMatch(MatchGamePlayer::isDisconnected)
                || this.getParticipantB().getAllPlayers().stream().allMatch(MatchGamePlayer::isDisconnected));
    }

    /**
     * Broadcasts a message to all players in the match when a player scores.
     * 当有玩家得分时，向比赛中所有玩家广播消息。
     *
     * @param winner The player who scored.
     *               得分的玩家。
     * @param loser  The player who was scored on.
     *               被得分的玩家。
     * @param scorer The name of the player who scored.
     *               得分玩家的名称。
     */
    public void broadcastPlayerScoreMessage(GameParticipant<MatchGamePlayer> winner, GameParticipant<MatchGamePlayer> loser, String scorer) {
        LocaleService localeService = AlleyPlugin.getInstance().getService(LocaleService.class);

        boolean messageEnabled = localeService.getBoolean(GameMessagesLocaleImpl.MATCH_SCORED_MESSAGE_ENABLED_BOOLEAN);
        if (messageEnabled) {
            List<String> message;
            if (this.isTeamMatch()) {
                message = localeService.getStringList(GameMessagesLocaleImpl.MATCH_SCORED_MESSAGE_SOLO_FORMAT);
            } else {
                message = localeService.getStringList(GameMessagesLocaleImpl.MATCH_SCORED_MESSAGE_TEAM_FORMAT);
            }

            message.forEach(line -> this.notifyAll(line
                    .replace("{scorer}", scorer)
                    .replace("{winner}", winner.getLeader().getUsername())
                    .replace("{winner-color}", String.valueOf(this.getTeamColor(winner)))
                    .replace("{winner-goals}", String.valueOf(winner.getLeader().getData().getScore()))
                    .replace("{loser}", loser.getLeader().getUsername())
                    .replace("{loser-color}", String.valueOf(this.getTeamColor(loser)))
                    .replace("{loser-goals}", String.valueOf(loser.getLeader().getData().getScore()))
                    .replace("{current-score}", String.valueOf(winner.getLeader().getData().getScore()))
                    .replace("{max-rounds}", String.valueOf(this.rounds))
            ));
        }

        if (localeService.getBoolean(VisualsLocaleImpl.TITLE_TEAM_SCORED_ENABLED_BOOLEAN)) {
            String header = localeService.getString(VisualsLocaleImpl.TITLE_TEAM_SCORED_HEADER)
                    .replace("{loser-color}", String.valueOf(this.getTeamColor(loser)))
                    .replace("{winner-color}", String.valueOf(this.getTeamColor(winner)))
                    .replace("{scorer}", scorer)
                    .replace("{current-score}", String.valueOf(winner.getLeader().getData().getScore()))
                    .replace("{opponent-current-score}", String.valueOf(loser.getLeader().getData().getScore()))
                    .replace("{max-rounds}", String.valueOf(this.rounds));
            String footer = localeService.getString(VisualsLocaleImpl.TITLE_TEAM_SCORED_FOOTER)
                    .replace("{loser-color}", String.valueOf(this.getTeamColor(loser)))
                    .replace("{winner-color}", String.valueOf(this.getTeamColor(winner)))
                    .replace("{scorer}", scorer)
                    .replace("{current-score}", String.valueOf(winner.getLeader().getData().getScore()))
                    .replace("{opponent-current-score}", String.valueOf(loser.getLeader().getData().getScore()))
                    .replace("{max-rounds}", String.valueOf(this.rounds));
            int fadeIn = localeService.getInt(VisualsLocaleImpl.TITLE_TEAM_SCORED_FADE_IN);
            int stay = localeService.getInt(VisualsLocaleImpl.TITLE_TEAM_SCORED_STAY);
            int fadeOut = localeService.getInt(VisualsLocaleImpl.TITLE_TEAM_SCORED_FADEOUT);

            this.sendTitle(header, footer, fadeIn, stay, fadeOut, true);
        }
    }
}
