package dev.revere.alley.feature.clan;

import lombok.Getter;

import java.util.UUID;

/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 26/06/2026
 *
 * Represents a pending clan invitation from one player to another.
 * 表示一个从一名玩家发送给另一名玩家的待处理公会邀请。
 */
@Getter
public class ClanInvite {
    private final String clanName;
    private final UUID sender;
    private final UUID target;
    private final long timestamp;

    /** Invite expires after 5 minutes (300,000 ms). */
    private static final long EXPIRE_TIME = 300_000L;

    /**
     * Constructor for ClanInvite.
     *
     * @param clanName The name of the clan sending the invite.
     * @param sender   The UUID of the invite sender.
     * @param target   The UUID of the invite receiver.
     */
    public ClanInvite(String clanName, UUID sender, UUID target) {
        this.clanName = clanName;
        this.sender = sender;
        this.target = target;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Checks if this invite has expired.
     *
     * @return True if the invite is older than EXPIRE_TIME.
     */
    public boolean isExpired() {
        return System.currentTimeMillis() - this.timestamp >= EXPIRE_TIME;
    }
}
