package dev.revere.alley.common.constants.internal;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.bootstrap.AlleyContext;
import dev.revere.alley.bootstrap.annotation.Service;
import dev.revere.alley.common.constants.PluginConstant;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginDescriptionFile;
import org.reflections.Reflections;

import java.util.List;

/**
 * This class holds constants related to a bootstrap.
 * 该类保存与bootstrap相关的常量。
 * It allows for easy access to specific bootstrap constants and configurations.
 * 它允许方便地访问特定的bootstrap常量和配置。
 *
 * @author Emmy
 * @project Alley
 * @since 03/04/2025
 */
@Getter
@Service(provides = PluginConstant.class, priority = 0)
public class PluginConstantImpl implements PluginConstant {
    private final AlleyPlugin plugin;

    private String name;
    private String version;
    private String description;
    private List<String> authors;
    private String spigotVersion;
    private ChatColor mainColor;
    private String packageDirectory;
    private String adminPermissionPrefix;
    private String permissionLackMessage;
    private Reflections reflections;

    /**
     * Constructor for the PluginConstant class.
     * PluginConstant类的构造函数。
     *
     * @param plugin The Alley bootstrap instance.
     *        Alley bootstrap实例。
     */
    public PluginConstantImpl(AlleyPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initialize(AlleyContext context) {
        PluginDescriptionFile pluginDescription = plugin.getDescription();

        this.name = pluginDescription.getName();
        this.version = pluginDescription.getVersion();
        this.authors = pluginDescription.getAuthors();
        this.description = pluginDescription.getDescription();
        this.spigotVersion = this.getBukkitVersionExact();

        this.mainColor = ChatColor.GOLD;
        this.packageDirectory = "dev.revere.alley";

        this.adminPermissionPrefix = this.name + ".admin";
        this.permissionLackMessage = ChatColor.RED + "Missing permission.";

        this.reflections = new Reflections(this.packageDirectory);
    }

    /**
     * Gets the exact Bukkit version of the server (e.g., "1.8.8").
     * 获取服务器的精确Bukkit版本（例如"1.8.8"）。
     * @return The exact Bukkit version string.
     *         精确的Bukkit版本字符串。
     */
    private String getBukkitVersionExact() {
        String serverVersion = this.plugin.getServer().getVersion();
        int mcIndex = serverVersion.indexOf("MC: ");
        if (mcIndex != -1) {
            int endIndex = serverVersion.indexOf(")", mcIndex);
            if (endIndex != -1) {
                return serverVersion.substring(mcIndex + 4, endIndex);
            }
        }
        return "Unknown";
    }
}