package dev.revere.alley.core.profile.data.types;

import dev.revere.alley.core.profile.enums.ChatChannel;
import dev.revere.alley.core.profile.enums.WorldTime;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

/**
 * @author Emmy
 * @project Alley
 * @date 25/05/2024 - 15:22
 */
@Getter
@Setter
public class ProfileSettingData {

    //TODO: Clean this class up a bit, make generic methods for toggling settings, use an enum map to store settings, etc.
    //TODO: 稍微清理一下这个类，创建通用的设置切换方法，使用枚举映射来存储设置等。

    /**
     * public class ProfileSettingData {
     * private final EnumMap<ProfileSetting, ToggleState> settings = new EnumMap<>(ProfileSetting.class);
     * private ChatChannel chatChannel = ChatChannel.GLOBAL;
     * private WorldTime worldTime = WorldTime.DEFAULT;
     * <p>
     * public ProfileSettingData() {
     * settings.put(ProfileSetting.PARTY_MESSAGES, ToggleState.ENABLED);
     * settings.put(ProfileSetting.PARTY_INVITES, ToggleState.ENABLED);
     * settings.put(ProfileSetting.SCOREBOARD, ToggleState.ENABLED);
     * settings.put(ProfileSetting.TABLIST, ToggleState.ENABLED);
     * settings.put(ProfileSetting.SCOREBOARD_LINES, ToggleState.ENABLED);
     * settings.put(ProfileSetting.PROFANITY_FILTER, ToggleState.DISABLED);
     * settings.put(ProfileSetting.DUEL_REQUESTS, ToggleState.ENABLED);
     * settings.put(ProfileSetting.LOBBY_MUSIC, ToggleState.ENABLED);
     * settings.put(ProfileSetting.SERVER_TITLES, ToggleState.ENABLED);
     * }
     * <p>
     * public boolean isEnabled(ProfileSetting setting) {
     * return settings.get(setting).asBoolean();
     * }
     * <p>
     * public void set(ProfileSetting setting, ToggleState state) {
     * settings.put(setting, state);
     * }
     * <p>
     * public void applyWorldTime(Player player) {
     * worldTime.apply(player);
     * }
     * }
     * <p>
     * public enum WorldTime {
     * DEFAULT("Default", (player) -> player.resetPlayerTime()),
     * DAY("Day", (player) -> player.setPlayerTime(6000L, false)),
     * SUNSET("Sunset", (player) -> player.setPlayerTime(12000L, false)),
     * NIGHT("Night", (player) -> player.setPlayerTime(18000L, false));
     * <p>
     * private final String name;
     * private final Consumer<Player> applier;
     * <p>
     * WorldTime(String name, Consumer<Player> applier) {
     * this.name = name;
     * this.applier = applier;
     * }
     * <p>
     * public String getName() {
     * return name;
     * }
     * <p>
     * public void apply(Player player) {
     * applier.accept(player);
     * }
     * }
     * <p>
     * public enum ToggleState {
     * ENABLED, DISABLED;
     * <p>
     * public boolean asBoolean() {
     * return this == ENABLED;
     * }
     * <p>
     * public static ToggleState fromBoolean(boolean value) {
     * return value ? ENABLED : DISABLED;
     * }
     * }
     */

    private boolean partyMessagesEnabled;
    private boolean partyInvitesEnabled;
    private boolean scoreboardEnabled;
    private boolean tablistEnabled;
    private boolean showScoreboardLines;
    private boolean isProfanityFilterEnabled;
    private boolean receiveDuelRequestsEnabled;
    private boolean lobbyMusicEnabled;
    private boolean serverTitles;
    private boolean hidePlayersEnabled;
    private boolean showMatchCps;
    private boolean showMatchPing;
    private boolean showMatchOpponent;
    private boolean matchMvpMusicEnabled;
    private boolean flyOnLoss;
    private boolean flyOnWin;
    private int queuePingRange;
    private boolean swingSlowlyEnabled;
    private boolean allowSpectators;
    private boolean disablePublicChatWhenInMatch;
    private boolean hideOtherSpectators;
    private boolean swordBlockSoundsEnabled;
    private boolean showChatLevelPrefix;
    private String chatChannel;
    private String time;

