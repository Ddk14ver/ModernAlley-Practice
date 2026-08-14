package dev.revere.alley.feature.tournament.execution.internal;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.bootstrap.annotation.Service;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.feature.tournament.model.TournamentParticipant;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import dev.revere.alley.feature.tournament.broadcast.BroadcastEvent;
import dev.revere.alley.feature.tournament.broadcast.TournamentBroadcaster;
import dev.revere.alley.feature.tournament.broadcast.TournamentPlacements;
import dev.revere.alley.feature.tournament.engine.TournamentEvent;
import dev.revere.alley.feature.tournament.execution.ExecutionResult;
import dev.revere.alley.feature.tournament.execution.TournamentExecutionStrategy;
import dev.revere.alley.feature.tournament.formation.TournamentTeamFormationService;
import dev.revere.alley.feature.tournament.internal.helpers.TournamentMessageBuilder;
import dev.revere.alley.feature.tournament.match.MatchOrchestrationResult;
import dev.revere.alley.feature.tournament.match.MatchOrchestrator;
import dev.revere.alley.feature.tournament.model.Tournament;
import dev.revere.alley.feature.tournament.model.TournamentParticipant;
import dev.revere.alley.feature.tournament.model.TournamentState;
import dev.revere.alley.feature.tournament.participant.ParticipantRegistry;
import dev.revere.alley.feature.tournament.participant.ParticipantStatus;
import dev.revere.alley.feature.tournament.player.PlayerTournamentStateService;
import dev.revere.alley.feature.tournament.state.TournamentStateManager;
import dev.revere.alley.feature.tournament.task.TournamentCountdownService;
import dev.revere.alley.feature.tournament.task.TournamentRoundStartTask;
import dev.revere.alley.feature.tournament.task.TournamentStartTask;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Remi
 * @project alley-practice
 * @date 8/08/2025
 */
@Service(provides = TournamentExecutionStrategy.class, priority = 1000)
public class StandardExecutionStrategy implements TournamentExecutionStrategy {
    private final TournamentStateManager stateManager;
    private final ParticipantRegistry registry;
    private final MatchOrchestrator orchestrator;
    private final TournamentBroadcaster broadcaster;
    private final TournamentTeamFormationService formation;
    private final PlayerTournamentStateService playerState;

    public StandardExecutionStrategy(
            TournamentStateManager stateManager,
            ParticipantRegistry registry,
            MatchOrchestrator orchestrator,
            TournamentBroadcaster broadcaster,
            TournamentTeamFormationService formation,
            PlayerTournamentStateService playerState) {
        this.stateManager = stateManager;
        this.registry = registry;
        this.orchestrator = orchestrator;
        this.broadcaster = broadcaster;
        this.formation = formation;
        this.playerState = playerState;
    }

    @Override
    public ExecutionResult handleEvent(Tournament tournament, TournamentEvent event) {
        if (tournament.getState() == TournamentState.ENDED) {
            return ExecutionResult.complete(tournament, "Already ended");
        }

        if (event instanceof TournamentEvent.PlayerJoinRequest) {
            return handlePlayerJoin(tournament, (TournamentEvent.PlayerJoinRequest) event);
        }
        if (event instanceof TournamentEvent.PlayerDeparture) {
            return handlePlayerDeparture(tournament, (TournamentEvent.PlayerDeparture) event);
        }
        if (event instanceof TournamentEvent.MatchCompletion) {
            return handleMatchCompletion(tournament, (TournamentEvent.MatchCompletion) event);
        }
        if (event instanceof TournamentEvent.ForceStart) {
            return handleForceStart(tournament);
        }
        if (event instanceof TournamentEvent.AdminCancellation) {
            return handleCancellation(tournament, (TournamentEvent.AdminCancellation) event);
        }
        if (event instanceof TournamentEvent.StartCountdownFinished) {
            return beginTournament(tournament);
        }
        if (event instanceof TournamentEvent.RoundCountdownFinished) {
            return startNextRound(tournament);
        }
        return ExecutionResult.continueExecution(tournament);
    }

