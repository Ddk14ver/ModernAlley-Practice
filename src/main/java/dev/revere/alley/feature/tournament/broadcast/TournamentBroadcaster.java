package dev.revere.alley.feature.tournament.broadcast;

import dev.revere.alley.bootstrap.lifecycle.Service;
import dev.revere.alley.feature.tournament.model.Tournament;
import dev.revere.alley.feature.tournament.model.TournamentParticipant;

/**
 * @author Remi
 * @project alley-practice
 * @date 8/08/2025
 */
public interface TournamentBroadcaster extends Service {
    /**
     * Broadcasts a tournament event to all relevant players.
     * 向所有相关玩家广播锦标赛事件。
     *
     * @param event The broadcast event to send.
     *              要发送的广播事件。
     */
    void broadcast(BroadcastEvent event);

    /**
     * Sends a targeted message to specific participants of a tournament.
     * 向锦标赛的特定参与者发送定向消息。
     *
     * @param tournament   The tournament context.
     *                     锦标赛上下文。
     * @param participants The participants to message.
     *                     要发送消息的参与者。
     * @param message      The message to send.
     *                     要发送的消息。
     */
    void sendTargetedMessage(Tournament tournament, Iterable<TournamentParticipant> participants, String message);

    /**
     * Broadcasts the final tournament results and winner (top 3), using the
     * exact format of the original system.
     * 广播最终的锦标赛结果和获胜者（前三名），使用原始系统的确切格式。
     *
     * @param tournament The completed tournament.
     *                   已完成的锦标赛。
     * @param placements The final participant placements.
     *                   最终的参与者排名。
     */
    void broadcastResults(Tournament tournament, TournamentPlacements placements);
}