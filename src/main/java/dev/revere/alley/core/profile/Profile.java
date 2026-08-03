package dev.revere.alley.core.profile;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.adapter.core.CoreAdapter;
import dev.revere.alley.core.profile.data.ProfileData;
import dev.revere.alley.core.profile.data.types.ProfileFFAData;
import dev.revere.alley.core.profile.data.types.ProfilePlayTimeData;
import dev.revere.alley.core.profile.data.types.ProfileRankedKitData;
import dev.revere.alley.core.profile.data.types.ProfileUnrankedKitData;
import dev.revere.alley.core.profile.enums.GlobalCooldown;
import dev.revere.alley.core.profile.enums.ProfileState;
import dev.revere.alley.feature.abilities.Ability;
import dev.revere.alley.feature.abilities.cooldown.AbilityCooldown;
import dev.revere.alley.feature.division.Division;
import dev.revere.alley.feature.division.DivisionService;
import dev.revere.alley.feature.division.model.DivisionTier;
import dev.revere.alley.feature.ffa.FFAMatch;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.kit.KitService;
import dev.revere.alley.feature.leaderboard.LeaderboardType;
import dev.revere.alley.feature.match.Match;
import dev.revere.alley.feature.clan.Clan;
import dev.revere.alley.feature.party.Party;
import dev.revere.alley.feature.queue.QueueProfile;
import dev.revere.alley.feature.queue.QueueType;
import dev.revere.alley.feature.tournament.model.Tournament;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Emmy
 * @project Alley
 * @date 19/05/2024 - 22:35
 * 玩家档案类，存储和管理玩家的所有数据。
 * Profile class, storing and managing all player data.
 */
@Getter
@Setter
public class Profile {
    private final UUID uuid;
    private String name;
    private long firstJoin;
    private boolean online;

    private ProfileData profileData;
    private QueueProfile queueProfile;
    private ProfileState state;

    private LeaderboardType leaderboardType;
    private QueueType queueType;

    private final Map<Class<? extends Ability>, AbilityCooldown> abilityCooldowns;
    private final Map<GlobalCooldown, AbilityCooldown> globalCooldowns;

    private Tournament tournament;
    private FFAMatch ffaMatch;
    private Match match;
    private Party party;
    private Clan clan;

    private ChatColor nameColor;

    /**
     * Constructor for the Profile class.
     * Profile 类的构造函数。
     *
     * @param uuid The UUID of the player.
     *             玩家的 UUID。
     * @param name The name of the player.
     *             玩家的名称。
     */
    public Profile(UUID uuid, String name) {
        this.uuid = uuid;
        this.firstJoin = System.currentTimeMillis();
        this.state = ProfileState.LOBBY;
        this.profileData = new ProfileData();
        this.name = name;
        this.leaderboardType = LeaderboardType.RANKED;
        this.queueType = QueueType.UNRANKED;
        this.nameColor = ChatColor.WHITE;

        this.abilityCooldowns = new HashMap<>();
        this.globalCooldowns = new EnumMap<>(GlobalCooldown.class);
    }

    /**
     * Advanced method to retrieve the player's current color.
     * Before accessing, check if the cached color is up to date. If not, re-assign it using the CoreAdapter.
     * Logic is in place to avoid unnecessary calls to the CoreAdapter.
     * 获取玩家当前颜色的高级方法。
     * 在访问之前，检查缓存的颜色是否是最新的。如果不是，则使用 CoreAdapter 重新分配。
     * 内置逻辑可避免对 CoreAdapter 的不必要调用。
     *
     * @return The ChatColor representing the player's name color.
     *         表示玩家名称颜色的 ChatColor。
     */
    public ChatColor getNameColor() {
        CoreAdapter adapter = AlleyPlugin.getInstance().getService(CoreAdapter.class);
        if (adapter == null) {
            return this.nameColor;
        }

        Player player = AlleyPlugin.getInstance().getServer().getPlayer(this.uuid);
        if (player == null) {
            return this.nameColor;
        }

        ChatColor upToDateColor = adapter.getCore().getPlayerColor(player);
        if (upToDateColor == null) {
            upToDateColor = this.nameColor;
        }

        if (this.nameColor != upToDateColor) {
            this.nameColor = upToDateColor;
        }

        return this.nameColor;
    }

