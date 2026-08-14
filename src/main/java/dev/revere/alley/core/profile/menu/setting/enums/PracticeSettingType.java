package dev.revere.alley.core.profile.menu.setting.enums;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.common.text.LoreHelper;
import dev.revere.alley.core.locale.LocaleService;
import dev.revere.alley.core.locale.internal.impl.SettingsLocaleImpl;
import dev.revere.alley.core.profile.data.types.ProfileSettingData;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * @author Emmy
 * @project Alley
 * @since 21/04/2025
 */
public enum PracticeSettingType {
    PARTY_MESSAGES(10, "&6&lToggle Party Messages", Material.FEATHER,
            settings -> Arrays.asList(
                    CC.MENU_BAR,
                    "&7See party chat messages.",
                    "",
                    LoreHelper.displayEnabled(settings.isPartyMessagesEnabled()),
                    "",
                    "&aClick to toggle.",
                    CC.MENU_BAR
            )
    ),

    PARTY_INVITES(11, "&6&lToggle Party Invites", Material.NAME_TAG,
            settings -> Arrays.asList(
                    CC.MENU_BAR,
                    "&7Receive party invites.",
                    "",
                    LoreHelper.displayEnabled(settings.isPartyInvitesEnabled()),
                    "",
                    "&aClick to toggle.",
                    CC.MENU_BAR
            )
    ),

    SIDEBAR_VISIBILITY(12, "&6&lSidebar Visibility", Material.LIME_CARPET, 0,
            settings -> Arrays.asList(
                    CC.MENU_BAR,
                    "&7See the scoreboard.",
                    "",
                    LoreHelper.displayShown(settings.isScoreboardEnabled()),
                    "",
                    "&aClick to toggle.",
                    CC.MENU_BAR
            )
    ),

    TAB_VISIBILITY(13, "&6&lTablist Visibility", Material.ITEM_FRAME,
            settings -> Arrays.asList(
                    CC.MENU_BAR,
                    "&7See the tablist.",
                    "",
                    LoreHelper.displayShown(settings.isTablistEnabled()),
                    "",
                    "&aClick to toggle.",
                    CC.MENU_BAR
            )
    ),

    WORLD_TIME(14, "&6&lWorld time", Material.CLOCK, settings -> Arrays.asList(
            CC.MENU_BAR,
            "&7Change your world time.",
            "",
            formatTime("Default", settings.isDefaultTime(), "&a&l"),
            formatTime("Day", settings.isDayTime(), "&e&l"),
            formatTime("Sunset", settings.isSunsetTime(), "&6&l"),
            formatTime("Night", settings.isNightTime(), "&4&l"),
            "",
            "&aClick to toggle.",
            CC.MENU_BAR
    )),

    SCOREBOARD_LINES(19, "&6&lShow Scoreboard Lines", Material.STRING,
            settings -> Arrays.asList(
                    CC.MENU_BAR,
                    "&7Show scoreboard lines.",
                    "",
                    LoreHelper.displayShown(settings.isShowScoreboardLines()),
                    "",
                    "&aClick to toggle.",
                    CC.MENU_BAR
            )
    ),

    PROFANITY_FILTER(20, "&6&lProfanity Filter", Material.ROTTEN_FLESH,
            settings -> Arrays.asList(
                    CC.MENU_BAR,
                    "&7Hide rude and offensive words.",
                    "",
                    AlleyPlugin.getInstance().getService(LocaleService.class).getBoolean(SettingsLocaleImpl.SERVER_CHAT_FORMAT_ENABLED_BOOLEAN)
                            ?
                            LoreHelper.displayEnabled(settings.isProfanityFilterEnabled())
                            :
                            "&cServer has disabled this setting."
                    ,
                    "",
                    "&aClick to toggle.",
                    CC.MENU_BAR
            )
    ),

    DUEL_REQUESTS(21, "&6&lDuel Requests", Material.DIAMOND_SWORD,
            settings -> Arrays.asList(
                    CC.MENU_BAR,
                    "&7Receive duel requests.",
                    "",
                    LoreHelper.displayEnabled(settings.isReceiveDuelRequestsEnabled()),
                    "",
                    "&aClick to toggle.",
                    CC.MENU_BAR
            )
    ),
    SERVER_TITLES(22, "&6&lServer Titles", Material.PAPER,
            settings -> Arrays.asList(
                    CC.MENU_BAR,
                    "&7Display titles sent by the server.",
                    "",
                    LoreHelper.displayShown(settings.isServerTitles()),
                    "",
                    "&aClick to toggle.",
                    CC.MENU_BAR
            )
    ),

    MATCH_SETTINGS(16, "&6&lMatch Settings", Material.BOOK,
            settings -> Arrays.asList(
                    CC.MENU_BAR,
                    "&7Adjust your match settings.",
                    "",
                    "&aClick to view.",
                    CC.MENU_BAR
            )
    ),

