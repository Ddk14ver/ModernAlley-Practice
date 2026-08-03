package dev.revere.alley.feature.ffa.model;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.feature.ffa.FFAState;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * @author Emmy
 * @project Alley
 * @since 02/06/2025
 */
@Getter
@Setter
public class GameFFAPlayer {
    private final UUID uuid;
    private final String name;

    private FFAState state;

    /**
     * Constructor for the GameFFAPlayer class.
     * GameFFAPlayer 类的构造函数。
     *
     * @param uuid The UUID of the player.
     * @param uuid 玩家的 UUID。
     * @param name The name of the player.
     * @param name 玩家的名称。
     */
    public GameFFAPlayer(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.state = FFAState.SPAWN;
    }

    /**
     * Gets the Player object associated with this GameFFAPlayer.
     * 获取与此 GameFFAPlayer 关联的 Player 对象。
     *
     * @return The Player object, or null if the player is not online.
     * @return Player 对象，如果玩家不在线则为 null。
     */
    public Player getPlayer() {
        return AlleyPlugin.getInstance().getServer().getPlayer(this.uuid);
    }
}