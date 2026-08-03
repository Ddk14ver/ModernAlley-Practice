package dev.revere.alley.common.constants;

import dev.revere.alley.bootstrap.lifecycle.Service;
import org.bukkit.ChatColor;
import org.reflections.Reflections;

import java.util.List;

/**
 * @author Remi
 * @project alley-practice
 * @date 2/07/2025
 */
public interface PluginConstant extends Service {
    String getName();

    String getVersion();

    String getDescription();

    List<String> getAuthors();

    String getSpigotVersion();

    ChatColor getMainColor();

    String getPackageDirectory();

    String getAdminPermissionPrefix();

    String getPermissionLackMessage();

    /**
     * Gets the Reflections instance for classpath scanning.
     * 获取用于类路径扫描的Reflections实例。
     *
     * @return The Reflections object.
     *         Reflections对象。
     */
    Reflections getReflections();
}