    /**
     * Gets the fancy name of the profile with the color.
     * 获取带有颜色的玩家档案美化名称。
     *
     * @return The colored name of the profile.
     *         带有颜色的玩家档案名称。
     */
    public String getFancyName() {
        return this.nameColor + this.name;
    }

    /**
     * Checks if the profile is currently busy with a match or FFA.
     * 检查玩家当前是否正在比赛或 FFA 中。
     *
     * @return True if the profile is busy, otherwise false.
     *         如果玩家忙碌则返回 true，否则返回 false。
     */
    public boolean isBusy() {
        return this.state != ProfileState.LOBBY;
    }

    /**
     * Checks if the profile is currently in a tournament.
     * 检查玩家当前是否在锦标赛中。
     *
     * @return True if the profile is in a tournament, otherwise false.
     *         如果玩家在锦标赛中则返回 true，否则返回 false。
     */
    public boolean inTournament() {
        return this.tournament != null;
    }

    /**
     * Checks if the profile is in the lobby or in a queue.
     * 检查玩家是否在大厅或队列中。
     *
     * @return True if the profile is in the lobby or in a queue, otherwise false.
     *         如果玩家在大厅或队列中则返回 true，否则返回 false。
     */
    public boolean isInLobbyOrInQueue() {
        return this.state == ProfileState.LOBBY || this.state == ProfileState.WAITING;
    }

