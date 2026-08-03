package dev.revere.alley.library.menu;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.PlayerUtil;
import dev.revere.alley.common.constants.PluginConstant;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.locale.LocaleEntry;
import dev.revere.alley.core.locale.LocaleService;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.UUID;

public abstract class Button {
    /**
     * Creates a placeholder button with the specified material and title.
     * 使用指定的材质和标题创建一个占位按钮。
     *
     * @param material The material of the placeholder item.
     *                 占位物品的材质。
     * @param title    The title of the placeholder item.
     *                 占位物品的标题。
     * @return A Button instance representing the placeholder.
     *         表示该占位符的按钮实例。
     */
    public static Button placeholder(final Material material, String... title) {
        return new Button() {
            public ItemStack getButtonItem(Player player) {
                ItemStack it = new ItemStack(material, 1);
                ItemMeta meta = it.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(StringUtils.join(title));
                    it.setItemMeta(meta);
                }
                return it;
            }
        };
    }

    /**
     * Plays a sound to the player indicating a failed action.
     * 向玩家播放表示操作失败的声音。
     *
     * @param player The player to play the sound to.
     *               要播放声音的目标玩家。
     */
    public void playFail(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 20F, 0.1F);
    }

    /**
     * Plays a sound to the player indicating a successful action.
     * 向玩家播放表示操作成功的声音。
     *
     * @param player The player to play the sound to.
     *               要播放声音的目标玩家。
     */
    public void playSuccess(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 20F, 15F);
    }

    /**
     * Plays a neutral sound to the player.
     * 向玩家播放一个中性声音。
     *
     * @param player The player to play the sound to.
     *               要播放声音的目标玩家。
     */
    public void playNeutral(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 20F, 15F);
    }

    /**
     * Gets the item to be displayed for this button.
     * 获取此按钮要显示的物品。
     *
     * @param player The player viewing the button.
     *               正在查看此按钮的玩家。
     * @return The ItemStack representing the button.
     *         表示该按钮的ItemStack。
     */
    public abstract ItemStack getButtonItem(Player player);

    /**
     * Handles the click event for the button.
     * 处理按钮的点击事件。
     *
     * @param player    The player who clicked the button.
     *                  点击按钮的玩家。
     * @param clickType The type of click.
     *                  点击类型。
     */
    public void clicked(Player player, ClickType clickType) {

    }

    /**
     * Handles the click event for the button with additional parameters exclusively for advanced use cases.
     * 处理按钮的点击事件，附加参数专门用于高级用例。
     *
     * @param player     The player who clicked the button.
     *                   点击按钮的玩家。
     * @param slot       The slot that was clicked.
     *                   被点击的槽位。
     * @param clickType  The type of click.
     *                   点击类型。
     * @param hotbarSlot The hotbar slot that was clicked.
     *                   被点击的快捷栏槽位。
     */
    public void clicked(Player player, int slot, ClickType clickType, int hotbarSlot) {

    }

    /**
     * Determines if the click event should be cancelled.
     * 确定点击事件是否应被取消。
     *
     * @param player    The player who clicked the button.
     *                  点击按钮的玩家。
     * @param clickType The type of click.
     *                  点击类型。
     * @return True if the event should be cancelled, false otherwise.
     *         如果事件应被取消则返回true，否则返回false。
     */
    public boolean shouldCancel(Player player, ClickType clickType) {
        return true;
    }

    /**
     * Determines if the button should update after being clicked.
     * 确定按钮在被点击后是否应更新。
     *
     * @param player    The player who clicked the button.
     *                  点击按钮的玩家。
     * @param clickType The type of click.
     *                  点击类型。
     * @return True if the button should update, false otherwise.
     *         如果按钮应更新则返回true，否则返回false。
     */
    public boolean shouldUpdate(Player player, ClickType clickType) {
        return false;
    }

    /**
     * Either fetches the profile of an online player or retrieves the offline profile.
     * 获取在线玩家的档案，或检索离线玩家的档案。
     *
     * @param target The name of the player.
     *               玩家名称。
     * @param sender The command sender.
     *               命令发送者。
     * @return The profile of the player, or null if not found.
     *         玩家档案，如果未找到则返回null。
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
     * 通过玩家的UUID获取其档案。
     *
     * @param uuid The UUID of the player.
     *             玩家的UUID。
     * @return The profile of the player, or null if not found.
     *         玩家档案，如果未找到则返回null。
     */
    public Profile getProfile(UUID uuid) {
        ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);
        return profileService.getProfile(uuid);
    }

    /**
     * Gets the admin permission prefix for the bootstrap.
     * 获取引导程序的管理员权限前缀。
     *
     * @return The admin permission prefix.
     *         管理员权限前缀。
     */
    public String getAdminPermission() {
        return AlleyPlugin.getInstance().getService(PluginConstant.class).getAdminPermissionPrefix();
    }

    /**
     * Gets the locale service.
     * 获取本地化服务。
     *
     * @return The locale service.
     *         本地化服务实例。
     */
    private LocaleService getLocaleService() {
        return AlleyPlugin.getInstance().getService(LocaleService.class);
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
