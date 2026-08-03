package dev.revere.alley.feature.tournament.player;

import dev.revere.alley.bootstrap.lifecycle.Service;
import dev.revere.alley.feature.tournament.model.Tournament;
import org.bukkit.entity.Player;

/**
 * @author Remi
 * @project alley-practice
 * @date 8/08/2025
 */
public interface PlayerTournamentStateService extends Service {
    /**
     * Sets a player's profile state to PLAYING_TOURNAMENT and applies their
     * tournament hotbar.
     * 将玩家的个人资料状态设置为 PLAYING_TOURNAMENT 并应用其锦标赛快捷栏。
     *
     * @param player The player to update.
     *               要更新的玩家。
     * @param tournament The tournament they are joining.
     *                   他们正在加入的锦标赛。
     */
    void setPlayerTournamentState(Player player, Tournament tournament);

    /**
     * Resets a player's profile state back to LOBBY and teleports them to spawn
     * if applicable.
     * 将玩家的个人资料状态重置为 LOBBY，并在适用时将其传送到出生点。
     *
     * @param player The player to reset.
     *               要重置的玩家。
     */
    void resetPlayerStateToLobby(Player player);
}