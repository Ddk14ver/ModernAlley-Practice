package dev.revere.alley.feature.cosmetic.model;

import dev.revere.alley.feature.cosmetic.internal.repository.impl.suit.BaseSuit;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.function.BiConsumer;

/**
 * @author Remi
 * @project Alley
 * @date 6/23/2025
 */
@Getter
public enum CosmeticType {
    KILL_EFFECT("killeffect", "Display a fancy effect upon killing a player.", (cosmetic, player) -> {
    }),
    SOUND_EFFECT("soundeffect", "Play a custom sound when you get a kill.", (cosmetic, player) -> {
    }),
    PROJECTILE_TRAIL("projectiletrail", "Leave a nice particle trail on your projectiles.", (cosmetic, player) -> {
    }),
    KILL_MESSAGE("killmessage", "&7Broadcast custom messages when you die.", (cosmetic, player) -> {
    }),
    SUIT("suit", "Wear a fancy suit to show off your style.", (cosmetic, player) -> {
        if (cosmetic instanceof BaseSuit) {
            ((BaseSuit) cosmetic).onSelect(player);
        }
    }),
    CLOAK("cloak", "Wear a cloak to show off your style.", (cosmetic, player) -> {
    }),
    TITLE("title", "Purchase a special title to show off.", (cosmetic, player) -> {
    }),
    MVP_MUSIC("mvpmusic", "Custom MVP music when you become MVP of a match.", (cosmetic, player) -> {
    });

    private final String permissionKey;
    private final String description;
    private final BiConsumer<BaseCosmetic, Player> selectionHandler;

    CosmeticType(String permissionKey, String description, BiConsumer<BaseCosmetic, Player> selectionHandler) {
        this.permissionKey = permissionKey;
        this.description = description;
        this.selectionHandler = selectionHandler;
    }

    /**
     * Handles the selection behavior for this cosmetic type.
     * 处理此装饰品类型的选择行为。
     *
     * @param cosmetic The cosmetic being selected
     *                 正在被选择的装饰品
     * @param player   The player selecting the cosmetic
     *                 正在选择装饰品的玩家
     */
    public void handleSelection(BaseCosmetic cosmetic, Player player) {
        selectionHandler.accept(cosmetic, player);
    }

    /**
     * Finds a CosmeticType from a user-friendly string, ignoring case, dashes, and underscores.
     * 从用户友好的字符串中查找装饰品类型，忽略大小写、连字符和下划线。
     *
     * @param input The string provided by the user (e.g., "KillEffect", "kill-effect").
     *              用户提供的字符串（例如 "KillEffect"、"kill-effect"）。
     * @return The matching EnumCosmeticType, or null if not found.
     *         匹配的枚举装饰品类型，如果未找到则返回null。
     */
    public static CosmeticType fromString(String input) {
        if (input == null) {
            return null;
        }
        String sanitizedInput = input.replace("-", "").replace("_", "").toUpperCase();

        for (CosmeticType type : values()) {
            String sanitizedEnumName = type.name().replace("_", "");
            if (sanitizedEnumName.equals(sanitizedInput)) {
                return type;
            }

            String sanitizedPermissionKey = type.permissionKey.replace("-", "").replace("_", "").toUpperCase();
            if (sanitizedPermissionKey.equals(sanitizedInput)) {
                return type;
            }
        }
        return null;
    }
}