package dev.revere.alley.feature.emoji.listener;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.feature.emoji.EmojiService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Map;

/**
 * @author Emmy
 * @project Alley
 * @date 10/11/2024 - 09:34
 */
public class EmojiListener implements Listener {
    @EventHandler
    private void onAsyncChat(AsyncPlayerChatEvent event) {
        String message = event.getMessage();

        for (Map.Entry<String, String> entry : AlleyPlugin.getInstance().getService(EmojiService.class).getEmojis().entrySet()) {
            if (message.contains(entry.getKey())) {
                message = message.replace(entry.getKey(), entry.getValue());
            }
        }

        event.setMessage(message);
    }
}