package dev.revere.alley.feature.challenge.internal;

import dev.revere.alley.bootstrap.AlleyContext;
import dev.revere.alley.bootstrap.annotation.Service;
import dev.revere.alley.common.logger.Logger;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.config.ConfigService;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.data.types.ProfileChallengeData;
import dev.revere.alley.core.profile.data.types.ProfileChallengeProgress;
import dev.revere.alley.feature.challenge.ChallengeDefinition;
import dev.revere.alley.feature.challenge.ChallengePeriod;
import dev.revere.alley.feature.challenge.ChallengeService;
import dev.revere.alley.feature.challenge.ChallengeType;
import dev.revere.alley.feature.coin.CoinRewardService;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
/**
 * @author Ddk1\5.6sol
 * @project Alley
 * @date 14/09/2024 - 23:03
 */
@Service(provides = ChallengeService.class, priority = 350)
public class ChallengeServiceImpl implements ChallengeService {
    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Hong_Kong");

    private final ConfigService configService;
    private final CoinRewardService coinRewardService;
    private final Map<ChallengePeriod, Map<ChallengeType, ChallengeDefinition>> definitions =
            new EnumMap<>(ChallengePeriod.class);
    private final Set<UUID> deferredCompletionMessages = new HashSet<>();
    private final Map<UUID, List<PendingCompletion>> pendingCompletionMessages = new HashMap<>();

    private ZoneId zoneId = DEFAULT_ZONE;
    private Clock clock = Clock.system(DEFAULT_ZONE);

    public ChallengeServiceImpl(ConfigService configService, CoinRewardService coinRewardService) {
        this.configService = configService;
        this.coinRewardService = coinRewardService;
    }

    @Override
    public void initialize(AlleyContext context) {
        reloadDefinitions();
    }

    @Override
    public void reloadDefinitions() {
        FileConfiguration config = this.configService.getChallengesConfig();
        String configuredZone = config.getString("timezone", DEFAULT_ZONE.getId());

        try {
            this.zoneId = ZoneId.of(configuredZone == null ? DEFAULT_ZONE.getId() : configuredZone);
        } catch (Exception exception) {
            this.zoneId = DEFAULT_ZONE;
            Logger.warn("Invalid challenge timezone '" + configuredZone + "'. Using " + DEFAULT_ZONE.getId() + ".");
        }
        this.clock = Clock.system(this.zoneId);

        this.definitions.clear();
        for (ChallengePeriod period : ChallengePeriod.values()) {
            Map<ChallengeType, ChallengeDefinition> periodDefinitions = new EnumMap<>(ChallengeType.class);
            for (ChallengeType type : ChallengeType.values()) {
                String path = "tasks." + period.getConfigKey() + "." + type.getConfigKey();
                int defaultRequirement = defaultRequirement(period, type);
                int defaultReward = period == ChallengePeriod.DAILY ? 100 : 500;
                int requirement = config.getInt(path + ".requirement", defaultRequirement);
                int reward = config.getInt(path + ".reward-coins", defaultReward);

                if (requirement <= 0) {
                    Logger.warn("Challenge requirement at " + path + " must be positive. Using " + defaultRequirement + ".");
                    requirement = defaultRequirement;
                }
                if (reward < 0) {
                    Logger.warn("Challenge reward at " + path + " cannot be negative. Using " + defaultReward + ".");
                    reward = defaultReward;
                }

                String defaultName = "&6&l" + period.getDisplayName() + " " + type.getUnitName() + " Challenge";
                String displayName = config.getString(path + ".name", defaultName);
                periodDefinitions.put(type, new ChallengeDefinition(period, type, displayName,
                        requirement, reward));
            }
            this.definitions.put(period, periodDefinitions);
        }
    }

    @Override
    public boolean accept(Profile profile, ChallengePeriod period, ChallengeType type) {
        synchronizePeriod(profile);
        ProfileChallengeProgress progress = getProgress(profile, period, type);
        if (progress.isAccepted()) {
            return false;
        }

        progress.setAccepted(true);
        profile.save();
        return true;
    }

    @Override
    public void recordProgress(Profile profile, ChallengeType type, int amount) {
        if (profile == null || amount <= 0) {
            return;
        }

        synchronizePeriod(profile);
        boolean completedAny = false;
        for (ChallengePeriod period : ChallengePeriod.values()) {
            ChallengeDefinition definition = getDefinition(period, type);
            ProfileChallengeProgress progress = getProgress(profile, period, type);
            if (!progress.isAccepted() || progress.isCompleted()) {
                continue;
            }

            int updated = Math.min(definition.getRequirement(), progress.getProgress() + amount);
            progress.setProgress(updated);
            if (updated >= definition.getRequirement()) {
                complete(profile, definition, progress);
                completedAny = true;
            }
        }

        if (completedAny) {
            profile.save();
        }
    }

    @Override
    public void deferCompletionMessages(Profile profile) {
        if (profile != null) {
            this.deferredCompletionMessages.add(profile.getUuid());
        }
    }

