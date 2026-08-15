package dev.revere.alley.adapter.core;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.locale.LocaleService;
import dev.revere.alley.core.locale.internal.impl.SettingsLocaleImpl;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.feature.level.LevelService;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Objects;

/**
 * @author Emmy
 * @project Alley
 * @since 26/04/2025
 */
public interface Core {
    /**
     * Retrieves the bootstrap name of the server implementation.
     * 获取服务器实现的引导名称。
     *
     * @return The bootstrap name as a String.
     *         引导名称（字符串）。
     */
    CoreType getType();

    /**
     * Retrieves the color associated with a given player.
     * 获取与给定玩家关联的颜色。
     *
     * @param player The player whose color is to be retrieved.
     *               要获取其颜色的玩家。
     * @return The color as a ChatColor object.
     *         颜色（ChatColor 对象）。
     */
    ChatColor getPlayerColor(Player player);

    /**
     * Retrieves the rank prefix for a given player
     * 获取给定玩家的排名前缀。
     *
     * @param player The player whose rank prefix is to be retrieved.
     *               要获取其排名前缀的玩家。
     * @return The rank prefix as a String.
     *         排名前缀（字符串）。
     */
    String getRankPrefix(Player player);

    /**
     * Retrieves the rank name for a given player.
     * 获取给定玩家的排名名称。
     *
     * @param player The player whose rank is to be retrieved.
     *               要获取其排名的玩家。
     * @return The rank name as a String.
     *         排名名称（字符串）。
     */
    String getRankName(Player player);

    /**
     * Retrieves the rank suffix for a given player.
     * 获取给定玩家的排名后缀。
     *
     * @param player The player whose rank suffix is to be retrieved.
     *               要获取其排名后缀的玩家。
     * @return The rank suffix as a String.
     *         排名后缀（字符串）。
     */
    String getRankSuffix(Player player);

    /**
     * Retrieves the rank color for a given player.
     * 获取给定玩家的排名颜色。
     *
     * @param player The player whose rank color is to be retrieved.
     *               要获取其排名颜色的玩家。
     * @return The rank color as a ChatColor object.
     *         排名颜色（ChatColor 对象）。
     */
    ChatColor getRankColor(Player player);

    /**
     * Retrieves the tag prefix for a given player.
     * 获取给定玩家的标签前缀。
     *
     * @param player The player whose tag prefix is to be retrieved.
     *               要获取其标签前缀的玩家。
     * @return The tag prefix as a String.
     *         标签前缀（字符串）。
     */
    String getTagPrefix(Player player);

    /**
     * Retrieves the color associated with a given player's tag.
     * 获取与给定玩家标签关联的颜色。
     *
     * @param player The player whose tag color is to be retrieved.
     *               要获取其标签颜色的玩家。
     * @return The tag color as a String.
     *         标签颜色（字符串）。
     */
    ChatColor getTagColor(Player player);

    /** Applies the player's level-prefix preference to the configured chat format. */
    default String applyChatLevelPrefix(Profile profile, String chatFormat) {
        boolean enabled = profile.getProfileData().getSettingData().isShowChatLevelPrefix();
        if (!enabled) {
            return stripDisabledLevelPrefix(chatFormat);
        }

        String level = Objects.requireNonNull(CC.translate(AlleyPlugin.getInstance().getService(LevelService.class)
                .getLevel(profile.getProfileData().getGlobalLevel()).getDisplayName()), "Level cannot be null");
        String levelPrefix = CC.translate("&7[" + level + "&7]&r ");

        if (chatFormat.contains("{level-prefix}")) {
            return chatFormat.replace("{level-prefix}", levelPrefix);
        }

        if (chatFormat.contains("{level}")) {
            return chatFormat.replace("{level}", level);
        }

        return levelPrefix + chatFormat;
    }

    default String stripDisabledLevelPrefix(String chatFormat) {
        String stripped = chatFormat
                .replace("{level-prefix}", "")
                .replace("{level}", "");
        // Configs such as "&7[{level}&7]&r " leave the brackets behind after
        // the placeholder is removed. Drop that leftover wrapper too.
        stripped = stripped.replaceAll("(?i)(&7|§7)?\\[\\s*](&7|§7)?(&r|§r)?\\s*", "");
        return stripped;
    }

    /**
     * Retrieves the chat format for a given player and message.
     * 获取给定玩家和消息的聊天格式。
     *
     * @param player       The player whose chat format is to be retrieved.
     *                     要获取其聊天格式的玩家。
     * @param eventMessage The message to be formatted.
     *                     要格式化的消息。
     * @param separator    The separator to be used in the chat format.
     *                     聊天格式中使用的分隔符。
     * @return The formatted chat message as a String.
     *         格式化后的聊天消息（字符串）。
     */
    default String getChatFormat(Player player, String eventMessage, String separator) {
        Profile profile = AlleyPlugin.getInstance().getService(ProfileService.class).getProfile(player.getUniqueId());

        String prefix = CC.translate(this.getRankPrefix(player));
        String suffix = CC.translate(this.getRankSuffix(player));
        String tagPrefix = CC.translate(this.getTagPrefix(player));

        ChatColor nameColor = profile.getNameColor() != null ? profile.getNameColor() : this.getPlayerColor(player);
        ChatColor rankColor = this.getRankColor(player);
        ChatColor tagColor = this.getTagColor(player);

        String selectedTitleRaw = profile.getProfileData().getSelectedTitle();
        String selectedTitle = "";
        if (selectedTitleRaw != null && !selectedTitleRaw.isEmpty()) {
            var titleRec = ((dev.revere.alley.feature.title.internal.TitleServiceImpl)
                    AlleyPlugin.getInstance().getService(dev.revere.alley.feature.title.TitleService.class))
                    .getSortedTitles().stream()
                    .filter(t -> t.getName().equalsIgnoreCase(selectedTitleRaw))
                    .findFirst().orElse(null);
            selectedTitle = CC.translate(titleRec != null ? titleRec.getPrefix() : selectedTitleRaw) + " ";
        }
        String tagAppearanceFormat = AlleyPlugin.getInstance().getService(LocaleService.class).getString(SettingsLocaleImpl.SERVER_CHAT_FORMAT_TAG_APPEARANCE_FORMAT)
                .replace("{tag-color}", String.valueOf(tagColor))
                .replace("{tag-prefix}", CC.translate(tagPrefix));

        if (player.hasPermission(AlleyPlugin.getInstance().getService(LocaleService.class).getString(SettingsLocaleImpl.PERMISSION_USE_OF_COLOR_CODES_IN_CHAT))) {
            eventMessage = CC.translate(eventMessage);
        }

        String chatFormat = this.applyChatLevelPrefix(profile, AlleyPlugin.getInstance().getService(LocaleService.class)
                .getString(SettingsLocaleImpl.SERVER_CHAT_FORMAT_GLOBAL));

        return chatFormat
                .replace("{prefix}", prefix)
                .replace("{rank-color}", String.valueOf(rankColor))
                .replace("{name-color}", String.valueOf(nameColor))
                .replace("{player}", player.getName())
                .replace("{suffix}", suffix)
                .replace("{tag}", tagPrefix.isEmpty() ? "" : tagAppearanceFormat)
                .replace("{separator}", separator)
                .replace("{message}", eventMessage)
                .replace("{selected-title}", selectedTitle);
    }
}
