package dev.revere.alley.common;

import dev.revere.alley.AlleyPlugin;
import lombok.experimental.UtilityClass;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * @author Emmy
 * @project alley-practice
 * @since 26/09/2025
 */
@UtilityClass
public class PlaceholderUtil {
    /**
     * Safely sets PlaceholderAPI placeholders if the plugin is enabled.
     * If PlaceholderAPI is not enabled, it returns the original messages.
     * 如果插件已启用，安全地设置PlaceholderAPI占位符。
     * 如果PlaceholderAPI未启用，则返回原始消息。
     *
     * @param player   the player for whom to set the placeholders
     *                 要为其设置占位符的玩家
     * @param messages the list of messages to set placeholders in
     *                 要设置占位符的消息列表
     * @return a list of messages with placeholders set, or the original messages if PlaceholderAPI is not enabled
     *         设置了占位符的消息列表，如果PlaceholderAPI未启用则返回原始消息
     */
    public List<String> setPapiSafe(Player player, List<String> messages) {
        AlleyPlugin plugin = AlleyPlugin.getInstance();
        if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            return PlaceholderAPI.setPlaceholders(player, messages);
        }

        return messages;
    }

    /**
     * Safely sets PlaceholderAPI placeholders if the plugin is enabled.
     * If PlaceholderAPI is not enabled, it returns the original message.
     * 如果插件已启用，安全地设置PlaceholderAPI占位符。
     * 如果PlaceholderAPI未启用，则返回原始消息。
     *
     * @param player  the player for whom to set the placeholders
     *                要为其设置占位符的玩家
     * @param message the message to set placeholders in
     *                要设置占位符的消息
     * @return a message with placeholders set, or the original message if PlaceholderAPI is not enabled
     *         设置了占位符的消息，如果PlaceholderAPI未启用则返回原始消息
     */
    public String setPapiSafe(Player player, String message) {
        AlleyPlugin plugin = AlleyPlugin.getInstance();
        if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            return PlaceholderAPI.setPlaceholders(player, message);
        }

        return message;
    }
}