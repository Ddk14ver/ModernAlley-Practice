package dev.revere.alley.feature.leaderboard.hologram;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TextDisplay;
import org.bukkit.metadata.FixedMetadataValue;
import dev.revere.alley.AlleyPlugin;

/**
 * @author Alley
 * @project Alley
 * @since 02/07/2025
 *
 * Factory for creating and configuring TextDisplay entities used as hologram lines.
 * 为用作全息图行的TextDisplay实体的创建和配置工厂。
 */
public final class TextDisplayFactory {

    /** Metadata key used to identify hologram entities for protection. */
    public static final String HOLOGRAM_META_KEY = "alley_hologram";

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    /**
     * Creates a new TextDisplay entity at the given location.
     */
    public static TextDisplay create(Location location, String text) {
        if (location.getWorld() == null) return null;

        TextDisplay display = (TextDisplay) location.getWorld().spawnEntity(location, EntityType.TEXT_DISPLAY);
        configure(display, text);
        display.setMetadata(HOLOGRAM_META_KEY, new FixedMetadataValue(AlleyPlugin.getInstance(), true));
        return display;
    }

    /**
     * Configures a TextDisplay entity with standard hologram settings.
     */
    public static void configure(TextDisplay display, String text) {
        display.text(deserializeText(text));
        display.setBillboard(Display.Billboard.CENTER);
        display.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
        display.setSeeThrough(false);
        display.setShadowed(true);
        display.setGravity(false);
        display.setInvulnerable(true);
        display.setPersistent(false);
    }

    /**
     * Updates the text of an existing TextDisplay entity.
     */
    public static void updateText(TextDisplay display, String text) {
        if (isAlive(display)) {
            display.text(deserializeText(text));
        }
    }

    /**
     * Converts legacy color codes to Adventure Component via MiniMessage.
     */
    private static Component deserializeText(String text) {
        String replaced = text.replace('§', '&');
        replaced = replaced.replace("&0", "<black>")
                .replace("&1", "<dark_blue>").replace("&2", "<dark_green>")
                .replace("&3", "<dark_aqua>").replace("&4", "<dark_red>")
                .replace("&5", "<dark_purple>").replace("&6", "<gold>")
                .replace("&7", "<gray>").replace("&8", "<dark_gray>")
                .replace("&9", "<blue>").replace("&a", "<green>")
                .replace("&b", "<aqua>").replace("&c", "<red>")
                .replace("&d", "<light_purple>").replace("&e", "<yellow>")
                .replace("&f", "<white>").replace("&k", "<obfuscated>")
                .replace("&l", "<bold>").replace("&m", "<strikethrough>")
                .replace("&n", "<underline>").replace("&o", "<italic>")
                .replace("&r", "<reset>");
        return MINI_MESSAGE.deserialize(replaced);
    }

    /**
     * Checks if a TextDisplay entity is alive.
     */
    public static boolean isAlive(TextDisplay display) {
        return display != null && !display.isDead();
    }

    /**
     * Safely removes a TextDisplay entity.
     */
    public static void safeRemove(TextDisplay display) {
        if (isAlive(display)) {
            display.remove();
        }
    }

    /**
     * Checks if an entity is a hologram created by this system.
     */
    public static boolean isHologramEntity(Entity entity) {
        return entity instanceof TextDisplay && entity.hasMetadata(HOLOGRAM_META_KEY);
    }
}
