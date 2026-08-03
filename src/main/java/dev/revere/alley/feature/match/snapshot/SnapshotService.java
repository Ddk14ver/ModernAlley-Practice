package dev.revere.alley.feature.match.snapshot;

import dev.revere.alley.bootstrap.lifecycle.Service;

import java.util.Map;
import java.util.UUID;

/**
 * @author Remi
 * @project alley-practice
 * @date 2/07/2025
 * 快照服务接口 - 管理赛后快照的存储、检索和删除。
 */
public interface SnapshotService extends Service {
    Map<UUID, Snapshot> getSnapshots();

    /**
     * Adds a post-match snapshot to the repository.
     * 向存储库添加一个赛后快照。
     * If a snapshot for the same player already exists, it will be overwritten.
     * 如果同一玩家已存在快照，则会被覆盖。
     *
     * @param snapshot The snapshot to add.
     *        要添加的快照。
     */
    void addSnapshot(Snapshot snapshot);

    /**
     * Retrieves a snapshot by the player's UUID.
     * 通过玩家 UUID 检索快照。
     *
     * @param uuid The UUID of the player.
     *        玩家的 UUID。
     * @return The Snapshot object, or null if not found.
     *         快照对象，如果未找到则返回 null。
     */
    Snapshot getSnapshot(UUID uuid);

    /**
     * Retrieves a snapshot by the player's username (case-insensitive).
     * 通过玩家用户名检索快照（不区分大小写）。
     *
     * @param username The username of the player.
     *        玩家的用户名。
     * @return The Snapshot object, or null if not found.
     *         快照对象，如果未找到则返回 null。
     */
    Snapshot getSnapshot(String username);

    /**
     * Removes a snapshot from the repository, typically after it has been viewed
     * 从存储库中移除快照，通常是在快照被查看后
     * or has expired.
     * 或已过期后执行。
     *
     * @param uuid The UUID of the player whose snapshot should be removed.
     *        要移除其快照的玩家的 UUID。
     */
    void removeSnapshot(UUID uuid);
}