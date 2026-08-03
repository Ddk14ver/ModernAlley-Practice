package dev.revere.alley.core.database;

import com.mongodb.client.MongoDatabase;
import dev.revere.alley.bootstrap.lifecycle.Service;

/**
 * @author Remi
 * @project alley-practice
 * @date 2/07/2025
 *
 * MongoDB 服务接口，提供对其他服务可用的 MongoDB 数据库实例。
 * MongoDB service interface, providing the MongoDB database instance for other services to use.
 */
public interface MongoService extends Service {
    /**
     * Gets the active MongoDatabase instance for other services to use.
     *
     * 获取活动的 MongoDatabase 实例供其他服务使用。
     *
     * @return The MongoDatabase instance.
     *         MongoDatabase 实例。
     * @throws IllegalStateException if the service has not been initialized yet.
     *                               如果服务尚未初始化。
     */
    MongoDatabase getMongoDatabase();
}