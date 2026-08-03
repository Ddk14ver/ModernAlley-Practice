package dev.revere.alley.feature.party;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

/**
 * 队伍请求类，表示一个队伍加入邀请。
 * @author Emmy
 * @project Alley
 * @date 25/05/2024 - 18:44
 */
@Getter
@Setter
public class PartyRequest {
    private final Player sender;
    private final Player target;

    private final long expireTime;

    /**
     * Constructor for the PartyRequest class.
     * PartyRequest 类的构造函数。
     *
     * @param sender The player sending the request.
     *               发送请求的玩家。
     * @param target The player receiving the request.
     *               接收请求的玩家。
     */
    public PartyRequest(Player sender, Player target) {
        this.sender = sender;
        this.target = target;
        this.expireTime = System.currentTimeMillis() + 300000L;
    }

    /**
     * Check if the party request has expired.
     * 检查队伍请求是否已过期。
     *
     * @return True if the party request has expired, false otherwise.
     *         如果请求已过期则返回 true，否则返回 false。
     */
    public boolean hasExpired() {
        return System.currentTimeMillis() > this.expireTime;
    }

    /*
    public long getRemainingTime() {
        return this.expireTime - System.currentTimeMillis();
    }

    public String getRemainingTimeFormatted() {
        long seconds = this.getRemainingTime() / 1000;
        long minutes = seconds / 60;
        return String.format("%02d:%02d", minutes, seconds % 60);
    }*/
}