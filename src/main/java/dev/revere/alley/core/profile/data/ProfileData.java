package dev.revere.alley.core.profile.data;

import com.google.common.collect.Maps;
import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.feature.kit.KitService;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.kit.setting.types.mode.KitSettingRanked;
import dev.revere.alley.feature.level.LevelService;
import dev.revere.alley.feature.title.TitleService;
import dev.revere.alley.feature.title.model.TitleRecord;
import dev.revere.alley.feature.match.data.MatchData;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.data.types.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Emmy
 * @project Alley
 * @date 21/05/2024 - 22:03
 */
@Getter
@Setter
public class ProfileData {
    private Map<String, ProfileUnrankedKitData> unrankedKitData;
    private Map<String, ProfileRankedKitData> rankedKitData;
    private Map<String, ProfileFFAData> ffaData;

    /** Tournament wins per kit — counts toward division progression for that kit. */
    private Map<String, Integer> tournamentKitWins;

    private ProfileLayoutData layoutData;
    private ProfileSettingData settingData;
    private ProfileCosmeticData cosmeticData;
    private ProfilePlayTimeData playTimeData;
    private ProfileMusicData musicData;
    private ProfileChallengeData challengeData;

    private List<MatchData> previousMatches;

    private List<String> unlockedTitles;

    private String selectedTitle = "";
    private String globalLevel = "";

    private int elo = 1000;
    private int coins = 100;
    private int unrankedWins = 0;
    private int unrankedLosses = 0;
    private int rankedWins = 0;
    private int rankedLosses = 0;

    private boolean rankedBanned = false;
    private long rankedBanExpiry = 0L;
    private String rankedBanReason = "";
    private String rankedBanId = "";

    private int tournamentWins = 0;
    private int tournamentLosses = 0;
    private boolean tournamentParticipated = false;

    /** Returns true if the ban is active (not expired). */
    public boolean isRankedBanned() {
        if (!rankedBanned) return false;
        if (rankedBanExpiry > 0L && System.currentTimeMillis() > rankedBanExpiry) {
            rankedBanned = false;
            rankedBanExpiry = 0L;
            rankedBanReason = "";
            rankedBanId = "";
            return false;
        }
        return true;
    }

    public ProfileData() {
        this.initializeMaps();
        this.feedDataClasses();
        this.initializeDataClasses();
        this.previousMatches = new ArrayList<>();
        this.unlockedTitles = new ArrayList<>();
    }

    private void initializeDataClasses() {
        this.settingData = new ProfileSettingData();
        this.cosmeticData = new ProfileCosmeticData();
        this.playTimeData = new ProfilePlayTimeData();
        this.layoutData = new ProfileLayoutData();
        this.musicData = new ProfileMusicData();
        this.challengeData = new ProfileChallengeData();
    }

    private void feedDataClasses() {
        ensureKitData();
    }

    /**
     * Ensures all three kit data maps have entries for every kit currently loaded.
     * Safe to call repeatedly — only adds missing entries.
     */
    public void ensureKitData() {
        KitService kitService = AlleyPlugin.getInstance().getService(KitService.class);
        for (Kit kit : kitService.getKits()) {
            this.rankedKitData.putIfAbsent(kit.getName(), new ProfileRankedKitData());
            this.unrankedKitData.putIfAbsent(kit.getName(), new ProfileUnrankedKitData());
            this.ffaData.putIfAbsent(kit.getName(), new ProfileFFAData());
        }
    }

    private void initializeMaps() {
        this.unrankedKitData = Maps.newHashMap();
        this.rankedKitData = Maps.newHashMap();
        this.ffaData = Maps.newHashMap();
        this.tournamentKitWins = Maps.newHashMap();
    }

    /**
     * Calculates the global elo of the player
     * 计算玩家的全局ELO分数。
     *
     * @param profile the profile of the player
     *               玩家的资料。
     * @return the global elo of the player
     *         玩家的全局ELO分数。
     */
    private int calculateGlobalElo(Profile profile) {
        KitService kitService = AlleyPlugin.getInstance().getService(KitService.class);
        List<Kit> rankedKits = kitService.getKits().stream()
                .filter(kit -> kit.isSettingEnabled(KitSettingRanked.class))
                .collect(Collectors.toList());

        if (rankedKits.isEmpty()) {
            return 0;
        }

        int totalElo = rankedKits.stream()
                .mapToInt(kit -> {
                    ProfileRankedKitData kitData = profile.getProfileData().getRankedKitData().get(kit.getName());
                    return kitData != null ? kitData.getElo() : 0;
                })
                .sum();

        return totalElo / rankedKits.size();
    }

    public void determineTitles() {
        TitleService titleService = AlleyPlugin.getInstance().getService(TitleService.class);

        for (TitleRecord title : titleService.getTitles().values()) {
            if (title.getKit() == null) continue;
            var kitData = this.unrankedKitData.get(title.getKit().getName());
            if (kitData != null && kitData.getDivision() == title.getRequiredDivision()) {
                if (!this.unlockedTitles.contains(title.getName())) {
                    this.unlockedTitles.add(title.getName());
                }
            }
        }
    }

