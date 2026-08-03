package dev.revere.alley.feature.clan;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 26/06/2026
 *
 * Represents a clan (guild/group) with leader, officers, members, and associated data.
 * 表示一个拥有队长、官员、成员及相关数据的公会。
 */
@Getter
@Setter
public class Clan {
    /** Unique clan name (lowercase for storage, case-preserved for display). */
    private String name;

    /** Display name with color. */
    private String displayName;

    /** Clan description. */
    private String description;

    /** Clan leader's UUID (also in members list). */
    private UUID leader;

    /** Officer UUIDs (can invite, kick members). */
    private final List<UUID> officers = new ArrayList<>();

    /** All member UUIDs (includes leader and officers). */
    private final List<UUID> members = new ArrayList<>();

    /** Banned player UUIDs. */
    private final List<UUID> bannedPlayers = new ArrayList<>();

    /** Clan display color. */
    private ChatColor color = ChatColor.WHITE;

    /** Clan points (earned from tournaments and events). */
    private int points;

    /** Clan home location (for teleport). */
    private Location home;

    /** Whether the clan is invite-only. */
    private boolean inviteOnly = true;

    /** Whether clan chat is muted for non-officers. */
    private boolean chatMuted = false;

    /** Clan creation timestamp. */
    private final long createdAt;

    /**
     * Constructor for the Clan class.
     *
     * @param name   The unique clan name.
     * @param leader The clan leader player.
     */
    public Clan(String name, Player leader) {
        this(name, leader.getUniqueId());
    }

    /**
     * Constructor for deserialization (offline leader support).
     *
     * @param name      The unique clan name.
     * @param leaderUuid The clan leader's UUID.
     */
    public Clan(String name, UUID leaderUuid) {
        this.name = name;
        this.displayName = name;
        this.description = "A new clan.";
        this.leader = leaderUuid;
        this.members.add(leaderUuid);
        this.color = ChatColor.WHITE;
        this.points = 0;
        this.createdAt = System.currentTimeMillis();
    }

    /**
     * Gets the colored display name of the clan.
     *
     * @return The color + name string.
     */
    public String getColoredName() {
        return this.color + this.displayName;
    }

    /**
     * Gets all online player members.
     *
     * @return List of online Player objects.
     */
    public List<Player> getOnlinePlayers() {
        List<Player> online = new ArrayList<>();
        for (UUID uuid : this.members) {
            Player player = org.bukkit.Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                online.add(player);
            }
        }
        return online;
    }

    /**
     * Gets formatted list of member names (online=green, offline=gray).
     *
     * @return Comma-separated member string.
     */
    public String getMemberListFormatted() {
        StringBuilder sb = new StringBuilder();
        for (UUID uuid : this.members) {
            Player player = org.bukkit.Bukkit.getPlayer(uuid);
            String name = org.bukkit.Bukkit.getOfflinePlayer(uuid).getName();
            if (name == null) name = uuid.toString();

            if (uuid.equals(this.leader)) {
                sb.append("&6&l★ &f");
            } else if (this.officers.contains(uuid)) {
                sb.append("&e&l☆ &f");
            }

            if (player != null && player.isOnline()) {
                sb.append("&a").append(name);
            } else {
                sb.append("&7").append(name);
            }
            sb.append("&7, ");
        }
        if (sb.length() > 2) sb.setLength(sb.length() - 4);
        return sb.toString();
    }

    /**
     * Checks if a player is the leader.
     *
     * @param player The player to check.
     * @return True if the player is the leader.
     */
    public boolean isLeader(Player player) {
        return this.leader.equals(player.getUniqueId());
    }

    /**
     * Checks if a player is an officer.
     *
     * @param player The player to check.
     * @return True if the player is an officer.
     */
    public boolean isOfficer(Player player) {
        return this.officers.contains(player.getUniqueId());
    }

    /**
     * Checks if a player is the leader or an officer.
     *
     * @param player The player to check.
     * @return True if the player has staff privileges.
     */
    public boolean isLeaderOrOfficer(Player player) {
        return this.isLeader(player) || this.isOfficer(player);
    }

    /**
     * Checks if a player is a member of this clan.
     *
     * @param player The player to check.
     * @return True if the player is a member.
     */
    public boolean isMember(Player player) {
        return this.members.contains(player.getUniqueId());
    }

    /**
     * Checks if a player is banned from this clan.
     *
     * @param player The player to check.
     * @return True if the player is banned.
     */
    public boolean isBanned(Player player) {
        return this.bannedPlayers.contains(player.getUniqueId());
    }

    /**
     * Gets the total member count.
     *
     * @return The number of members (including leader, officers).
     */
    public int getMemberCount() {
        return this.members.size();
    }

    /**
     * Gets the number of online members.
     *
     * @return Count of online members.
     */
    public int getOnlineCount() {
        return this.getOnlinePlayers().size();
    }

    /**
     * Broadcasts a message to all online members.
     *
     * @param message The message to broadcast.
     */
    public void broadcast(String message) {
        for (Player player : this.getOnlinePlayers()) {
            player.sendMessage(message);
        }
    }

    /**
     * Adds a member to the clan.
     *
     * @param player The player to add.
     */
    public void addMember(Player player) {
        if (!this.members.contains(player.getUniqueId())) {
            this.members.add(player.getUniqueId());
        }
    }

    /**
     * Removes a member from the clan.
     *
     * @param uuid The UUID to remove.
     */
    public void removeMember(UUID uuid) {
        this.members.remove(uuid);
        this.officers.remove(uuid);
    }

    /**
     * Promotes a member to officer.
     *
     * @param player The player to promote.
     */
    public void promoteToOfficer(Player player) {
        if (this.isMember(player) && !this.isOfficer(player) && !this.isLeader(player)) {
            this.officers.add(player.getUniqueId());
        }
    }

    /**
     * Demotes an officer to member.
     *
     * @param player The player to demote.
     */
    public void demoteFromOfficer(Player player) {
        this.officers.remove(player.getUniqueId());
    }
}