    /**
     * Loads the profile from the database.
     * 从数据库加载玩家档案。
     */
    public void load() {
        ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);
        profileService.getDatabaseProfile().loadProfile(this);
    }

    /**
     * Saves the profile to the database.
     * 将玩家档案保存到数据库。
     */
    public void save() {
        ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);
        profileService.getDatabaseProfile().saveProfile(this);
    }

    /**
     * Gets the cooldown object for a specific ability.
     * If a cooldown for this ability doesn't exist yet for this profile, it will be created.
     * 获取特定技能的冷却对象。
     * 如果此配置文件中尚不存在此技能的冷却，则会创建它。
     *
     * @param abilityClass The class of the ability (e.g., GuardianAngel.class).
     *                     技能的类（例如 GuardianAngel.class）。
     * @return The AbilityCooldown object for that ability.
     *         该技能的 AbilityCooldown 对象。
     */
    public AbilityCooldown getCooldown(Class<? extends Ability> abilityClass) {
        return this.abilityCooldowns.computeIfAbsent(abilityClass, key -> new AbilityCooldown());
    }

    /**
     * Gets the cooldown object for a specific global cooldown type.
     * 获取特定全局冷却类型的冷却对象。
     *
     * @param type The global cooldown type from the enum.
     *             枚举中的全局冷却类型。
     * @return The AbilityCooldown object.
     *         AbilityCooldown 对象。
     */
    public AbilityCooldown getGlobalCooldown(GlobalCooldown type) {
        return this.globalCooldowns.computeIfAbsent(type, key -> new AbilityCooldown());
    }

    /**
     * Retrieves a sorted list of kits that the profile has participated in
     * based on the profile's ELO for each kit, overall wins/losses and FFA kills/deaths.
     * 检索玩家已参与过的套件的排序列表，
     * 基于玩家每个套件的 ELO、总胜场/败场以及 FFA 击杀/死亡数。
     *
     * @return A sorted list of kits that the profile has participated in.
     *         玩家已参与过的套件的排序列表。
     */
    public List<Kit> getSortedKits() {
        KitService kitService = AlleyPlugin.getInstance().getService(KitService.class);
        return kitService.getKits()
                .stream()
                .filter(kit -> {
                    ProfileRankedKitData rankedData = this.profileData.getRankedKitData().get(kit.getName());
                    ProfileUnrankedKitData unrankedData = this.profileData.getUnrankedKitData().get(kit.getName());
                    ProfileFFAData ffaData = this.profileData.getFfaData().get(kit.getName());

                    return (rankedData != null && (rankedData.getWins() != 0 || rankedData.getLosses() != 0)) ||
                            (unrankedData != null && (unrankedData.getWins() != 0 || unrankedData.getLosses() != 0)) ||
                            (ffaData != null && (ffaData.getKills() != 0 || ffaData.getDeaths() != 0));
                })
                .sorted(Comparator.comparingInt((Kit kit) -> {
                            ProfileRankedKitData ranked = this.profileData.getRankedKitData().get(kit.getName());
                            return ranked != null ? ranked.getElo() : 0;
                        }).reversed()
                        .thenComparingInt(kit -> {
                            ProfileRankedKitData ranked = this.profileData.getRankedKitData().get(kit.getName());
                            return ranked != null ? ranked.getWins() : 0;
                        }).reversed()
                        .thenComparingInt(kit -> {
                            ProfileFFAData ffa = this.profileData.getFfaData().get(kit.getName());
                            return ffa != null ? ffa.getKills() : 0;
                        }).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Checks if the profile has participated in ranked matches.
     * 检查玩家是否参与过排位赛。
     *
     * @return True if the profile has participated in ranked matches, otherwise false.
     *         如果玩家参与过排位赛则返回 true，否则返回 false。
     */
    public boolean hasParticipatedInRanked() {
        return this.profileData.getRankedKitData().values().stream().anyMatch(data -> data.getWins() > 0 || data.getLosses() > 0 || data.getElo() != 1000);
    }

    /**
     * Checks if the profile has participated in tournaments.
     * 检查玩家是否参与过锦标赛。
     *
     * @return True if the profile has participated in tournaments, otherwise false.
     *         如果玩家参与过锦标赛则返回 true，否则返回 false。
     */
    public boolean hasParticipatedInTournament() {
        return this.profileData.isTournamentParticipated();
    }

    /**
     * Checks if the profile has participated in FFA matches.
     * 检查玩家是否参与过 FFA 比赛。
     *
     * @return True if the profile has participated in FFA matches, otherwise false.
     *         如果玩家参与过 FFA 比赛则返回 true，否则返回 false。
     */
    public boolean hasParticipatedInFFA() {
        return this.profileData.getFfaData().values().stream().anyMatch(data -> data.getKills() > 0 || data.getDeaths() > 0);
    }

    /**
     * Get the next division or tier string for a given profile and kit.
     * 获取给定玩家档案和套件的下一个段位或等级字符串。
     *
     * @param kitName The name of the kit.
     *                套件的名称。
     * @return The next division or tier string.
     *         下一个段位或等级字符串。
     */
    public String getNextDivisionAndTier(String kitName) {
        ProfileUnrankedKitData profileUnrankedKitData = this.profileData.getUnrankedKitData().get(kitName);
        Division division = profileUnrankedKitData.getDivision();
        DivisionTier tier = profileUnrankedKitData.getTier();

        List<DivisionTier> tiers = division.getTiers();
        int tierIndex = tiers.indexOf(tier);

        if (tierIndex < tiers.size() - 1) {
            DivisionTier nextTier = tiers.get(tierIndex + 1);
            return division.getName() + " " + nextTier.getName();
        }

        DivisionService divisionService = AlleyPlugin.getInstance().getService(DivisionService.class);
        List<Division> divisions = divisionService.getDivisions();
        int divisionIndex = divisions.indexOf(division);

        if (divisionIndex < divisions.size() - 1) {
            Division nextDivision = divisions.get(divisionIndex + 1);
            return nextDivision.getName() + " " + nextDivision.getTiers().get(0).getName();
        }

        return profileUnrankedKitData.getDivision().getName() + " " + profileUnrankedKitData.getTier().getName();
    }

    /**
     * Get the next division for a given profile and kit.
     * 获取给定玩家档案和套件的下一个段位。
     *
     * @param kitName The name of the kit.
     *                套件的名称。
     * @return The next division.
     *         下一个段位。
     */
    public Division getNextDivision(String kitName) {
        ProfileUnrankedKitData profileUnrankedKitData = this.profileData.getUnrankedKitData().get(kitName);
        Division division = profileUnrankedKitData.getDivision();

        DivisionService divisionService = AlleyPlugin.getInstance().getService(DivisionService.class);

        List<Division> divisions = divisionService.getDivisions();
        int divisionIndex = divisions.indexOf(division);

        if (divisionIndex < divisions.size() - 1) {
            return divisions.get(divisionIndex + 1);
        }

        return null;
    }

    /**
     * Updates the last play time of the profile.
     * 更新玩家档案的最后游戏时间。
     */
    public void updatePlayTime() {
        ProfilePlayTimeData playTimeData = this.profileData.getPlayTimeData();
        playTimeData.setTotal(playTimeData.getTotal() + (System.currentTimeMillis() - playTimeData.getLastLogin()));
    }
}
