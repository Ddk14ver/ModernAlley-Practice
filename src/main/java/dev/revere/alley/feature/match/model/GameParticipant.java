package dev.revere.alley.feature.match.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * @author Remi
 * @project Alley
 * @date 5/21/2024
 */
@Getter
@Setter
public class GameParticipant<T extends GamePlayer> {
    // This is the leader of the participant
    // 这是参与方的队长
    private T leader;

    // These fields are all for game logic
    // 这些字段均用于游戏逻辑
    private boolean lostCheckpoint;
    private boolean bedBroken;
    private int teamHits;

    /**
     * Constructor for the GameParticipant class.
     * GameParticipant 类的构造函数。
     *
     * @param leader The player.
     *        玩家。
     */
    public GameParticipant(T leader) {
        this.leader = leader;
    }

    /**
     * Gets the player associated with the participant.
     * 获取与该参与方关联的玩家。
     *
     * @return The player associated with the participant.
     *         与该参与方关联的玩家。
     */
    public List<T> getPlayers() {
        return this.leader.isDisconnected() ? Collections.emptyList() : Collections.singletonList(this.leader);
    }

    /**
     * Gets the player associated with the participant.
     * 获取与该参与方关联的玩家。
     *
     * @return The player associated with the participant.
     *         与该参与方关联的玩家。
     */
    public List<T> getAllPlayers() {
        return Collections.singletonList(this.leader);
    }

    /**
     * Gets the player associated with the participant.
     * 获取与该参与方关联的玩家。
     *
     * @return The player associated with the participant.
     *         与该参与方关联的玩家。
     */
    public int getPlayerSize() {
        return this.leader.isDead() ? 0 : 1;
    }

    /**
     * Gets the amount of players that are alive.
     * 获取存活的玩家数量。
     *
     * @return The amount of players that are alive.
     *         存活的玩家数量。
     */
    public int getAlivePlayerSize() {
        return this.leader.isDead() ? 0 : 1;
    }

    /**
     * Gets the conjoined names of the players in the participant.
     * 获取参与方中所有玩家的合并名称。
     *
     * @return The conjoined names of the players in the participant.
     *         参与方中所有玩家的合并名称。
     */
    public String getConjoinedNames() {
        return this.leader.getUsername();
    }


    /**
     * Adds a player to the team participant.
     * 向队伍参与方添加一名玩家。
     *
     * @param player the player to add.
     *        要添加的玩家。
     */
    public void addPlayer(T player) {
        this.leader = player;
    }

    /**
     * Removes a player from the team participant.
     * 从队伍参与方中移除一名玩家。
     *
     * @param player The player to remove.
     *        要移除的玩家。
     */
    public void removePlayer(T player) {
       if (this.leader != null && this.leader.getUuid().equals(player.getUuid())) {
            this.leader = null;
        }
    }

    /**
     * Checks if all the players in the participant are dead.
     * 检查参与方中的所有玩家是否已死亡。
     *
     * @return True if all the players are dead.
     *         如果所有玩家都已死亡则返回 true。
     */
    public boolean isAllDead() {
        return this.leader.isDead();
    }

    /**
     * Checks if all the players in the participant are eliminated.
     * 检查参与方中的所有玩家是否已被淘汰。
     *
     * @return True if all the players are eliminated.
     *         如果所有玩家都已被淘汰则返回 true。
     */
    public boolean isAllEliminated() {
        return this.leader.isEliminated();
    }

    /**
     * Checks if the participant contains a player.
     * 检查参与方是否包含指定玩家。
     *
     * @param uuid The UUID of the player.
     *        玩家的 UUID。
     * @return True if the participant contains the player.
     *         如果参与方包含该玩家则返回 true。
     */
    public boolean containsPlayer(UUID uuid) {
        return this.leader.getUuid().equals(uuid);
    }
}