package dev.revere.alley.core.profile.menu.setting.enums;

import dev.revere.alley.common.text.CC;
import dev.revere.alley.common.text.LoreHelper;
import dev.revere.alley.core.profile.data.types.ProfileSettingData;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public enum MatchSettingType {
    SHOW_CPS(10, "&6&lShow CPS", Material.CLOCK,
            settings -> toggleLore("Show both players' CPS", settings.isShowMatchCps())),

    SHOW_PING(11, "&6&lShow Ping", Material.COMPASS,
            settings -> toggleLore("Show both players' ping", settings.isShowMatchPing())),

    SHOW_OPPONENT(12, "&6&lShow Opponent", Material.PLAYER_HEAD,
            settings -> toggleLore("Show your opponent's name", settings.isShowMatchOpponent())),

    MVP_MUSIC(13, "&6&lMVP Music", Material.JUKEBOX,
            settings -> toggleLore("Hear MVP music after matches", settings.isMatchMvpMusicEnabled())),

    FLY_ON_LOSS(14, "&6&lFly On Loss", Material.FEATHER,
            settings -> toggleLore("Automatically fly after losing", settings.isFlyOnLoss())),

    FLY_ON_WIN(15, "&6&lFly On Win", Material.ELYTRA,
            settings -> toggleLore("Automatically fly after winning", settings.isFlyOnWin())),

    QUEUE_PING_RANGE(16, "&6&lQueue Ping Range", Material.STICK,
            settings -> Arrays.asList(
                    CC.MENU_BAR,
                    "&7Limit the ping difference between",
                    "&7you and matched opponents.",
                    "",
                    formatPingRange("Disabled", settings.getQueuePingRange() == 0),
                    formatPingRange("+/-30ms", settings.getQueuePingRange() == 30),
                    formatPingRange("+/-50ms", settings.getQueuePingRange() == 50),
                    formatPingRange("+/-100ms", settings.getQueuePingRange() == 100),
                    formatPingRange("+/-200ms", settings.getQueuePingRange() == 200),
                    "",
                    "&aClick to cycle.",
                    CC.MENU_BAR
            )),

    KILL_EFFECTS(19, "&6&lKill Effects", Material.NETHER_STAR,
            settings -> viewLore("Customize the effect played", "when you defeat an opponent.")),

    SWING_SLOWLY(20, "&6&lSwing Slowly", Material.GOLDEN_PICKAXE,
            settings -> toggleLore("Apply a slow-motion final hit effect", settings.isSwingSlowlyEnabled())),

    ALLOW_SPECTATORS(21, "&6&lAllow Spectators", Material.ENDER_EYE,
            settings -> toggleLore("Allow others to spectate your match", settings.isAllowSpectators())),

    DISABLE_PUBLIC_CHAT(22, "&6&lDisable Public Chat In Match", Material.WRITABLE_BOOK,
            settings -> toggleLore("Only show opponent and system messages", settings.isDisablePublicChatWhenInMatch())),

    HIDE_OTHER_SPECTATORS(23, "&6&lHide Other Spectators", Material.SPECTRAL_ARROW,
            settings -> toggleLore("Hide other spectators while spectating", settings.isHideOtherSpectators())),

    SWORD_BLOCK_SOUNDS(24, "&6&lSword Block Sounds", Material.SHIELD,
            settings -> toggleLore("Hear successful and failed sword blocks", settings.isSwordBlockSoundsEnabled())),

    SHOW_CHAT_LEVEL_PREFIX(25, "&6&lChat Level Prefix", Material.EXPERIENCE_BOTTLE,
            settings -> toggleLore("Show your level before chat messages", settings.isShowChatLevelPrefix()));

    public final int slot;
    public final String displayName;
    public final Material material;
    public final Function<ProfileSettingData, List<String>> loreProvider;

    MatchSettingType(int slot, String displayName, Material material,
                     Function<ProfileSettingData, List<String>> loreProvider) {
        this.slot = slot;
        this.displayName = displayName;
        this.material = material;
        this.loreProvider = loreProvider;
    }

    private static List<String> toggleLore(String description, boolean enabled) {
        return Arrays.asList(
                CC.MENU_BAR,
                "&7" + description + ".",
                "",
                LoreHelper.displayEnabled(enabled),
                "",
                "&aClick to toggle.",
                CC.MENU_BAR
        );
    }

    private static List<String> viewLore(String... description) {
        return Arrays.asList(
                CC.MENU_BAR,
                "&7" + description[0],
                "&7" + description[1],
                "",
                "&aClick to view.",
                CC.MENU_BAR
        );
    }

    private static String formatPingRange(String label, boolean active) {
        return " &6│ " + (active ? "&a&l" : "&7") + label;
    }
}
