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
import dev.revere.alley.common.text.CC;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Arrays;
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

    private ProfileLayoutData layoutData;
    private ProfileSettingData settingData;
    private ProfileCosmeticData cosmeticData;
    private ProfilePlayTimeData playTimeData;
    private ProfileMusicData musicData;

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
     * Updates the elo and division of the player
     * 更新玩家的ELO分数和段位。
     *
     * @param profile the profile of the player
     *               玩家的资料。
     */
    public void updateElo(Profile profile) {
        int previousElo = this.elo;
        LevelService levelService = AlleyPlugin.getInstance().getService(LevelService.class);
        String previousLevel = levelService.getLevel(previousElo).getName();

        this.elo = this.calculateGlobalElo(profile);
        String newLevel = levelService.getLevel(this.elo).getName();

        if (!newLevel.equals(previousLevel)) {
            this.sendLevelUpMessage(profile, newLevel);
        }
    }

    /**
     * Sends a level up message to the player
     * 向玩家发送升级消息。
     *
     * @param profile  the profile of the player
     *                 玩家的资料。
     * @param newLevel the new level of the player
     *                 玩家的新等级。
     */
    private void sendLevelUpMessage(Profile profile, String newLevel) {
        Arrays.asList(
                "",
                "&6&lNEW LEVEL &f| &a&lCONGRATULATIONS!",
                " &fYou have reached &6" + newLevel + " &fin the global ranking system.",
                ""
        ).forEach(line -> Bukkit.getPlayer(profile.getUuid()).sendMessage(CC.translate(line)));
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
