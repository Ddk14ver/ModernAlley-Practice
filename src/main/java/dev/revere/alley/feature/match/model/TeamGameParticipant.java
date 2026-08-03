package dev.revere.alley.feature.match.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Remi
 * @project Alley
 * @date 5/21/2024
 */
public class TeamGameParticipant<T extends GamePlayer> extends GameParticipant<T> {
    private final List<T> players;

    /**
     * Constructor for the TeamGameParticipant class.
     * TeamGameParticipant 类的构造函数。
     *
     * @param t The player.
     *        玩家。
     */
    public TeamGameParticipant(T t) {
        super(t);

        this.players = new ArrayList<>();
        this.players.add(t);
    }

    /**
     * Method to retrieve the players in the team participant who are not disconnected.
     * 获取队伍参与方中未断开连接的玩家。
     *
     * @return the list of players in the team participant.
     *         队伍参与方中的玩家列表。
     */
    @Override
    public List<T> getPlayers() {
        return this.players.stream()
                .filter(player -> !player.isDisconnected())
                .collect(Collectors.toList());
    }

    /**
     * Method to retrieve the players in the team participant who are not disconnected.
     * 获取队伍参与方中的所有玩家（包括已断开连接的）。
     *
     * @return the list of players in the team participant.
     *         队伍参与方中的玩家列表。
     */
    @Override
    public List<T> getAllPlayers() {
        return this.players;
    }

    /**
     * Gets the size of all players added to the list of players in the team participant.
     * 获取已添加到队伍参与方玩家列表中的玩家总数。
     *
     * @return the size of the player list.
     *         玩家列表的大小。
     */
    @Override
    public int getPlayerSize() {
        return this.players.size();
    }

    /**
     * Adds a player to the team participant.
     * 向队伍参与方添加一名玩家。
     *
     * @param t the player to add.
     *        要添加的玩家。
     */
    @Override
    public void addPlayer(T t) {
        if (t == null || this.players.contains(t)) {
            return;
        }

        this.players.add(t);
    }

    /**
     * Removes a player from the team participant.
     * 从队伍参与方中移除一名玩家。
     *
     * @param player The player to remove.
     *        要移除的玩家。
     */
    @Override
    public void removePlayer(T player) {
        this.players.remove(player);
    }

    /**
     * Gets the amount of players that are alive in the team participant.
     * 获取队伍参与方中存活的玩家数量。
     *
     * @return the amount of alive players in the team participant.
     *         队伍参与方中存活的玩家数量。
     */
    @Override
    public int getAlivePlayerSize() {
        int i = 0;

        for (GamePlayer gamePlayer : this.players) {
            if (!gamePlayer.isDead() && !gamePlayer.isDisconnected()) {
                i++;
            }
        }

        return i;
    }

    /**
     * Checks if all players in the team participant are dead or disconnected.
     * 检查队伍参与方中的所有玩家是否已死亡或断开连接。
     *
     * @return true if all players are dead or disconnected, false otherwise.
     *         如果所有玩家都已死亡或断开连接则返回 true，否则返回 false。
     */
    @Override
    public boolean isAllDead() {
        int i = 0;

        for (GamePlayer gamePlayer : this.players) {
            if (gamePlayer.isDead() || gamePlayer.isDisconnected()) {
                i++;
            }
        }

        return this.players.size() == i;
    }

    @Override
    public boolean isAllEliminated() {
        int i = 0;

        for (GamePlayer gamePlayer : this.players) {
            if (gamePlayer.isEliminated()) {
                i++;
            }
        }

        return this.players.size() == i;
    }

    /**
     * Method to determine whether the provided UUID is contained within the team participant's player list.
     * 判断提供的 UUID 是否包含在队伍参与方的玩家列表中。
     *
     * @param uuid The UUID of the player.
     *        玩家的 UUID。
     * @return true if the participant contains the player, false otherwise.
     *         如果参与方包含该玩家则返回 true，否则返回 false。
     */
    @Override
    public boolean containsPlayer(UUID uuid) {
        for (GamePlayer gamePlayer : this.players) {
            if (gamePlayer.getUuid().equals(uuid)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Gets the conjoined names of all players in the team participant.
     * 获取队伍参与方中所有玩家的合并名称。
     *
     * @return a string containing the conjoined names of the players.
     *         包含所有玩家合并名称的字符串。
     */
    @Override
    public String getConjoinedNames() {
        StringBuilder builder = new StringBuilder();

        int size = this.players.size();
        if (size == 1) {
            return this.players.get(0).getUsername();
        }

        for (int i = 0; i < size; i++) {
            builder.append(this.players.get(i).getUsername());

            if (i == size - 2) {
                builder.append(" and ");
            } else if (i < size - 2) {
                builder.append(", ");
            }
        }

        return builder.toString();
    }
}