package dev.revere.alley.feature.tournament.validation;

import dev.revere.alley.bootstrap.lifecycle.Service;
import dev.revere.alley.feature.tournament.model.Tournament;
import org.bukkit.entity.Player;

/**
 * @author Remi
 * @project alley-practice
 * @date 8/08/2025
 */
public interface TournamentValidationService extends Service {
    /**
     * Validates if a player can host a tournament.
     * 验证玩家是否可以举办锦标赛。
     *
     * @param player The player attempting to host.
     *               尝试举办锦标赛的玩家。
     * @return True if they can host.
     *         如果可以举办则返回 true。
     */
    boolean canPlayerHostTournament(Player player);

    /**
     * Validates if a tournament can be force started.
     * 验证锦标赛是否可以强制开始。
     *
     * @param tournament The tournament to check.
     *                   要检查的锦标赛。
     * @return True if it can be force started.
     *         如果可以强制开始则返回 true。
     */
    boolean canForceStartTournament(Tournament tournament);
}