    COSMETICS(25, "&6&lCosmetics", Material.NETHER_STAR,
            settings -> Arrays.asList(
                    CC.MENU_BAR,
                    "&7Customize your cosmetics.",
                    "",
                    "&aClick to view.",
                    CC.MENU_BAR
            )
    ),

    LOBBY_MUSIC(34, "&6&lLobby Music", Material.JUKEBOX,
            settings -> Arrays.asList(
                    CC.MENU_BAR,
                    "&7Customize your lobby music.",
                    "",
                    "&aClick to view.",
                    CC.MENU_BAR
            )
    ),

    SHOP(28, "&6&lShop", Material.EMERALD,
            settings -> Arrays.asList(
                    CC.MENU_BAR,
                    "&7Purchase cosmetics, Kill Effects, and more.",
                    "",
                    "&aClick to open.",
                    CC.MENU_BAR
            )
    ),

    TITLES(29, "&6&lTitles", Material.ARMOR_STAND,
            settings -> Arrays.asList(
                    CC.MENU_BAR,
                    "&7Select and equip your titles.",
                    "",
                    "&aClick to view.",
                    CC.MENU_BAR
            )
    ),

    DIVISIONS(30, "&6&lDivisions", Material.NETHERITE_INGOT,
            settings -> Arrays.asList(
                    CC.MENU_BAR,
                    "&7View all divisions and tiers.",
                    "",
                    "&aClick to view.",
                    CC.MENU_BAR
            )
    ),

    LEVELS(31, "&6&lLevels", Material.EXPERIENCE_BOTTLE,
            settings -> Arrays.asList(
                    CC.MENU_BAR,
                    "&7View your level and progress.",
                    "",
                    "&aClick to view.",
                    CC.MENU_BAR
            )
    ),

    CHALLENGES(32, "&6&lChallenges", Material.PAPER,
            settings -> Arrays.asList(
                    CC.MENU_BAR,
                    "&7Complete daily and weekly tasks",
                    "&7to earn coins.",
                    "",
                    "&aClick to view.",
                    CC.MENU_BAR
            )
    ),

    HIDE_PLAYERS(23, "&6&lHide Players", Material.PLAYER_HEAD, settings -> Arrays.asList(
            CC.MENU_BAR,
            "&7Hide all other players",
            "&7while in the lobby.",
            "",
            LoreHelper.displayToggled(settings.isHidePlayersEnabled()),
            "",
            "&aClick to toggle.",
            CC.MENU_BAR
    )),

    ;

    public final int slot;
    public final String displayName;
    public final Material material;
    public final int durability;
    public final Function<ProfileSettingData, List<String>> loreProvider;

    /**
     * Constructor for the EnumPracticeSettingType enum.
     * EnumPracticeSettingType 枚举的构造方法。
     *
     * @param slot         The slot of the item in the menu.
     *                     物品在菜单中的槽位。
     * @param displayName  The display name of the item.
     *                     物品的显示名称。
     * @param material     The material of the item.
     *                     物品的材质。
     * @param loreProvider A function that provides the lore for the item based on ProfileSettingData.
     *                     一个根据 ProfileSettingData 提供物品描述的 lambda 函数。
     */
    PracticeSettingType(int slot, String displayName, Material material, Function<ProfileSettingData, List<String>> loreProvider) {
        this(slot, displayName, material, 0, loreProvider);
    }

    /**
     * Constructor for the EnumPracticeSettingType enum.
     * EnumPracticeSettingType 枚举的构造方法。
     *
     * @param slot         The slot of the item in the menu.
     *                     物品在菜单中的槽位。
     * @param displayName  The display name of the item.
     *                     物品的显示名称。
     * @param material     The material of the item.
     *                     物品的材质。
     * @param durability   The durability of the item.
     *                     物品的耐久度。
     * @param loreProvider A function that provides the lore for the item based on ProfileSettingData.
     *                     一个根据 ProfileSettingData 提供物品描述的 lambda 函数。
     */
    PracticeSettingType(int slot, String displayName, Material material, int durability, Function<ProfileSettingData, List<String>> loreProvider) {
        this.slot = slot;
        this.displayName = displayName;
        this.material = material;
        this.durability = durability;
        this.loreProvider = loreProvider;
    }

    /**
     * Formats the time string based on the active status.
     * 根据激活状态格式化时间字符串。
     *
     * @param label       The label to display.
     *                    要显示的标签。
     * @param active      Whether the time is active or not.
     *                    时间是否处于激活状态。
     * @param activeColor The color for the active state.
     *                    激活状态的颜色。
     * @return The formatted time string.
     *         格式化后的时间字符串。
     */
    private static String formatTime(String label, boolean active, String activeColor) {
        return " &6│ " + (active ? activeColor : "&7") + label;
    }
}