    /**
     * Registers a joiner, broadcasts, and auto-starts if full.
     * 注册加入者，进行广播，若满员则自动开始。
     *
     * @param tournament The tournament.
     *                   锦标赛。
     * @param event      Join event.
     *                   加入事件。
     * @return The execution result.
     *         执行结果。
     */
    private ExecutionResult handlePlayerJoin(Tournament tournament, TournamentEvent.PlayerJoinRequest event) {
        TournamentState state = tournament.getState();
        if (state != TournamentState.WAITING && state != TournamentState.STARTING) {
            return ExecutionResult.continueExecution(tournament);
        }

        TournamentParticipant participant = registry.registerParticipant(event.getPlayer(), tournament);
        if (participant == null) {
            return ExecutionResult.continueExecution(tournament);
        }

        // Mark tournament participation for all members
        dev.revere.alley.core.profile.ProfileService ps = AlleyPlugin.getInstance()
                .getService(dev.revere.alley.core.profile.ProfileService.class);
        collectParticipantUUIDs(participant).forEach(uuid -> {
            dev.revere.alley.core.profile.Profile p = ps.getProfile(uuid);
            if (p != null) p.getProfileData().setTournamentParticipated(true);
        });

        tournament.addToWaitingPool(participant);
        broadcaster.broadcast(new BroadcastEvent.ParticipantJoined(tournament, participant));

        int teamSize = Math.max(1, tournament.getTeamSize());
        int maxPlayers = tournament.getMaxTeams() * teamSize;
        int currentPlayers = tournament.getWaitingPool().stream()
                .mapToInt(TournamentParticipant::getSize)
                .sum();
        int currentTeams = currentPlayers / teamSize;

        boolean meetsMinTeams = currentTeams >= tournament.getMinTeams();
        boolean isFull = currentPlayers >= maxPlayers;
        int oneThirdThreshold = (int) Math.ceil(maxPlayers / 3.0);
        boolean meetsOneThird = currentPlayers >= oneThirdThreshold;

        if (state == TournamentState.WAITING && meetsMinTeams && (isFull || meetsOneThird)) {
            return initiateStart(tournament);
        }

        return ExecutionResult.continueExecution(tournament);
    }

    /**
     * Resets leaver’s state, eliminates empty teams, and ends if only one left.
     * 重置离开者的状态，淘汰空队伍，若仅剩一支队伍则结束。
     *
     * @param tournament The tournament.
     *                   锦标赛。
     * @param event      Departure event.
     *                   离开事件。
     * @return The execution result.
     *         执行结果。
     */
    private ExecutionResult handlePlayerDeparture(Tournament tournament, TournamentEvent.PlayerDeparture event) {
        TournamentParticipant participant = registry.findParticipantByPlayer(event.getPlayer(), tournament);
        if (participant == null) return ExecutionResult.continueExecution(tournament);

        playerState.resetPlayerStateToLobby(event.getPlayer());

        if (tournament.getState() == TournamentState.WAITING || tournament.getState() == TournamentState.STARTING) {
            registry.unregisterParticipant(participant, tournament);

            String names = TournamentMessageBuilder.getNaturalTeamNameListWithProfileColors(participant);
            if (names == null || names.trim().isEmpty() || names.contains("unknown")) {
                names = participant.getLeaderName() != null
                        ? "&e" + participant.getLeaderName()
                        : "&eA team";
            }
            String verb = participant.getSize() > 1 ? "have left" : "has left";
            String message = "&e" + names + " &f" + verb + "!";

            tournament.removeParticipant(participant);
            broadcaster.sendTargetedMessage(tournament, tournament.getWaitingPool(), message);

            return ExecutionResult.continueExecution(tournament);
        }

        participant.removeMember(event.getPlayer().getUniqueId());

        if (participant.isEmpty()) {
            registry.updateParticipantStatus(participant, ParticipantStatus.ELIMINATED);
            tournament.getPlacementList().add(0, participant);

            int placement = tournament.getPlacementList().size();

            broadcaster.broadcast(new BroadcastEvent.ParticipantEliminated(tournament, participant, null, placement));
            registry.unregisterParticipant(participant, tournament);

            if (shouldEndTournament(tournament)) {
                return endTournament(tournament);
            }
        }

        return ExecutionResult.continueExecution(tournament);
    }

    /**
     * Processes match completion and advances if round finished.
     * 处理比赛完成，若该轮结束则推进赛程。
     *
     * @param tournament The tournament.
     *                   锦标赛。
     * @param event      Match completion event.
     *                   比赛完成事件。
     * @return The execution result.
     *         执行结果。
     */
    private ExecutionResult handleMatchCompletion(Tournament tournament, TournamentEvent.MatchCompletion event) {
        orchestrator.processMatchResult(event.getMatch(), tournament);
        if (orchestrator.isRoundComplete(tournament)) {
            return processRoundCompletion(tournament);
        }

        return ExecutionResult.continueExecution(tournament);
    }

    /**
     * Force-start handler if minimum teams satisfied.
     * 强制开始处理：若满足最小队伍数则强制开始。
     *
     * @param tournament The tournament.
     *                   锦标赛。
     * @return The execution result.
     *         执行结果。
     */
    private ExecutionResult handleForceStart(Tournament tournament) {
        if (tournament.getState() != TournamentState.WAITING) {
            return ExecutionResult.continueExecution(tournament);
        }

        int currentTeams = tournament.getWaitingPool().size();
        if (currentTeams >= tournament.getMinTeams()) {
            return initiateStart(tournament);
        }

        return ExecutionResult.continueExecution(tournament);
    }

