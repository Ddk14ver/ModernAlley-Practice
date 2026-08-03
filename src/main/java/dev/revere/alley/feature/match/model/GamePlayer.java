package dev.revere.alley.feature.match.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Remi
 * @project Alley
 * @date 5/21/2024
 */
@Data
@Getter
@Setter
public class GamePlayer {
    private UUID uuid;
    private String username;

    private boolean disconnected;
    private boolean eliminated;
    private boolean dead;

    private List<UUID> players;

    // These fields are all for game logic
    // 这些字段均用于游戏逻辑
    private Location checkpoint;
    private int checkpointCount;
    private final List<Location> checkpoints;

    /**
     * Constructor for the GamePlayer class.
     * GamePlayer 类的构造函数。
     *
     * @param uuid     The UUID of the player.
     *                 玩家的 UUID。
     * @param username The username of the player.
     *                 玩家的用户名。
     */
    public GamePlayer(UUID uuid, String username) {
        this.uuid = uuid;
        this.username = username;
        this.players = new ArrayList<>();

        this.checkpoints = new ArrayList<>();
        this.checkpointCount = 0;
        this.checkpoint = null;
    }

    /**
     * Gets the player associated with the GamePlayer.
     * 获取与 GamePlayer 关联的玩家。
     *
     * @return The player associated with the GamePlayer.
     *         与 GamePlayer 关联的玩家。
     */
    public Player getTeamPlayer() {
        Player onlinePlayer = Bukkit.getPlayer(uuid);
        if (onlinePlayer != null) return onlinePlayer;

        // Bukkit.getEntity may load/query chunk entities and is forbidden from the async scoreboard thread.
        if (!Bukkit.isPrimaryThread()) return null;

        Entity entity = Bukkit.getEntity(uuid);
        return entity instanceof Player npcPlayer ? npcPlayer : null;
    }
}
