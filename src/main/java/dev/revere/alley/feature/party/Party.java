package dev.revere.alley.feature.party;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.feature.emoji.EmojiService;
import dev.revere.alley.common.text.CC;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 队伍类，表示一个游戏队伍及其成员管理。
 * @author Emmy
 * @project Alley
 * @date 21/05/2024 - 21:42
 */
@Getter
@Setter
public class Party {
    private Player leader;
    private PartyState state;
    private List<UUID> members;
    private List<UUID> bannedPlayers;

    /**
     * Constructor for the Party class.
     * Party 类的构造函数。
     *
     * @param leader The leader of the party.
     *               队伍的队长。
     */
    public Party(Player leader) {
        this.leader = leader;
        this.members = new ArrayList<>();
        this.members.add(leader.getUniqueId());
        this.bannedPlayers = new ArrayList<>();
        this.state = PartyState.PRIVATE;
    }

    /**
     * Sends a message to all party members.
     * 向所有队伍成员发送消息。
     *
     * @param message The message to notify the party members of.
     *                要通知队伍成员的消息。
     */
    public void notifyParty(String message) {
        for (Map.Entry<String, String> entry : AlleyPlugin.getInstance().getService(EmojiService.class).getEmojis().entrySet()) {
            if (message.contains(entry.getKey())) {
                message = message.replace(entry.getKey(), entry.getValue());
            }
        }

        for (UUID member : members) {
            Player player = AlleyPlugin.getInstance().getServer().getPlayer(member);
            if (player != null) {
                player.sendMessage(CC.translate(message));
            }
        }
    }

    /**
     * Determines whether the specified player is the leader of the party.
     * 判断指定玩家是否是队伍的队长。
     *
     * @param player The player to check.
     *               要检查的玩家。
     * @return True if the specified player is the leader of the party, false otherwise.
     *         如果指定玩家是队长则返回 true，否则返回 false。
     */
    public boolean isLeader(Player player) {
        return player != null && leader != null
                && leader.getUniqueId().equals(player.getUniqueId());
    }

    /**
     * Checks if the party is private.
     * 检查队伍是否为私有状态。
     *
     * @return True if the party is private, false otherwise.
     *         如果队伍是私有的则返回 true，否则返回 false。
     */
    public boolean isPrivate() {
        return this.state == PartyState.PRIVATE;
    }
}
