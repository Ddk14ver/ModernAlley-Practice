package dev.revere.alley.feature.leaderboard.internal;

import com.mongodb.client.MongoCollection;
import dev.revere.alley.feature.kit.KitService;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.bootstrap.AlleyContext;
import dev.revere.alley.bootstrap.annotation.Service;
import dev.revere.alley.core.database.MongoService;
import dev.revere.alley.core.config.ConfigService;
import dev.revere.alley.feature.leaderboard.LeaderboardService;
import dev.revere.alley.feature.leaderboard.data.LeaderboardPlayerData;
import dev.revere.alley.feature.leaderboard.LeaderboardType;
import dev.revere.alley.feature.leaderboard.model.LeaderboardRecord;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.data.ProfileData;
import dev.revere.alley.core.profile.data.types.ProfileFFAData;
import dev.revere.alley.core.profile.data.types.ProfileRankedKitData;
import dev.revere.alley.core.profile.data.types.ProfileUnrankedKitData;
import dev.revere.alley.common.logger.Logger;
import lombok.Getter;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.time.DateTimeException;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * 排行榜服务的实现类，负责通过 MongoDB 聚合计算和管理排行榜数据。
 * Implementation of the leaderboard service, responsible for calculating and managing leaderboard data via MongoDB aggregation.
 *
 * @author Emmy
 * @project Alley
 * @since 03/03/2025
 */
@Getter
@Service(provides = LeaderboardService.class, priority = 280)
public class LeaderboardServiceImpl implements LeaderboardService {
    private final MongoService mongoService;
    private final KitService kitService;
    private final ProfileService profileService;
    private final ConfigService configService;
    private final ExecutorService executorService;

    private final Map<Kit, List<LeaderboardRecord>> leaderboardCache = new ConcurrentHashMap<>();
    private final Map<String, Integer> onlinePlayerCache = new ConcurrentHashMap<>();
    private final AtomicBoolean recalculating = new AtomicBoolean();
    private volatile ZoneId monthlyZoneId = ZoneId.systemDefault();
    private volatile String cachedMonthKey = "";
    private BukkitTask updateTask;

    /**
     * Constructor for DI.
     * 依赖注入构造方法。
     */
    public LeaderboardServiceImpl(MongoService mongoService, KitService kitService, ProfileService profileService,
                                  ConfigService configService) {
        this.mongoService = mongoService;
        this.kitService = kitService;
        this.profileService = profileService;
        this.configService = configService;
        this.executorService = Executors.newFixedThreadPool(4);
    }

    @Override
    public void initialize(AlleyContext context) {
        this.monthlyZoneId = resolveMonthlyZoneId();
        this.forceRecalculateAll();

        long refreshMinutes = Math.max(1L,
                this.configService.getSettingsConfig().getLong("leaderboards.database-refresh-minutes", 5L));
        long refreshTicks = refreshMinutes * 60L * 20L;
        this.updateTask = new LeaderboardUpdateTask(this)
                .runTaskTimerAsynchronously(context.getPlugin(), refreshTicks, refreshTicks);
    }

