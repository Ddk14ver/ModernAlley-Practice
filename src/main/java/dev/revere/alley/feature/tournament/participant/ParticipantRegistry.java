package dev.revere.alley.feature.tournament.participant;

import dev.revere.alley.bootstrap.lifecycle.Service;
import dev.revere.alley.feature.tournament.model.Tournament;
import dev.revere.alley.feature.tournament.model.TournamentParticipant;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * @author Remi
 * @project alley-practice
 * @date 8/08/2025
 */
public interface ParticipantRegistry extends Service {
    /**
     * Finds the participant containing a specific player.
     * 查找包含特定玩家的参赛者。
     *
     * @param player     The player to search for.
     *                   要搜索的玩家。
     * @param tournament The tournament to search in.
     *                   要在其中搜索的锦标赛。
     * @return The participant or null if not found.
     *         找到的参赛者，如果未找到则返回null。
     */
    TournamentParticipant findParticipantByPlayer(Player player, Tournament tournament);

    /**
     * Creates and registers a new participant from a player (party-aware) if
     * validation passes.
     * 如果验证通过，从玩家创建并注册一个新的参赛者（感知队伍）。
     *
     * @param player     The player joining.
     *                   正在加入的玩家。
     * @param tournament The tournament being joined.
     *                   正在加入的锦标赛。
     * @return The created participant or null if joining failed.
     *         创建的参赛者，如果加入失败则返回null。
     */
    TournamentParticipant registerParticipant(Player player, Tournament tournament);

    /**
     * Removes a participant from the tournament registry and resets member
     * states as needed.
     * 从锦标赛注册表中移除参赛者，并根据需要重置成员状态。
     *
     * @param participant The participant to remove.
     *                    要移除的参赛者。
     * @param tournament  The tournament they're leaving.
     *                    他们正在离开的锦标赛。
     */
    void unregisterParticipant(TournamentParticipant participant, Tournament tournament);

    /**
     * Purges all participants from the tournament, resetting their states.
     * 清除锦标赛中的所有参赛者，重置他们的状态。
     *
     * @param tournament The tournament to purge.
     *                   要清除的锦标赛。
     */
    void purgeTournament(Tournament tournament);

    /**
     * Updates a participant's status in the tournament.
     * 更新锦标赛中参赛者的状态。
     *
     * @param participant The participant to update.
     *                    要更新的参赛者。
     * @param status      The new status.
     *                    新状态。
     */
    void updateParticipantStatus(TournamentParticipant participant, ParticipantStatus status);

    /**
     * Gets all participants with a specific status.
     * 获取具有特定状态的所有参赛者。
     *
     * @param tournament The tournament to search.
     *                   要搜索的锦标赛。
     * @param status     The status to filter by.
     *                   用于筛选的状态。
     * @return List of matching participants.
     *         匹配的参赛者列表。
     */
    List<TournamentParticipant> getParticipantsByStatus(
            Tournament tournament, ParticipantStatus status);
}