    /**
     * Cancels tournament, resets everyone, broadcasts cancel, ends.
     * 取消锦标赛，重置所有玩家，广播取消消息，结束。
     *
     * @param tournament The tournament.
     *                   锦标赛。
     * @param event      Admin cancel event.
     *                   管理员取消事件。
     * @return The execution result.
     *         执行结果。
     */
    private ExecutionResult handleCancellation(Tournament tournament, TournamentEvent.AdminCancellation event) {
        String message = "&cThe tournament has been cancelled! &7(" + event.getReason() + ")";

        String translated = CC.translate(message);
        tournament.getAllPlayers().forEach(p -> p.sendMessage(translated));
        tournament.getAllPlayers().forEach(playerState::resetPlayerStateToLobby);

        Tournament ended = stateManager.transitionState(tournament, TournamentState.ENDED);

        return ExecutionResult.complete(ended, "Tournament cancelled");
    }

    /**
     * Transitions to STARTING and runs start countdown.
     * 转换至 STARTING 状态并运行开始倒计时。
     *
     * @param tournament The tournament.
     *                   锦标赛。
     * @return The execution result.
     *         执行结果。
     */
    private ExecutionResult initiateStart(Tournament tournament) {
        Tournament starting = stateManager.transitionState(tournament, TournamentState.STARTING);

        if (starting.getInactivityTask() != null) {
            starting.getInactivityTask().cancel();
            starting.setInactivityTask(null);
        }

        TournamentStartTask startTask = new TournamentStartTask(starting);
        AlleyPlugin.getInstance()
                .getService(TournamentCountdownService.class)
                .setStartTask(startTask);

        starting.setStartingTask(Bukkit.getScheduler().runTaskTimer(AlleyPlugin.getInstance(), startTask, 0L, 20L));

        return ExecutionResult.transitionState(starting, "Tournament starting");
    }

    /**
     * Forms teams, sets counts, broadcasts round 1, creates matches.
     * 组建队伍，设置计数，广播第1轮，创建比赛。
     *
     * @param tournament The tournament.
     *                   锦标赛。
     * @return The execution result.
     *         执行结果。
     */
    private ExecutionResult beginTournament(Tournament tournament) {
        Tournament inProgress = stateManager.transitionState(tournament, TournamentState.IN_PROGRESS);
        inProgress.setCurrentRound(1);

        List<TournamentParticipant> teams = formation.formTeamsForTournament(inProgress);
        teams.forEach(inProgress::addFinalizedParticipant);

        inProgress.setInitialPlayerCount(inProgress.getParticipants().stream().mapToInt(TournamentParticipant::getSize).sum());

        broadcaster.broadcast(new BroadcastEvent.RoundStarting(inProgress, 1));

        MatchOrchestrationResult result =
                orchestrator.createRoundMatches(inProgress, inProgress.getRoundParticipants());

        if (result.isSuccessful()) {
            return ExecutionResult.transitionState(inProgress, "Tournament begun");
        }

        return ExecutionResult.complete(inProgress, "Failed to create matches");
    }

    /**
     * Goes to INTERMISSION, increments round, launches round countdown.
     * 进入 INTERMISSION 状态，递增轮次，启动轮间倒计时。
     *
     * @param tournament The tournament.
     *                   锦标赛。
     * @return The execution result.
     *         执行结果。
     */
    private ExecutionResult processRoundCompletion(Tournament tournament) {
        if (tournament.getRoundStartTask() != null || tournament.getState() == TournamentState.INTERMISSION) {
            return ExecutionResult.continueExecution(tournament);
        }

        if (tournament.getRoundParticipants().size() <= 1) {
            return endTournament(tournament);
        }

        Tournament intermission = stateManager.transitionState(tournament, TournamentState.INTERMISSION);
        intermission.setCurrentRound(intermission.getCurrentRound() + 1);

        TournamentRoundStartTask roundTask = new TournamentRoundStartTask(intermission);
        AlleyPlugin.getInstance()
                .getService(TournamentCountdownService.class)
                .setRoundStartTask(roundTask);

        intermission.setRoundStartTask(
                Bukkit.getScheduler().runTaskTimer(AlleyPlugin.getInstance(), roundTask, 0L, 20L)
        );

        return ExecutionResult.transitionState(intermission, "Round completed");
    }

