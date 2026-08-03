package dev.revere.alley.feature.tournament.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @author Remi
 * @project alley-practice
 * @date 6/08/2025
 */
@Getter
@EqualsAndHashCode(of = "groupId")
public class TournamentParticipant {
    private final UUID groupId;
    @Setter
    private UUID leaderUuid;
    @Setter private String leaderName;

    private final List<UUID> memberUuids = new CopyOnWriteArrayList<>();

    /**
     * Constructs a new TournamentParticipant for a party of players.
     * 为玩家队伍构造一个新的TournamentParticipant。
     *
     * @param leader   The player designated as the party leader.
     *                 被指定为队伍队长的玩家。
     * @param members  A list of all members in the party, including the leader.
     *                 队伍中所有成员的列表，包括队长。
     */
    public TournamentParticipant(Player leader, List<Player> members) {
        this.groupId = UUID.randomUUID();
        this.leaderUuid = leader.getUniqueId();
        this.leaderName = leader.getName();
        members.forEach(m -> this.memberUuids.add(m.getUniqueId()));
    }

    /**
     * Constructs a new TournamentParticipant for a single, solo player.
     * 为单个独奏玩家构造一个新的TournamentParticipant。
     *
     * @param soloPlayer The solo player.
     *                   独奏玩家。
     */
    public TournamentParticipant(Player soloPlayer) {
        this.groupId = UUID.randomUUID();
        this.leaderUuid = soloPlayer.getUniqueId();
        this.leaderName = soloPlayer.getName();
        this.memberUuids.add(soloPlayer.getUniqueId());
    }

    /**
     * Gets a list of all currently online players in this participant group.
     * 获取此参赛者组中所有当前在线的玩家列表。
     *
     * @return A list of online Player objects.
     *         在线Player对象列表。
     */
    public List<Player> getOnlinePlayers() {
        return memberUuids.stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Gets the total number of members in this participant group.
     * 获取此参赛者组中成员的总数量。
     *
     * @return The size of the group.
     *         组的大小。
     */
    public int getSize() {
        return memberUuids.size();
    }

    /**
     * Merges another participant group into this one. This is used for combining
     * smaller groups during team formation.
     * 将另一个参赛者组合并到此组中。用于在队伍组建时合并较小的组。
     *
     * @param other The other participant group to merge.
     *              要合并的另一个参赛者组。
     */
    public void merge(TournamentParticipant other) {
        other.getMemberUuids().forEach(uuid -> {
            if (!this.memberUuids.contains(uuid)) {
                this.memberUuids.add(uuid);
            }
        });
    }

    /**
     * Checks if a specific player is a member of this group.
     * 检查某个玩家是否为此组的成员。
     *
     * @param uuid The UUID of the player to check.
     *             要检查的玩家的UUID。
     * @return {@code true} if the player is in the group, {@code false} otherwise.
     *         如果玩家在组中则返回{@code true}，否则返回{@code false}。
     */
    public boolean containsPlayer(UUID uuid) {
        return memberUuids.contains(uuid);
    }

    /**
     * Removes a player from this group. If the leader leaves, a new leader is promoted.
     * 从此组移除一个玩家。如果队长离开，将推选一名新队长。
     *
     * @param memberUuid The UUID of the member to remove.
     *                   要移除的成员的UUID。
     */
    public void removeMember(UUID memberUuid) {
        memberUuids.remove(memberUuid);

        boolean wasLeader = this.leaderUuid.equals(memberUuid);

        if (wasLeader && !memberUuids.isEmpty()) {
            UUID newLeaderUuid = memberUuids.get(0);
            Player newLeaderPlayer = Bukkit.getPlayer(newLeaderUuid);

            setLeaderUuid(newLeaderUuid);
            if (newLeaderPlayer != null) {
                setLeaderName(newLeaderPlayer.getName());
                newLeaderPlayer.getName();
            }
        }
    }

    /**
     * Checks if the participant group has any members left.
     * 检查参赛者组是否还有剩余成员。
     *
     * @return {@code true} if the group is empty, {@code false} otherwise.
     *         如果组为空则返回{@code true}，否则返回{@code false}。
     */
    public boolean isEmpty() {
        return memberUuids.isEmpty();
    }
}