package dev.revere.alley.feature.match.internal.types;

import org.bukkit.entity.Player;

import java.util.UUID;

public interface GomokuPlayable {
    boolean tryPlaceFromView(Player player);

    void surrender(Player player);

    boolean isCurrentPlayer(Player player);

    String getCurrentPlayerName();

    String getCurrentColorName();

    String getPlayerColorName(UUID playerId);

    int getRemainingTurnSeconds();

    int getPlacedStones();

    void cleanupGomoku();
}
