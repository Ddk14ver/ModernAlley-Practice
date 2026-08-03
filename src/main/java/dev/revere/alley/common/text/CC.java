package dev.revere.alley.common.text;

import dev.revere.alley.AlleyPlugin;
import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Color code translation utility class.
 * 颜色代码翻译工具类。
 *
 * @author Emmy
 * @project Alley
 * @date 25/05/2024 - 12:41
 */
@UtilityClass
public class CC {
    public final String MENU_BAR;
    public final String PREFIX;
    public final String ERROR_PREFIX;
    public final String WARNING_PREFIX;

    static {
        MENU_BAR = translate("&8&m----------------------");
        PREFIX = translate("&f[&6" + AlleyPlugin.getInstance().getDescription().getName() + "&f] &r");
        ERROR_PREFIX = translate("&c[&4" + AlleyPlugin.getInstance().getDescription().getName() + "&c] &r");
        WARNING_PREFIX = translate("&f[&c" + AlleyPlugin.getInstance().getDescription().getName() + "&f] &r");
    }

    /**
     * Translate a string to a colored string.
     * 将字符串翻译为彩色字符串。
     *
     * @param string The string to translate.
     *               要翻译的字符串。
     * @return The translated string.
     *         翻译后的字符串。
     */
    public String translate(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    /**
     * Translate a list of strings to a colored list of strings.
     * 将字符串列表翻译为彩色字符串列表。
     *
     * @param string The list of strings to translate.
     *               要翻译的字符串列表。
     * @return The translated list of strings.
     *         翻译后的字符串列表。
     */
    public List<String> translateList(List<String> string) {
        List<String> list = new ArrayList<>();

        for (String line : string) {
            list.add(ChatColor.translateAlternateColorCodes('&', line));
        }

        return list;
    }

    /**
     * Translate an array of strings to a colored list of strings.
     * 将字符串数组翻译为彩色字符串列表。
     *
     * @param string The array of strings to translate.
     *               要翻译的字符串数组。
     * @return The translated list of strings.
     *         翻译后的字符串列表。
     */
    public List<String> translateArray(String[] string) {
        List<String> list = new ArrayList<>();

        for (String line : string) {
            if (line != null) {
                list.add(ChatColor.translateAlternateColorCodes('&', line));
            }
        }

        return list;
    }

    public static void sender(CommandSender sender, String in) {
        sender.sendMessage(translate(in));
    }

    public static void message(Player player, String in) {
        player.sendMessage(translate(in));
    }
}