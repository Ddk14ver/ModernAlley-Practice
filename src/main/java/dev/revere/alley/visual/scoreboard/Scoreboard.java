package dev.revere.alley.visual.scoreboard;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.core.profile.enums.ProfileState;
import dev.revere.alley.common.animation.AnimationService;
import dev.revere.alley.common.animation.AnimationType;
import dev.revere.alley.common.animation.internal.types.DotAnimation;
import dev.revere.alley.common.reflect.utility.ReflectionUtility;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * @author Emmy
 * @project Alley
 * @since 30/04/2025
 *
 * 计分板接口，定义计分板行的获取方法。
 */
public interface Scoreboard {
    /**
     * Gets the scoreboard lines for the given profile.
     * 获取指定档案的计分板行。
     *
     * @param profile The profile to get the scoreboard lines for.
     *                要获取计分板行的档案。
     * @return The scoreboard lines.
     *         计分板行列表。
     */
    List<String> getLines(Profile profile);

    /**
     * Gets the scoreboard lines for the given profile.
     * 获取指定档案和玩家的计分板行。
     *
     * @param profile The profile to get the scoreboard lines for.
     *                要获取计分板行的档案。
     * @param player  The player to get the scoreboard lines for.
     *                要获取计分板行的玩家。
     * @return The scoreboard lines.
     *         计分板行列表。
     */
    List<String> getLines(Profile profile, Player player);

    /**
     * Gets the dot animation for the scoreboard.
     * 获取计分板的点动画。
     *
     * @return The dot animation.
     *         点动画实例。
     */
    default DotAnimation getDotAnimation() {
        return AlleyPlugin.getInstance().getService(AnimationService.class).getAnimation(DotAnimation.class, AnimationType.INTERNAL);
    }

    /**
     * Gets the ping of the player by using reflect.
     * 通过反射获取玩家的延迟值。
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

    /**
     * Safely counts the number of profiles in a given state.
     * 安全地统计处于指定状态的档案数量。
     *
     * @param service The ProfileService to use for counting.
     *                用于计数的 ProfileService。
     * @param state   The ProfileState to count.
     *                要统计的 ProfileState。
     * @return The count of profiles in the specified state, or 0 if an error occurs.
     *         处于指定状态的档案数量，如果发生错误则返回 0。
     */
    default int safeCountState(ProfileService service, ProfileState state) {
        try {
            return (int) service.getProfiles().values().parallelStream()
                    .filter(p -> p.getState() == state)
                    .count();
        } catch (Exception e) {
            return 0;
        }
    }
}