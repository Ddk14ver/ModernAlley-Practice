package dev.revere.alley.core.profile;

import com.mongodb.client.MongoCollection;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.bootstrap.lifecycle.Service;
import dev.revere.alley.core.database.model.DatabaseProfile;
import org.bson.Document;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

/**
 * @author Remi
 * @project alley-practice
 * @date 2/07/2025
 * 玩家档案服务接口，提供对玩家档案的访问和管理功能。
 * Profile service interface, providing access and management of player profiles.
 */
public interface ProfileService extends Service {
    /**
     * Gets a player's profile by their UUID.
     * 根据 UUID 获取玩家的档案。
     * <p>
     * This method features lazy-loading: if the profile is not found in the cache,
     * it will be loaded from the database on-demand.
     * 此方法支持延迟加载：如果在缓存中未找到该档案，则会按需从数据库加载。
     *
     * @param uuid The UUID of the player.
     *             玩家的 UUID。
     * @return The player's Profile object.
     *         玩家的 Profile 对象。
     */
    Profile getProfile(UUID uuid);

    /**
     * Gets the Data Access Object (DAO) responsible for database operations for profiles.
     * This is used internally to load and save individual profiles.
     * 获取负责玩家档案数据库操作的数据访问对象（DAO）。
     * 此对象在内部用于加载和保存单个玩家档案。
     *
     * @return The DatabaseProfile DAO instance.
     *         DatabaseProfile DAO 实例。
     */
    DatabaseProfile getDatabaseProfile();

    /**
     * Gets the raw MongoDB collection for profiles.
     * 获取玩家档案的原始 MongoDB 集合。
     * <p>
     * Warning: Use with caution. Interacting with this collection directly bypasses
     * the caching and management logic of this service. It is intended for services
     * that need to perform complex, custom queries.
     * 警告：请谨慎使用。直接与此集合交互会绕过此服务的缓存和管理逻辑。
     * 它适用于需要执行复杂自定义查询的服务。
     *
     * @return The MongoCollection for profiles.
     *         玩家档案的 MongoCollection。
     */
    MongoCollection<Document> getCollection();

    /**
     * Gets the map of all currently cached profiles.
     * 获取当前所有已缓存玩家档案的映射。
     *
     * @return A map of UUIDs to Profile objects.
     *         UUID 到 Profile 对象的映射。
     */
    Map<UUID, Profile> getProfiles();

    /**
     * Removes a profile from the in-memory cache.
     * 从内存缓存中移除玩家档案。
     * <p>
     * This does not delete the profile from the database; it only removes it from the cache.
     * 这不会从数据库中删除该档案，仅将其从缓存中移除。
     *
     * @param uuid The UUID of the profile to remove.
     *             要移除的玩家档案的 UUID。
     */
    void removeProfile(UUID uuid);

    /**
     * Resets the statistics for a target player and archives their old profile.
     * 重置目标玩家的统计数据并归档其旧档案。
     *
     * @param player The staff member issuing the command.
     *               执行该命令的管理员。
     * @param target The UUID of the player whose stats are being reset.
     *               被重置统计数据的玩家的 UUID。
     */
    void resetStats(Player player, UUID target);

    /**
     * Resets the inventory layout for a specific kit across all player profiles.
     * 重置所有玩家档案中特定套件的物品栏布局。
     *
     * @param kit The kit to reset the layout for.
     *            要重置布局的套件。
     */
    void resetLayoutForKit(Kit kit);

    /**
     * Removes all saved inventory layouts for a specific kit from all player profiles.
     * 从所有玩家档案中移除指定套件的已保存物品栏布局。
     *
     * @param kit The kit whose layouts should be removed.
     *            要移除布局的套件。
     */
    void clearLayoutsForKit(Kit kit);
}
