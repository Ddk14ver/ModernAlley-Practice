package dev.revere.alley.feature.abilities.cooldown;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 26/06/2026
 */
public class AbilityCooldown {

    private final Map<UUID, Long> cooldownMap = new HashMap<>();

    public void applyCooldown(Player player, long cooldown) {
        cooldownMap.put(player.getUniqueId(), System.currentTimeMillis() + cooldown);
    }

    public boolean onCooldown(Player player) {
        return cooldownMap.containsKey(player.getUniqueId()) && (cooldownMap.get(player.getUniqueId()) >= System.currentTimeMillis());
    }

    public void cooldownRemove(Player player) {
        cooldownMap.remove(player.getUniqueId());
    }

    public String getRemaining(Player player) {
        long l = cooldownMap.get(player.getUniqueId()) - System.currentTimeMillis();
        return DurationFormatUtils.formatDuration(l, "s");
    }

    public long getRemainingMillis(Player player) {
        long l = cooldownMap.get(player.getUniqueId());
        return (int) (l - System.currentTimeMillis());
    }
}