    public void determineLevel() {
        LevelService levelService = AlleyPlugin.getInstance().getService(LevelService.class);
        this.globalLevel = levelService.getLevel(this.elo).getName();
    }

    /**
     * Updates the elo of the player.
     * The level-up announcement itself is handled by the match-end flow (see DefaultMatch).
     * 更新玩家的ELO分数。
     * 升级公告本身由对局结束流程处理（见 DefaultMatch）。
     *
     * @param profile the profile of the player
     *               玩家的资料。
     */
    public void updateElo(Profile profile) {
        this.elo = this.calculateGlobalElo(profile);
    }

    /**
     * Gets the total number of wins a player has for a given kit across every mode
     * (unranked solo/duo, ranked and tournament). This total drives division progression.
     * 获取玩家在指定套件下所有模式（非排位单打/双打、排位、锦标赛）的总胜场数。
     * 该总数用于驱动段位进度。
     *
     * @param kitName The name of the kit.
     *                套件的名称。
     * @return The combined win count across all modes for the kit.
     *         该套件在所有模式下的胜场总数。
     */
    public int getKitWins(String kitName) {
        ProfileUnrankedKitData unranked = this.unrankedKitData.get(kitName);
        int unrankedWins = unranked == null ? 0 : unranked.getWins();
        ProfileRankedKitData ranked = this.rankedKitData.get(kitName);
        int rankedWins = ranked == null ? 0 : ranked.getWins();
        int tournamentWins = this.tournamentKitWins.getOrDefault(kitName, 0);
        return unrankedWins + rankedWins + tournamentWins;
    }

    /**
     * Recomputes the stored division/tier of a kit from its combined win count.
     * Should be called after any win that affects the kit (unranked solo/duo, ranked, tournament).
     * 根据套件的总胜场数重新计算存储的段位/层级。
     * 应在任何影响该套件的胜利之后调用（非排位单打/双打、排位、锦标赛）。
     *
     * @param kitName The name of the kit.
     *                套件的名称。
     */
    public void refreshDivision(String kitName) {
        ProfileUnrankedKitData kitData = this.unrankedKitData.get(kitName);
        if (kitData != null) {
            kitData.determineDivision(this.getKitWins(kitName));
        }
    }

    /**
     * Recomputes the stored division/tier for every kit from combined win counts.
     * Used when a profile is loaded so the stored division reflects all modes immediately.
     * 在加载玩家资料时，为所有套件重新计算段位/层级。
     *
     */
    public void refreshAllDivisions() {
        this.unrankedKitData.keySet().forEach(this::refreshDivision);
    }

    /**
     * Records a tournament win for a kit and refreshes that kit's division.
     * 记录某套件的锦标赛胜场，并刷新该套件的段位。
     *
     * @param kitName The name of the kit the tournament was played with.
     *                锦标赛所使用的套件名称。
     */
    public void incrementTournamentKitWins(String kitName) {
        this.tournamentKitWins.merge(kitName, 1, Integer::sum);
        this.refreshDivision(kitName);
    }

    /**
     * Get the total amount of wins
     * 获取总胜场数。
     *
     * @return The total amount of wins
     *         总胜场数。
     */
    public int getTotalWins() {
        return this.rankedWins + this.unrankedWins;
    }

    /**
     * Get the total amount of losses
     * 获取总败场数。
     *
     * @return The total amount of losses
     *         总败场数。
     */
    public int getTotalLosses() {
        return this.rankedLosses + this.unrankedLosses;
    }

    /**
     * Get the total amount of kills of the player ffa data.
     * 获取玩家FFA数据中的总击杀数。
     *
     * @return The total amount of kills
     *         总击杀数。
     */
    public int getTotalFFAKills() {
        return this.ffaData.values().stream().mapToInt(ProfileFFAData::getKills).sum();
    }

    /**
     * Get the total amount of deaths of the player ffa data.
     * 获取玩家FFA数据中的总死亡数。
     *
     * @return The total amount of deaths
     *         总死亡数。
     */
    public int getTotalFFADeaths() {
        return this.ffaData.values().stream().mapToInt(ProfileFFAData::getDeaths).sum();
    }

    public void incrementUnrankedWins() {
        this.unrankedWins++;
    }

    public void incrementUnrankedLosses() {
        this.unrankedLosses++;
    }

    public void incrementRankedWins() {
        this.rankedWins++;
    }

    public void incrementRankedLosses() {
        this.rankedLosses++;
    }

    /**
     * Increments the player's coins by the specified amount.
     * 按指定数量增加玩家的金币。
     *
     * @param amount The amount of coins to add.
     *               要添加的金币数量。
     */
    public void incrementCoins(int amount) {
        this.coins += amount;
    }
}
