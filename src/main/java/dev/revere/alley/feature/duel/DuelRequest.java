package dev.revere.alley.feature.duel;

import dev.revere.alley.feature.arena.Arena;
import dev.revere.alley.feature.kit.Kit;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

/**
 * @author Emmy
 * @project Alley
 * @date 17/10/2024 - 20:04
 */
@Getter
@Setter
public class DuelRequest {
    private final Player sender;
    private final Player target;

    private Kit kit;

    private Arena arena;

    private final long expireTime;
    private boolean party;

    /**
     * Instantiates a new Duel request.
     * 实例化一个新的决斗请求。
     *
     * @param sender the sender
     *               发送者
     * @param target the target
     *               目标
     * @param kit    the kit
     *               套件
     * @param arena  the arena
     *               竞技场
     */
    public DuelRequest(Player sender, Player target, Kit kit, Arena arena, boolean party) {
        this.sender = sender;
        this.target = target;
        this.kit = kit;
        this.arena = arena;
        this.party = party;
        this.expireTime = System.currentTimeMillis() + 30000L;
    }

    /**
     * Check if the duel request has expired.
     * 检查决斗请求是否已过期。
     *
     * @return true if the duel request has expired, false otherwise
     *         如果决斗请求已过期返回true，否则返回false
     */
    public boolean hasExpired() {
        return System.currentTimeMillis() > expireTime;
    }

    /**
     * Get the remaining time until the duel request expires.
     * 获取决斗请求过期前的剩余时间。
     *
     * @return the remaining time until the duel request expires
     *         决斗请求过期前的剩余时间
     */
    public long getRemainingTime() {
        return expireTime - System.currentTimeMillis();
    }

    /**
     * Get the remaining time formatted as a string.
     * 获取以字符串格式表示的剩余时间。
     *
     * @return the remaining time formatted as a string
     *         以字符串格式表示的剩余时间
     */
    public String getRemainingTimeFormatted() {
        long seconds = getRemainingTime() / 1000;
        long minutes = seconds / 60;
        return String.format("%02d:%02d", minutes, seconds % 60);
    }
}