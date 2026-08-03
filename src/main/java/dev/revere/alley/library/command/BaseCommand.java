package dev.revere.alley.library.command;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.PlayerUtil;
import dev.revere.alley.common.constants.PluginConstant;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.locale.LocaleEntry;
import dev.revere.alley.core.locale.LocaleService;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.UUID;

public abstract class BaseCommand {
    protected final AlleyPlugin plugin;

    /**
     * Constructor for the BaseCommand class.
     * BaseCommand 类的构造函数。
     */
    public BaseCommand() {
        this.plugin = AlleyPlugin.getInstance();
        this.plugin.getService(CommandFramework.class).registerCommands(this);
    }

    /**
     * Method to be called when a command is executed.
     * 当命令被执行时调用的方法。
     *
     * @param command The command.
     *                命令对象。
     */
    public abstract void onCommand(CommandArgs command);

    /**
     * Either fetches the profile of an online player or retrieves the offline profile.
     * 获取在线玩家的 Profile，或检索离线玩家的 Profile。
     *
     * @param target The name of the player.
     *               玩家名称。
     * @param sender The command sender.
     *               命令发送者。
     * @return The profile of the player, or null if not found.
     *         玩家的 Profile，如果未找到则返回 null。
     */
    public Profile getOfflineProfile(String target, CommandSender sender) {
        OfflinePlayer offlinePlayer = PlayerUtil.getOfflinePlayerByName(target);
        if (offlinePlayer == null) {
            sender.sendMessage(CC.translate("&cThat player does not exist."));
            return null;
        }

        UUID uuid = offlinePlayer.getUniqueId();
        if (uuid == null) {
            sender.sendMessage(CC.translate("&cThat player is invalid."));
            return null;
        }

        ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);
        Profile profile = profileService.getProfile(uuid);
        if (profile == null) {
            sender.sendMessage(CC.translate("&cThat player does not have a profile."));
            return null;
        }

        return profile;
    }

    /**
     * Fetches the profile of a player by their UUID.
     * 通过 UUID 获取玩家的 Profile。
     *
     * @param uuid The UUID of the player.
     *             玩家的 UUID。
     * @return The profile of the player, or null if not found.
     *         玩家的 Profile，如果未找到则返回 null。
     */
    public Profile getProfile(UUID uuid) {
        ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);
        return profileService.getProfile(uuid);
    }

    /**
     * Gets the admin permission prefix for the bootstrap.
     * 获取插件框架的管理员权限前缀。
     *
     * @return The admin permission prefix.
     *         管理员权限前缀。
     */
    public String getAdminPermission() {
        return this.plugin.getService(PluginConstant.class).getAdminPermissionPrefix();
    }

    /**
     * Gets the locale service.
     * 获取本地化服务。
     *
     * @return The locale service.
     *         本地化服务实例。
     */
    private LocaleService getLocaleService() {
        return this.plugin.getService(LocaleService.class);
    }

    /**
     * Fetches a localized message from the locale service.
     * 从本地化服务中获取本地化消息。
     *
     * @param entry The locale entry.
     *              本地化条目。
     * @return The localized message.
     *         本地化消息。
     */
    public String getString(LocaleEntry entry) {
        return this.getLocaleService().getString(entry);
    }

    /**
     * Fetches a localized list of messages from the locale service.
     * 从本地化服务中获取本地化消息列表。
     *
     * @param entry The locale entry.
     *              本地化条目。
     * @return The localized list of messages.
     *         本地化消息列表。
     */
    public List<String> getStringList(LocaleEntry entry) {
        return this.getLocaleService().getStringList(entry);
    }

    /**
     * Fetches a boolean value from the locale service.
     * 从本地化服务中获取布尔值。
     *
     * @param entry The locale entry.
     *              本地化条目。
     * @return The boolean value.
     *         布尔值。
     */
    public boolean getBoolean(LocaleEntry entry) {
        return this.getLocaleService().getBoolean(entry);
    }

    /**
     * Fetches an integer value from the locale service.
     * 从本地化服务中获取整数值。
     *
     * @param entry The locale entry.
     *              本地化条目。
     * @return The integer value.
     *         整数值。
     */
    public int getInt(LocaleEntry entry) {
        return this.getLocaleService().getInt(entry);
    }

    /**
     * Fetches a double value from the locale service.
     * 从本地化服务中获取双精度浮点数值。
     *
     * @param entry The locale entry.
     *              本地化条目。
     * @return The double value.
     *         双精度浮点数值。
     */
    public double getDouble(LocaleEntry entry) {
        return this.getLocaleService().getDouble(entry);
    }
}