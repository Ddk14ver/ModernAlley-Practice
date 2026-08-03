package dev.revere.alley.feature.tournament.validation;

import dev.revere.alley.bootstrap.lifecycle.Service;
import dev.revere.alley.feature.tournament.model.Tournament;
import org.bukkit.entity.Player;

/**
 * @author Remi
 * @project alley-practice
 * @date 8/08/2025
 */
public interface ParticipantValidator extends Service {
    /**
     * Validates if a player can join a tournament.
     * 验证玩家是否可以加入锦标赛。
     *
     * @param player     The player attempting to join.
     *                   尝试加入的玩家。
     * @param tournament The tournament to validate against.
     *                   要验证的锦标赛。
     * @return True if player can join.
     *         如果玩家可以加入则返回 true。
     */
    boolean canPlayerJoin(Player player, Tournament tournament);

    /**
     * Validates if a player's party has space in the tournament.
     * 验证玩家所在队伍在锦标赛中是否有空位。
     *
     * @param player     The player whose party to check.
     *                   要检查其队伍的玩家。
     * @param tournament The tournament to check space in.
     *                   要检查空位的锦标赛。
     * @return True if there's space.
     *         如果有空位则返回 true。
     */
    boolean hasSpaceForParty(Player player, Tournament tournament);
}