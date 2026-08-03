package dev.revere.alley.common.server;

import dev.revere.alley.bootstrap.lifecycle.Service;
import org.bukkit.entity.EntityType;

/**
 * Server environment service interface.
 * 服务器环境服务接口。
 *
 * @author Remi
 * @project alley-practice
 * @date 2/07/2025
 */
public interface ServerEnvironment extends Service {
    /**
     * Clears all entities of a specific type from all worlds.
     * 从所有世界中清除指定类型的所有实体。
     *
     * @param entityType The type of entity to clear.
     *                   要清除的实体类型。
     */
    void clearEntities(EntityType entityType);

    /**
     * Clears all entities of any type from all worlds.
     * 从所有世界中清除所有类型的实体。
     * <p>
     * Caution for people who may use this in their own project:
     * 对于可能在自己的项目中使用此功能的人请注意：
     * This is destructive and will remove players' mounts, item frames, etc.
     * 此操作具有破坏性，将移除玩家的坐骑、物品展示框等。
     */
    void clearAllEntities();
}