    /**
     * Transitions to IN_PROGRESS and creates next round matches.
     * 转换至 IN_PROGRESS 状态并创建下一轮比赛。
     *
     * @param tournament The tournament.
     *                   锦标赛。
     * @return The execution result.
     *         执行结果。
     */
    private ExecutionResult startNextRound(Tournament tournament) {
        Tournament inProgress = stateManager.transitionState(tournament, TournamentState.IN_PROGRESS);

        List<TournamentParticipant> pool = new ArrayList<>(inProgress.getRoundParticipants());

        broadcaster.broadcast(new BroadcastEvent.RoundStarting(inProgress, inProgress.getCurrentRound()));

        MatchOrchestrationResult result = orchestrator.createRoundMatches(inProgress, pool);
        if (result.isSuccessful()) {
            return ExecutionResult.transitionState(inProgress, "Next round started");
        }

        return ExecutionResult.complete(inProgress, "Failed to create round matches");
    }

    /**
     * Ends the tournament, broadcasts results, resets winner to lobby.
     * 结束锦标赛，广播结果，将获胜者重置回大厅。
     *
     * @param tournament The tournament.
     *                   锦标赛。
     * @return The execution result.
     *         执行结果。
     */
    private ExecutionResult endTournament(Tournament tournament) {
        Tournament ended = stateManager.transitionState(tournament, TournamentState.ENDED);

        List<TournamentParticipant> active = registry.getParticipantsByStatus(ended, ParticipantStatus.ACTIVE);

        if (!active.isEmpty()) {
            TournamentParticipant winner = active.get(0);
            registry.updateParticipantStatus(winner, ParticipantStatus.WINNER);

            // Record tournament stats for all participants
            dev.revere.alley.core.profile.ProfileService profileService =
                    AlleyPlugin.getInstance().getService(dev.revere.alley.core.profile.ProfileService.class);
            collectParticipantUUIDs(winner).forEach(uuid -> {
                dev.revere.alley.core.profile.Profile p = profileService.getProfile(uuid);
                if (p != null) {
                    p.getProfileData().setTournamentWins(p.getProfileData().getTournamentWins() + 1);
                    // Count the tournament win toward the kit's division progression.
                    // 将锦标赛胜场计入该套件的段位进度。
                    p.getProfileData().incrementTournamentKitWins(tournament.getKit().getName());
                }
            });
            for (TournamentParticipant eliminated : ended.getPlacementList()) {
                collectParticipantUUIDs(eliminated).forEach(uuid -> {
                    dev.revere.alley.core.profile.Profile p = profileService.getProfile(uuid);
                    if (p != null) p.getProfileData().setTournamentLosses(p.getProfileData().getTournamentLosses() + 1);
                });
            }

            TournamentPlacements placements = createFinalPlacements(ended, winner);
            broadcaster.broadcastResults(ended, placements);

            winner.getOnlinePlayers().forEach(playerState::resetPlayerStateToLobby);
        }

        cleanupTasks(ended);
        return ExecutionResult.complete(ended, "Tournament ended");
    }

    /**
     * Checks if the tournament should end due to insufficient teams.
     * 检查锦标赛是否因队伍不足而应当结束。
     *
     * @param tournament The tournament to check.
     *                   要检查的锦标赛。
     * @return True if ending condition reached.
     *         若满足结束条件则返回 true。
     */
    private boolean shouldEndTournament(Tournament tournament) {
        if (tournament.getState() == TournamentState.WAITING) {
            return false;
        }
        return tournament.getRoundParticipants().size() <= 1;
    }

    /**
     * Builds placements object with winner followed by prior placements.
     * 构建名次对象，获胜者在前，后续为之前的排名列表。
     *
     * @param tournament The tournament.
     *                   锦标赛。
     * @param winner     The winner.
     *                   获胜者。
     * @return Placements structure.
     *         名次结构。
     */
    private TournamentPlacements createFinalPlacements(Tournament tournament, TournamentParticipant winner) {
        ArrayList<TournamentParticipant> list = new ArrayList<>();
        list.add(winner);
        list.addAll(tournament.getPlacementList());
        return new TournamentPlacements(list, tournament.getInitialPlayerCount());
    }

    /**
     * Cancels and clears all scheduled tasks on the tournament.
     * 取消并清除锦标赛上所有已调度的任务。
     *
     * @param tournament The tournament to clean up.
     *                   要清理的锦标赛。
     */
    private void cleanupTasks(Tournament tournament) {
        if (tournament.getStartingTask() != null) {
            tournament.getStartingTask().cancel();
            tournament.setStartingTask(null);
        }
        if (tournament.getInactivityTask() != null) {
            tournament.getInactivityTask().cancel();
            tournament.setInactivityTask(null);
        }
        if (tournament.getRoundStartTask() != null) {
            tournament.getRoundStartTask().cancel();
            tournament.setRoundStartTask(null);
        }
    }

    /** Collects all UUIDs for a participant: leader + members. */
    private static List<UUID> collectParticipantUUIDs(TournamentParticipant participant) {
        List<UUID> uuids = new ArrayList<>();
        if (participant.getLeaderUuid() != null) uuids.add(participant.getLeaderUuid());
        uuids.addAll(participant.getMemberUuids());
        return uuids;
    }
}