    @Override
    public void flushCompletionMessages(Profile profile) {
        if (profile == null) {
            return;
        }

        UUID uuid = profile.getUuid();
        this.deferredCompletionMessages.remove(uuid);
        List<PendingCompletion> pending = this.pendingCompletionMessages.remove(uuid);
        if (pending == null || pending.isEmpty()) {
            return;
        }

        Player player = Bukkit.getPlayer(uuid);
        if (player == null || !player.isOnline()) {
            return;
        }

        for (PendingCompletion completion : pending) {
            this.sendCompletionMessage(player, completion.definition(), completion.progress());
        }
    }

    @Override
    public void synchronizePeriod(Profile profile) {
        ProfileChallengeData data = profile.getProfileData().getChallengeData();
        boolean changed = false;

        String dailyKey = currentDailyKey();
        if (!dailyKey.equals(data.getDailyPeriodKey())) {
            data.clear(ChallengePeriod.DAILY);
            data.setDailyPeriodKey(dailyKey);
            changed = true;
        }

        String weeklyKey = currentWeeklyKey();
        if (!weeklyKey.equals(data.getWeeklyPeriodKey())) {
            data.clear(ChallengePeriod.WEEKLY);
            data.setWeeklyPeriodKey(weeklyKey);
            changed = true;
        }

        if (changed) {
            profile.save();
        }
    }

    @Override
    public ChallengeDefinition getDefinition(ChallengePeriod period, ChallengeType type) {
        return this.definitions.get(period).get(type);
    }

    @Override
    public ProfileChallengeProgress getProgress(Profile profile, ChallengePeriod period, ChallengeType type) {
        return profile.getProfileData().getChallengeData().getOrCreate(period, type);
    }

    @Override
    public long getSecondsUntilReset(ChallengePeriod period) {
        ZonedDateTime now = ZonedDateTime.now(this.clock);
        ZonedDateTime reset;
        if (period == ChallengePeriod.DAILY) {
            reset = now.toLocalDate().plusDays(1).atStartOfDay(this.zoneId);
        } else {
            long daysUntilNextMonday = 8L - now.getDayOfWeek().getValue();
            reset = now.toLocalDate().plusDays(daysUntilNextMonday).atStartOfDay(this.zoneId);
        }
        return Math.max(0L, Duration.between(now, reset).getSeconds());
    }

    private void complete(Profile profile, ChallengeDefinition definition, ProfileChallengeProgress progress) {
        progress.setCompleted(true);
        if (progress.isRewarded()) {
            return;
        }

        Player player = Bukkit.getPlayer(profile.getUuid());
        if (player != null) {
            this.coinRewardService.rewardChallenge(player, definition.getRewardCoins());
            if (this.deferredCompletionMessages.contains(profile.getUuid())) {
                this.pendingCompletionMessages
                        .computeIfAbsent(profile.getUuid(), ignored -> new ArrayList<>())
                        .add(new PendingCompletion(definition, progress));
            } else {
                sendCompletionMessage(player, definition, progress);
            }
        } else {
            profile.getProfileData().incrementCoins(definition.getRewardCoins());
        }
        progress.setRewarded(true);
    }

    private void sendCompletionMessage(Player player, ChallengeDefinition definition,
                                       ProfileChallengeProgress progress) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.5F, 1.15F);

        FileConfiguration config = this.configService.getChallengesConfig();
        List<String> lines = config.getStringList("messages.completed");
        if (lines.isEmpty()) {
            lines = List.of(
                    "",
                    "&6&lCHALLENGE COMPLETE &8| &f{task}",
                    "&7Progress: &a{progress}/{requirement} &8| &6+{reward} coins",
                    ""
            );
        }

        for (String line : lines) {
            player.sendMessage(CC.translate(replacePlaceholders(line, definition, progress)));
        }
    }

    private String replacePlaceholders(String text, ChallengeDefinition definition,
                                       ProfileChallengeProgress progress) {
        return text
                .replace("{period}", definition.getPeriod().getDisplayName())
                .replace("{task}", CC.translate(definition.getDisplayName()))
                .replace("{type}", definition.getType().getUnitName())
                .replace("{progress}", String.valueOf(progress.getProgress()))
                .replace("{requirement}", String.valueOf(definition.getRequirement()))
                .replace("{reward}", String.valueOf(definition.getRewardCoins()));
    }

    private String currentDailyKey() {
        return LocalDate.now(this.clock).toString();
    }

    private String currentWeeklyKey() {
        LocalDate date = LocalDate.now(this.clock);
        WeekFields fields = WeekFields.ISO;
        return date.get(fields.weekBasedYear()) + "-W" + String.format("%02d", date.get(fields.weekOfWeekBasedYear()));
    }

    private record PendingCompletion(ChallengeDefinition definition, ProfileChallengeProgress progress) {
    }

    private int defaultRequirement(ChallengePeriod period, ChallengeType type) {
        if (period == ChallengePeriod.DAILY) {
            return switch (type) {
                case KILLS -> 20;
                case WINS -> 5;
                case ELO -> 50;
            };
        }
        return switch (type) {
            case KILLS -> 100;
            case WINS -> 25;
            case ELO -> 250;
        };
    }
}
