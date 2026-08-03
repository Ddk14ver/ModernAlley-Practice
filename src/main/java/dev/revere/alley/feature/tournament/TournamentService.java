package dev.revere.alley.feature.tournament;

import dev.revere.alley.bootstrap.lifecycle.Service;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.match.Match;
import dev.revere.alley.feature.tournament.model.Tournament;
import dev.revere.alley.feature.tournament.model.TournamentType;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

/**
 * @author Remi
 * @project alley-practice
 * @date 6/08/2025
 */
public interface TournamentService extends Service {
    /**
     * Hosts a new tournament, broadcasting its availability to all players.
     * 举办一场新的锦标赛，向所有玩家广播其可用性。
     * This method applies a cooldown to prevent rapid tournament hosting.
     * 此方法会施加冷却时间，以防止快速重复举办锦标赛。
     *
     * @param host The player hosting the tournament.
     *             举办锦标赛的玩家。
     * @param type The type of tournament (e.g., SOLO, DUO).
     *             锦标赛类型（例如：SOLO、DUO）。
     * @param kit  The kit to be used in the tournament matches.
     *             锦标赛比赛中所使用的职业套装。
     */
    void hostTournament(Player host, TournamentType type, Kit kit);

    /**
     * Allows an admin to host a new tournament without restrictions.
     * 允许管理员无限制地举办一场新的锦标赛。
     * This method bypasses the hosting cooldown and uses custom team size and
     * 此方法绕过举办冷却时间，并使用自定义的
     * max teams.
     * 团队大小和最大团队数量。
     *
     * @param host     The admin player hosting the tournament.
     *                 举办锦标赛的管理员玩家。
     * @param kit      The kit to be used in the tournament matches.
     *                 锦标赛比赛中所使用的职业套装。
     * @param teamSize The number of players per team.
     *                 每支队伍的玩家数量。
     * @param maxTeams The maximum number of teams.
     *                 最大队伍数量。
     */
    void adminHostTournament(Player host, Kit kit, int teamSize, int maxTeams);

    /**
     * Adds a player or their party to the tournament's waiting pool.
     * 将玩家或其队伍添加到锦标赛的等待池中。
     *
     * @param player     The player attempting to join.
     *                   尝试加入的玩家。
     * @param tournament The tournament to join.
     *                   要加入的锦标赛。
     */
    void joinTournament(Player player, Tournament tournament);

    /**
     * Handles a player leaving the tournament, either during the waiting phase
     * 处理玩家离开锦标赛的情况，无论是在等待阶段
     * or mid-match.
     * 还是比赛中途。
     *
     * @param player The player who is departing.
     *               正在离开的玩家。
     */
    void handlePlayerDeparture(Player player);

    /**
     * Processes the end of a tournament match, determining the winner and loser
     * 处理锦标赛比赛的结束，判定胜者和败者，
     * and updating the tournament's state accordingly.
     * 并相应更新锦标赛的状态。
     *
     * @param match The match that has just ended.
     *              刚刚结束的比赛。
     */
    void handleMatchEnd(Match match);

    /**
     * Forcibly starts a tournament if enough players have joined, bypassing the
     * 如果有足够的玩家加入，则强制开始锦标赛，绕过
     * need for a full lobby.
     * 需要满员大厅的要求。
     *
     * @param tournament The tournament to force start.
     *                   要强制开始的锦标赛。
     */
    void forceStartTournament(Tournament tournament);

    /**
     * Immediately cancels a tournament, notifying all participants and cleaning
     * 立即取消锦标赛，通知所有参与者并清理
     * up resources.
     * 相关资源。
     *
     * @param tournament The tournament to cancel.
     *                   要取消的锦标赛。
     * @param reason     The reason for the cancellation.
     *                   取消的原因。
     */
    void cancelTournament(Tournament tournament, String reason);

    /**
     * Retrieves an active tournament by its unique ID.
     * 通过其唯一 ID 检索活跃的锦标赛。
     *
     * @param tournamentId The UUID of the tournament.
     *                     锦标赛的 UUID。
     * @return The Tournament object, or {@code null} if not found.
     *         Tournament 对象，如果未找到则返回 {@code null}。
     */
    Tournament getTournament(UUID tournamentId);

    /**
     * Gets the tournament a specific player is currently participating in.
     * 获取指定玩家当前正在参与的锦标赛。
     *
     * @param player The player to check.
     *               要检查的玩家。
     * @return The Tournament object, or {@code null} if the player is not in one.
     *         Tournament 对象，如果玩家不在任何锦标赛中则返回 {@code null}。
     */
    Tournament getPlayerTournament(Player player);

    /**
     * Retrieves a list of all currently active tournaments.
     * 检索所有当前活跃锦标赛的列表。
     *
     * @return A list of all active Tournament objects.
     *         所有活跃 Tournament 对象的列表。
     */
    List<Tournament> getTournaments();
}