package dev.revere.alley.core.locale;


import dev.revere.alley.bootstrap.lifecycle.Service;

import java.util.List;

/**
 * @author Emmy
 * @project alley-practice
 * @since 09/09/2025
 */
public interface LocaleService extends Service {
    /**
     * Method to retrieve and translate a string from a specified configuration file.
     * 从指定配置文件中检索并翻译字符串的方法。
     *
     * @param entry The locale entry containing the configuration name and string path.
     *              包含配置名称和字符串路径的语言条目。
     * @return The translated configuration string, or a default error message if not found.
     *         翻译后的配置字符串，如果未找到则返回默认错误消息。
     */
    String getString(LocaleEntry entry);

    /**
     * Method to retrieve and translate a list of strings from a specified configuration file.
     * 从指定配置文件中检索并翻译字符串列表的方法。
     *
     * @param entry The locale entry containing the configuration name and string path.
     *              包含配置名称和字符串路径的语言条目。
     * @return The translated list of configuration strings, or a default error message if not found or empty.
     *         翻译后的配置字符串列表，如果未找到或为空则返回默认错误消息。
     */
    List<String> getStringList(LocaleEntry entry);

    /**
     * Method to retrieve a raw list of strings from a specified configuration file without translation.
     * 从指定配置文件中检索原始字符串列表（不进行翻译）的方法。
     *
     * @param entry The locale entry containing the configuration name and string path.
     *              包含配置名称和字符串路径的语言条目。
     * @return The raw list of configuration strings, or a default error message if not found or empty.
     *         原始的配置字符串列表，如果未找到或为空则返回默认错误消息。
     */
    List<String> getStringListRaw(LocaleEntry entry);

    /**
     * Method to retrieve an integer value from a specified configuration file.
     * 从指定配置文件中检索整数值的方法。
     *
     * @param entry The locale entry containing the configuration name and string path.
     *              包含配置名称和字符串路径的语言条目。
     * @return The integer value from the configuration.
     *         配置中的整数值。
     */
    int getInt(LocaleEntry entry);

    /**
     * Method to retrieve a double value from a specified configuration file.
     * 从指定配置文件中检索双精度浮点数值的方法。
     *
     * @param entry The locale entry containing the configuration name and string path.
     *              包含配置名称和字符串路径的语言条目。
     * @return The double value from the configuration.
     *         配置中的双精度浮点数值。
     */
    double getDouble(LocaleEntry entry);

    /**
     * Method to retrieve a boolean value from a specified configuration file.
     * 从指定配置文件中检索布尔值的方法。
     *
     * @param entry The locale entry containing the configuration name and string path.
     *              包含配置名称和字符串路径的语言条目。
     * @return The boolean value from the configuration.
     *         配置中的布尔值。
     */
    boolean getBoolean(LocaleEntry entry);

    /**
     * Method to set or update a message in the specified configuration file.
     * 在指定配置文件中设置或更新消息的方法。
     *
     * @param entry   The locale entry containing the configuration name and string path.
     *                包含配置名称和字符串路径的语言条目。
     * @param message The new message to set in the configuration.
     *                要在配置中设置的新消息。
     */
    void setString(LocaleEntry entry, String message);

    /**
     * Method to set or update a list of messages in the specified configuration file.
     * 在指定配置文件中设置或更新消息列表的方法。
     *
     * @param entry    The locale entry containing the configuration name and string path.
     *                 包含配置名称和字符串路径的语言条目。
     * @param messages The new list of messages to set in the configuration.
     *                 要在配置中设置的新消息列表。
     */
    void setList(LocaleEntry entry, List<String> messages);

    /**
     * Method to set or update an integer value in the specified configuration file.
     * 在指定配置文件中设置或更新整数值的方法。
     *
     * @param entry The locale entry containing the configuration name and string path.
     *              包含配置名称和字符串路径的语言条目。
     * @param value The new integer value to set in the configuration.
     *              要在配置中设置的新整数值。
     */
    void setInt(LocaleEntry entry, int value);

    /**
     * Method to set or update a double value in the specified configuration file.
     * 在指定配置文件中设置或更新双精度浮点数值的方法。
     *
     * @param entry The locale entry containing the configuration name and string path.
     *              包含配置名称和字符串路径的语言条目。
     * @param value The new double value to set in the configuration.
     *              要在配置中设置的新双精度浮点数值。
     */
    void setDouble(LocaleEntry entry, double value);

    /**
     * Method to set or update a boolean value in the specified configuration file.
     * 在指定配置文件中设置或更新布尔值的方法。
     *
     * @param entry The locale entry containing the configuration name and string path.
     *              包含配置名称和字符串路径的语言条目。
     * @param value The new boolean value to set in the configuration.
     *              要在配置中设置的新布尔值。
     */
    void setBoolean(LocaleEntry entry, boolean value);
}