    /**
     * Constructor for the ProfileSettingData class.
     * ProfileSettingData类的构造函数。
     */
    public ProfileSettingData() {
        this.partyMessagesEnabled = true;
        this.partyInvitesEnabled = true;
        this.scoreboardEnabled = true;
        this.tablistEnabled = true;
        this.showScoreboardLines = true;
        this.isProfanityFilterEnabled = false;
        this.receiveDuelRequestsEnabled = true;
        this.lobbyMusicEnabled = true;
        this.serverTitles = true;
        this.hidePlayersEnabled = false;
        this.showMatchCps = false;
        this.showMatchPing = true;
        this.showMatchOpponent = true;
        this.matchMvpMusicEnabled = true;
        this.flyOnLoss = false;
        this.flyOnWin = true;
        this.queuePingRange = 0;
        this.swingSlowlyEnabled = true;
        this.allowSpectators = true;
        this.disablePublicChatWhenInMatch = false;
        this.hideOtherSpectators = false;
        this.swordBlockSoundsEnabled = false;
        this.showChatLevelPrefix = false;
        this.chatChannel = ChatChannel.GLOBAL.toString();
        this.time = WorldTime.DEFAULT.getName();
    }

    /** Stores only the supported queue ping range values. */
    public void setQueuePingRange(int queuePingRange) {
        this.queuePingRange = queuePingRange == 30 || queuePingRange == 50
                || queuePingRange == 100 || queuePingRange == 200
                ? queuePingRange : 0;
    }

    /**
     * Set the world time for a player to the default time.
     * 将玩家的世界时间设置为默认时间。
     *
     * @param player The player to set the world time for.
     *               要设置世界时间的玩家。
     */
    public void setTimeDefault(Player player) {
        this.time = WorldTime.DEFAULT.getName();
        player.resetPlayerTime();
    }

    /**
     * Set the world time for a player to day.
     * 将玩家的世界时间设置为白天。
     *
     * @param player The player to set the world time for.
     *               要设置世界时间的玩家。
     */
    public void setTimeDay(Player player) {
        this.time = WorldTime.DAY.getName();
        player.setPlayerTime(6000L, false);
    }

    /**
     * Set the world time for a player to sunset.
     * 将玩家的世界时间设置为日落。
     *
     * @param player The player to set the world time for.
     *               要设置世界时间的玩家。
     */
    public void setTimeSunset(Player player) {
        this.time = WorldTime.SUNSET.getName();
        player.setPlayerTime(12000, false);
    }

    /**
     * Set the world time for a player to night.
     * 将玩家的世界时间设置为夜晚。
     *
     * @param player The player to set the player time for.
     *               要设置玩家时间的玩家。
     */
    public void setTimeNight(Player player) {
        this.time = WorldTime.NIGHT.getName();
        player.setPlayerTime(18000L, false);
    }

    /**
     * Get the world time based on the profile setting.
     * 根据资料设置获取世界时间。
     *
     * @return The world time based on the profile setting.
     *         基于资料设置的世界时间。
     */
    public WorldTime getWorldTime() {
        return WorldTime.getByName(this.time);
    }

    /**
     * Set the world time based on the profile setting.
     * 根据资料设置来设置世界时间。
     *
     * @param player The player to set the world time for.
     *               要设置世界时间的玩家。
     */
    public void setTimeBasedOnProfileSetting(Player player) {
        switch (this.getWorldTime()) {
            case DEFAULT:
                this.setTimeDefault(player);
                break;
            case DAY:
                this.setTimeDay(player);
                break;
            case SUNSET:
                this.setTimeSunset(player);
                break;
            case NIGHT:
                this.setTimeNight(player);
                break;
        }
    }

    /**
     * Check if the player is in day time.
     * 检查玩家是否处于白天时间。
     *
     * @return True if the player is in day time.
     *         如果玩家处于白天时间则返回True。
     */
    public boolean isDayTime() {
        return this.time.equals(WorldTime.DAY.getName());
    }

    /**
     * Check if the player is in sunset time.
     * 检查玩家是否处于日落时间。
     *
     * @return True if the player is in sunset time.
     *         如果玩家处于日落时间则返回True。
     */
    public boolean isSunsetTime() {
        return this.time.equals(WorldTime.SUNSET.getName());
    }

    /**
     * Check if the player is in night time.
     * 检查玩家是否处于夜晚时间。
     *
     * @return True if the player is in night time.
     *         如果玩家处于夜晚时间则返回True。
     */
    public boolean isNightTime() {
        return this.time.equals(WorldTime.NIGHT.getName());
    }

    /**
     * Check if the player is in default time.
     * 检查玩家是否处于默认时间。
     *
     * @return True if the player is in default time.
     *         如果玩家处于默认时间则返回True。
     */
    public boolean isDefaultTime() {
        return this.time.equals(WorldTime.DEFAULT.getName());
    }
}
