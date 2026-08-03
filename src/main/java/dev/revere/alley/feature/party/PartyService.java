package dev.revere.alley.feature.party;

import dev.revere.alley.feature.arena.Arena;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.bootstrap.lifecycle.Service;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

/**
 * 队伍服务接口，定义队伍创建、管理和匹配相关操作。
 * @author Remi
 * @project alley-practice
 * @date 2/07/2025
 */
public interface PartyService extends Service {
    /**
     * Gets a list of all currently active parties.
     * 获取所有当前活跃的队伍列表。
     *
     * @return An unmodifiable list of active parties.
     *         一个不可修改的活跃队伍列表。
     */
    List<Party> getParties();

    /**
     * Creates a new party with the given player as the leader.
     * 以给定玩家为队长创建新队伍。
     *
     * @param player The player creating the party.
     *               创建队伍的玩家。
     */
    void createParty(Player player);

    /**
     * Disbands a party, kicking all members. This can only be done by the leader.
     * 解散队伍并踢出所有成员。此操作只能由队长执行。
     *
     * @param leader The leader of the party to disband.
     *               要解散队伍的队长。
     */
    void disbandParty(Player leader);

    /**
     * Allows a player to leave the party they are currently in.
     * 允许玩家离开其当前所在的队伍。
     * If they are the leader, the party is disbanded.
     * 如果玩家是队长，队伍将被解散。
     *
     * @param player The player leaving the party.
     *               要离开队伍的玩家。
     */
    void leaveParty(Player player);

    /**
     * Kicks a member from the party. Can only be initiated by the party leader.
     * 从队伍中踢出成员。此操作只能由队长发起。
     *
     * @param leader The party leader.
     *               队伍队长。
     * @param member The player to kick.
     *               要踢出的玩家。
     */
    void kickMember(Player leader, Player member);

    /**
     * Bans a player from the party, preventing them from rejoining.
     * 封禁玩家使其无法重新加入队伍。
     *
     * @param leader The party leader.
     *               队伍队长。
     * @param target The player to ban.
     *               要封禁的玩家。
     */
    void banMember(Player leader, Player target);

    /**
     * Unbans a player from the party.
     * 解除对玩家的队伍封禁。
     *
     * @param leader The party leader.
     *               队伍队长。
     * @param target The player to unban.
     *               要解封的玩家。
     */
    void unbanMember(Player leader, Player target);

    /**
     * Allows a player to join an existing party.
     * 允许玩家加入现有的队伍。
     *
     * @param player The player joining.
     *               加入队伍的玩家。
     * @param leader The leader of the party to join.
     *               要加入队伍的队长。
     */
    void joinParty(Player player, Player leader);

    /**
     * Sends a party invitation from a sender to a target player.
     * 从发送方向目标玩家发送队伍邀请。
     *
     * @param party  The party instance.
     *               队伍实例。
     * @param sender The player sending the invite.
     *               发送邀请的玩家。
     * @param target The player receiving the invite.
     *               接收邀请的玩家。
     */
    void sendInvite(Party party, Player sender, Player target);

    /**
     * Gets the party that a player is the leader of.
     * 获取玩家作为队长的队伍。
     *
     * @param player The potential leader.
     *               潜在的队长。
     * @return The Party object, or null if they are not a leader.
     *         队伍对象，如果不是队长则返回 null。
     */
    Party getPartyByLeader(Player player);

    /**
     * Gets the party that a player is a member of.
     * 获取玩家作为成员的队伍。
     *
     * @param uuid The UUID of the potential member.
     *             潜在成员的 UUID。
     * @return The Party object, or null if they are not in a party.
     *         队伍对象，如果不在队伍中则返回 null。
     */
    Party getPartyByMember(UUID uuid);

    /**
     * Gets the party a player is in, regardless of their role (leader or member).
     * 获取玩家所在的队伍，无论其角色（队长或成员）。
     *
     * @param player The player.
     *               玩家。
     * @return The Party object, or null if they are not in a party.
     *         队伍对象，如果不在队伍中则返回 null。
     */
    Party getParty(Player player);

    /**
     * Starts a 2v2 party match.
     * 启动 2v2 队伍比赛。
     *
     * @param kit   The kit for the match.
     *              比赛的装备包。
     * @param arena The arena for the match.
     *              比赛的竞技场。
     * @param party The party starting the match.
     *              发起比赛的队伍。
     */
    void startSplitMatch(Kit kit, Arena arena, Party party);

    /**
     * Starts a free-for-all match for a party.
     * 启动队伍的自由混战比赛。
     *
     * @param kit   The kit for the match.
     *              比赛的装备包。
     * @param arena The arena for the match.
     *              比赛的竞技场。
     * @param party The party starting the match.
     *              发起比赛的队伍。
     */
    void startFFAMatch(Kit kit, Arena arena, Party party);

    /**
     * Announces a party to the entire server, inviting players to join.
     * 向整个服务器公告队伍信息，邀请玩家加入。
     *
     * @param party The party to announce.
     *              要公告的队伍。
     */
    void announceParty(Party party);

    /**
     * Gets the list of all party requests.
     * 获取所有队伍请求列表。
     *
     * @return A list of party requests.
     *         队伍请求列表。
     */
    List<PartyRequest> getPartyRequests();

    /**
     * Gets the party request for a specific player.
     * 获取特定玩家的队伍请求。
     *
     * @param player The player.
     *               玩家。
     * @return The PartyRequest, or null if none.
     *         队伍请求，如果没有则返回 null。
     */
    PartyRequest getRequest(Player player);

    /**
     * Removes a party request.
     * 移除队伍请求。
     *
     * @param request The request to remove.
     *                要移除的请求。
     */
    void removeRequest(PartyRequest request);

    /**
     * Gets the chat format
     * 获取聊天格式
     *
     * @return The chat format
     *         聊天格式
     */
    String getChatFormat();
}