    @Override
    public void shutdown(AlleyContext context) {
        if (this.updateTask != null) {
            this.updateTask.cancel();
            this.updateTask = null;
        }
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            Logger.info("Leaderboard executor service has been shut down.");
        }
    }

    @Override
    public void forceRecalculateAll() {
        if (!this.recalculating.compareAndSet(false, true)) return;

        this.monthlyZoneId = resolveMonthlyZoneId();
        MongoCollection<Document> profileCollection = this.mongoService.getMongoDatabase().getCollection("profiles");
        String calculationMonthKey = currentMonthKey();
        try {
            CompletableFuture<?>[] calculations = this.kitService.getKits().stream()
                    .map(kit -> CompletableFuture.runAsync(
                            () -> calculateLeaderboardForKit(kit, profileCollection), executorService))
                    .toArray(CompletableFuture[]::new);

            // Do not join here: initialize() and the admin reload menu can call
            // this method on the server thread, while every calculation performs
            // synchronous MongoDB I/O. Publish the cache when all workers finish.
            CompletableFuture.allOf(calculations).whenComplete((ignored, throwable) -> {
                try {
                    if (throwable == null) {
                        this.cachedMonthKey = calculationMonthKey;
                    } else {
                        Logger.error("Leaderboard recalculation failed: " + throwable.getMessage());
                    }
                } finally {
                    this.recalculating.set(false);
                }
            });
        } catch (RuntimeException exception) {
            this.recalculating.set(false);
            throw exception;
        }
    }

    private void calculateLeaderboardForKit(Kit kit, MongoCollection<Document> profileCollection) {
        List<LeaderboardRecord> records = Collections.synchronizedList(new ArrayList<>());

        for (LeaderboardType type : LeaderboardType.values()) {
            List<LeaderboardPlayerData> playerDataList = fetchOptimizedLeaderboard(profileCollection, kit, type);
            records.add(new LeaderboardRecord(type, playerDataList));
        }

        this.leaderboardCache.put(kit, records);
    }

    private List<LeaderboardPlayerData> fetchOptimizedLeaderboard(MongoCollection<Document> profileCollection, Kit kit, LeaderboardType type) {
        List<LeaderboardPlayerData> playerDataList = new ArrayList<>();

        List<Document> pipeline = buildAggregationPipeline(kit, type);

        for (Document doc : profileCollection.aggregate(pipeline)) {
            try {
                String name = doc.getString("name");
                UUID uuid = UUID.fromString(doc.getString("uuid"));
                int value = doc.getInteger("value", 0);

                if (value > 0 || type == LeaderboardType.RANKED) {
                    playerDataList.add(new LeaderboardPlayerData(name, uuid, kit, value));
                }
            } catch (Exception ignored) {
            }
        }

        return playerDataList;
    }

    private List<Document> buildAggregationPipeline(Kit kit, LeaderboardType type) {
        List<Document> pipeline = new ArrayList<>();

        Document projectStage = new Document("$project", new Document()
                .append("uuid", 1)
                .append("name", 1)
                .append("value", buildValueExtraction(kit, type)));

        pipeline.add(projectStage);

        if (type != LeaderboardType.RANKED) {
            pipeline.add(new Document("$match", new Document("value", new Document("$gt", 0))));
        }

        pipeline.add(new Document("$sort", new Document("value", -1)));

        pipeline.add(new Document("$limit", 100));

        return pipeline;
    }

    private Document buildValueExtraction(Kit kit, LeaderboardType type) {
        String kitName = kit.getName();

        switch (type) {
            case RANKED:
                return new Document("$ifNull", Arrays.asList(
                        new Document("$getField", new Document()
                                .append("field", "elo")
                                .append("input", new Document("$getField", new Document()
                                        .append("field", kitName)
                                        .append("input", "$profileData.rankedKitData")))),
                        1000
                ));
            case UNRANKED:
                return new Document("$ifNull", Arrays.asList(
                        new Document("$getField", new Document()
                                .append("field", "wins")
                                .append("input", new Document("$getField", new Document()
                                        .append("field", kitName)
                                        .append("input", "$profileData.unrankedKitData")))),
                        0
                ));
            case UNRANKED_MONTHLY:
                Document monthlyKitData = new Document("$getField", new Document()
                        .append("field", kitName)
                        .append("input", "$profileData.unrankedKitData"));
                Document monthlyPeriod = new Document("$ifNull", Arrays.asList(
                        new Document("$getField", new Document()
                                .append("field", "monthlyPeriodKey")
                                .append("input", monthlyKitData)),
                        ""
                ));
                Document monthlyWins = new Document("$ifNull", Arrays.asList(
                        new Document("$getField", new Document()
                                .append("field", "monthlyWins")
                                .append("input", monthlyKitData)),
                        0
                ));
                return new Document("$cond", Arrays.asList(
                        new Document("$eq", Arrays.asList(monthlyPeriod, currentMonthKey())),
                        monthlyWins,
                        0
                ));
            case FFA:
                return new Document("$ifNull", Arrays.asList(
                        new Document("$getField", new Document()
                                .append("field", "kills")
                                .append("input", new Document("$getField", new Document()
                                        .append("field", kitName)
                                        .append("input", "$profileData.ffaData")))),
                        0
                ));
            case WIN_STREAK:
                return new Document("$ifNull", Arrays.asList(
                        new Document("$getField", new Document()
                                .append("field", "winstreak")
                                .append("input", new Document("$getField", new Document()
                                        .append("field", kitName)
                                        .append("input", "$profileData.unrankedKitData")))),
                        0
                ));
            default:
                return new Document("$literal", 0);
        }
    }

    @Override
    public List<LeaderboardPlayerData> getLeaderboardEntries(Kit kit, LeaderboardType type) {
        if (type == LeaderboardType.UNRANKED_MONTHLY && !currentMonthKey().equals(this.cachedMonthKey)) {
            invalidateExpiredMonthlyCache();
        }
        this.refreshOnlinePlayersOptimized(kit, type);

        return this.leaderboardCache.getOrDefault(kit, Collections.emptyList())
                .stream()
                .filter(record -> record.getType() == type)
                .findFirst()
                .map(LeaderboardRecord::getParticipants)
                .orElse(Collections.emptyList());
    }

    private void refreshOnlinePlayersOptimized(Kit kit, LeaderboardType type) {
        LeaderboardRecord record = this.leaderboardCache.getOrDefault(kit, Collections.emptyList())
                .stream()
                .filter(r -> r.getType() == type)
                .findFirst()
                .orElse(null);

        if (record == null) return;

        List<LeaderboardPlayerData> leaderboard = record.getParticipants();

        Map<UUID, Integer> onlinePlayerUpdates = new HashMap<>();

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            Profile profile = this.profileService.getProfile(onlinePlayer.getUniqueId());
            if (profile != null) {
                int newValue = getValueForType(profile, kit, type);
                onlinePlayerUpdates.put(profile.getUuid(), newValue);
            }
        }

        for (LeaderboardPlayerData playerData : leaderboard) {
            Integer newValue = onlinePlayerUpdates.get(playerData.getUuid());
            if (newValue != null) {
                playerData.setValue(newValue);
            }
        }

        if (type != LeaderboardType.RANKED) {
            leaderboard.removeIf(playerData -> playerData.getValue() <= 0);
        }

        Set<UUID> leaderboardUuids = leaderboard.stream()
                .map(LeaderboardPlayerData::getUuid)
                .collect(Collectors.toSet());

        for (Map.Entry<UUID, Integer> entry : onlinePlayerUpdates.entrySet()) {
            if (!leaderboardUuids.contains(entry.getKey())
                    && (entry.getValue() > 0 || type == LeaderboardType.RANKED)) {
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player != null) {
                    leaderboard.add(new LeaderboardPlayerData(player.getName(), entry.getKey(), kit, entry.getValue()));
                }
            }
        }

        leaderboard.sort(Comparator.comparingInt(LeaderboardPlayerData::getValue).reversed());
    }

    private int getValueForType(Profile profile, Kit kit, LeaderboardType type) {
        ProfileData data = profile.getProfileData();
        switch (type) {
            case RANKED:
                return data.getRankedKitData().getOrDefault(kit.getName(), new ProfileRankedKitData()).getElo();
            case UNRANKED:
                return data.getUnrankedKitData().getOrDefault(kit.getName(), new ProfileUnrankedKitData()).getWins();
            case UNRANKED_MONTHLY:
                return data.getUnrankedKitData().getOrDefault(kit.getName(), new ProfileUnrankedKitData())
                        .getMonthlyWins(currentMonthKey());
            case FFA:
                return data.getFfaData().getOrDefault(kit.getName(), new ProfileFFAData()).getKills();
            case WIN_STREAK:
                return data.getUnrankedKitData().getOrDefault(kit.getName(), new ProfileUnrankedKitData()).getWinstreak();
            default:
                return 0;
        }
    }

    @Override
    public void recordMonthlyUnrankedWin(Profile profile, Kit kit) {
        if (profile == null || kit == null || profile.getProfileData() == null) return;
        ProfileUnrankedKitData kitData = profile.getProfileData().getUnrankedKitData().get(kit.getName());
        if (kitData != null) {
            kitData.incrementMonthlyWins(currentMonthKey());
        }
    }

    private ZoneId resolveMonthlyZoneId() {
        String fallback = ZoneId.systemDefault().getId();
        String configured = this.configService.getSettingsConfig()
                .getString("leaderboards.timezone", fallback);
        try {
            return ZoneId.of(configured == null || configured.isBlank() ? fallback : configured);
        } catch (DateTimeException exception) {
            Logger.warn("Invalid leaderboard timezone '" + configured + "'; using " + fallback + ".");
            return ZoneId.systemDefault();
        }
    }

    private String currentMonthKey() {
        return YearMonth.now(this.monthlyZoneId).toString();
    }

    private synchronized void invalidateExpiredMonthlyCache() {
        String currentKey = currentMonthKey();
        if (currentKey.equals(this.cachedMonthKey)) return;

        this.cachedMonthKey = currentKey;
        this.leaderboardCache.values().forEach(records -> records.stream()
                .filter(record -> record.getType() == LeaderboardType.UNRANKED_MONTHLY)
                .forEach(record -> record.getParticipants().clear()));
        forceRecalculateAll();
    }

    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
