package dev.revere.alley.core.database.internal;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import dev.revere.alley.bootstrap.AlleyContext;
import dev.revere.alley.bootstrap.annotation.Service;
import dev.revere.alley.common.logger.Logger;
import dev.revere.alley.core.database.MongoService;
import dev.revere.alley.core.locale.LocaleService;
import dev.revere.alley.core.locale.internal.impl.SettingsLocaleImpl;
import lombok.Getter;

/**
 * @author Emmy
 * @project Alley
 * @date 21/05/2024 - 21:40
 *
 * MongoDB 服务的实现类，负责建立和管理与 MongoDB 的连接。
 * Implementation of the MongoDB service, responsible for establishing and managing the MongoDB connection.
 */
@Getter
@Service(provides = MongoService.class, priority = 30)
public class MongoServiceImpl implements MongoService {
    private final LocaleService localeService;

    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;

    /**
     * DI Constructor for the MongoServiceImpl class.
     *
     * MongoServiceImpl 类的依赖注入构造函数。
     *
     * @param localeService The locale service.
     *                      国际化/本地化服务。
     */
    public MongoServiceImpl(LocaleService localeService) {
        this.localeService = localeService;
    }

    @Override
    public void initialize(AlleyContext context) {
        String uri = this.localeService.getString(SettingsLocaleImpl.MONGO_CREDENTIALS_URI);
        String databaseName = this.localeService.getString(SettingsLocaleImpl.MONGO_CREDENTIALS_DATABASE);

        if (uri == null || uri.isEmpty() || databaseName == null || databaseName.isEmpty()) {
            Logger.error("MongoDB URI or database name is not configured in database.yml.");
            throw new IllegalStateException("MongoDB configuration is missing.");
        }

        try {
            ConnectionString connectionString = new ConnectionString(uri);
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(connectionString)
                    .retryWrites(true)
                    .build();

            this.mongoClient = MongoClients.create(settings);
            this.mongoDatabase = this.mongoClient.getDatabase(databaseName);

            this.mongoDatabase.listCollectionNames().first();
        } catch (Exception e) {
            Logger.error("Failed to connect to MongoDB. Please check your credentials and network access.");
            throw new RuntimeException("MongoDB Connection Failure", e);
        }
    }

    @Override
    public void shutdown(AlleyContext context) {
        if (this.mongoClient != null) {
            this.mongoClient.close();
            Logger.info("MongoDB connection closed.");
        }
    }
}