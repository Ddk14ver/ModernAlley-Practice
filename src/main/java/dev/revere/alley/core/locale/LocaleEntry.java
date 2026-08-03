package dev.revere.alley.core.locale;

/**
 * @author Emmy
 * @project Alley
 * @since 09/09/2025
 */
public interface LocaleEntry {
    /**
     * Method to retrieve the configuration name.
     * 获取配置名称的方法。
     *
     * @return The name of the configuration file.
     *         配置文件的名称。
     */
    String getConfigName();

    /**
     * Method to retrieve the configuration string path.
     * 获取配置字符串路径的方法。
     *
     * @return The path to the specific string within the configuration file.
     *         配置文件中特定字符串的路径。
     */
    String getConfigPath();

    /**
     * Method to retrieve the default value for this locale entry.
     * 获取此语言条目默认值的方法。
     *
     * @return The default value associated with this locale entry.
     *         与此语言条目关联的默认值。
     */
    Object getDefaultValue();
}