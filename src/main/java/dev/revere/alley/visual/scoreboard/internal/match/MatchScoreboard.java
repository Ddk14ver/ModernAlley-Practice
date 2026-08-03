package dev.revere.alley.visual.scoreboard.internal.match;

import dev.revere.alley.feature.match.model.internal.MatchGamePlayer;
import dev.revere.alley.feature.match.model.GameParticipant;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.common.reflect.utility.ReflectionUtility;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * @author Emmy
 * @project Alley
 * @since 30/04/2025
 */
public interface MatchScoreboard {
    /**
     * Gets the scoreboard lines for the given profile in a regular solo match.
     * 获取常规单人比赛中给定档案的记分板行。
     *
     * @param profile  The profile to get the scoreboard lines for.
     *                 要获取记分板行的档案。
     * @param player   The player whose scoreboard is being displayed.
     *                 正在显示其记分板的玩家。
     * @param you      The player whose scoreboard is being displayed.
     *                 正在显示其记分板的玩家。
     * @param opponent The opponent player.
     *                 对手玩家。
     * @return The scoreboard lines.
     *         记分板行列表。
     */
    List<String> getLines(Profile profile, Player player, GameParticipant<MatchGamePlayer> you, GameParticipant<MatchGamePlayer> opponent);

    /**
     * Gets the corresponding color of the player including the player's name.
     * 获取玩家对应的颜色，包括玩家名称。
     *
     * @param profile The profile of the player.
     *                玩家的档案。
     * @return The formatted player name with color.
     *         带有颜色的格式化玩家名称。
     */
    default String getColoredName(Profile profile) {
        ChatColor nameColor = profile.getNameColor();
        String name = profile.getName();

        if (nameColor != null) {
            return nameColor + name;
        } else {
            return ChatColor.WHITE + name;
        }
    }

    /**
     * Gets the ping of the player by using reflect.
     * 通过反射获取玩家的延迟。
     *
     * @param player The player to get the ping for.
     *               要获取延迟的玩家。
     * @return The ping of the player.
     *         玩家的延迟值。
     */
    default int getPing(Player player) {
        if (player == null) {
            return 0;
        }

        return ReflectionUtility.getPing(player);
    }
}