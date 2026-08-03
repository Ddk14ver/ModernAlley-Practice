package dev.revere.alley.common.reflect.internal.types;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.reflect.Reflection;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

import java.time.Duration;

/**
 * @author Emmy
 * @project Alley
 * @since 03/04/2025
 */
public class TitleReflectionServiceImpl implements Reflection {
    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    /**
     * Send a title to a player with a subtitle and fade in/out times.
     * 向玩家发送标题、副标题以及淡入/淡出时间。
     *
     * @param player   the player to send the title to
     *                 要发送标题的玩家
     * @param title    the title to send
     *                 要发送的标题
     * @param subtitle the subtitle to send
     *                 要发送的副标题
     * @param fadeIn   the fade in time
     *                 淡入时间
     * @param stay     the stay time
     *                 停留时间
     * @param fadeOut  the fade out time
     *                 淡出时间
     */
    public void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        Profile profile = AlleyPlugin.getInstance().getService(ProfileService.class).getProfile(player.getUniqueId());
        if (profile == null || !profile.isOnline() || !profile.getProfileData().getSettingData().isServerTitles()) {
            return;
        }

        Component titleComponent = SERIALIZER.deserialize(CC.translate(title));
        Component subtitleComponent = SERIALIZER.deserialize(CC.translate(subtitle));

        Title.Times times = Title.Times.times(
            Duration.ofMillis(fadeIn * 50L),
            Duration.ofMillis(stay * 50L),
            Duration.ofMillis(fadeOut * 50L)
        );

        Title adventureTitle = Title.title(titleComponent, subtitleComponent, times);
        player.showTitle(adventureTitle);
    }

    /**
     * Send a title to a player with default fade in, stay, and fade out times.
     * 使用默认的淡入、停留和淡出时间向玩家发送标题。
     *
     * @param player   the player to send the title to
     *                 要发送标题的玩家
     * @param title    the title to send
     *                 要发送的标题
     * @param subtitle the subtitle to send
     *                 要发送的副标题
     */
    public void sendTitle(Player player, String title, String subtitle) {
        this.sendTitle(player, title, subtitle, 10, 20, 20);
    }
}