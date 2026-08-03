package dev.revere.alley.feature.kit.setting;

import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.bootstrap.lifecycle.Service;

import java.util.List;

/**
 * @author Remi
 * @project alley-practice
 * @date 2/07/2025
 */
public interface KitSettingService extends Service {

    /**
     * Gets a list of all discovered KitSetting template instances.
     * 获取所有已发现的 KitSetting 模板实例的列表。
     * @return A list of all KitSettings.
     * @return 所有 KitSettings 的列表。
     */
    List<KitSetting> getSettings();

    /**
     * Creates a new instance of a KitSetting by its registered name.
     * 通过注册名称创建 KitSetting 的新实例。
     * @param name The name of the setting (from @KitSettingData).
     * @param name 设置的名称（来自 @KitSettingData）。
     * @return A new KitSetting instance, or null if not found.
     * @return 新的 KitSetting 实例，如果未找到则返回 null。
     */
    KitSetting createSettingByName(String name);

    /**
     * Gets the template instance of a KitSetting by its name.
     * 通过名称获取 KitSetting 的模板实例。
     * @param name The name of the setting.
     * @param name 设置的名称。
     * @return The singleton KitSetting instance, or null if not found.
     * @return 单例 KitSetting 实例，如果未找到则返回 null。
     */
    KitSetting getSettingByName(String name);

    /**
     * Gets the template instance of a KitSetting by its class.
     * 通过类获取 KitSetting 的模板实例。
     * @param clazz The class of the setting.
     * @param clazz 设置的类。
     * @return The singleton KitSetting instance, or null if not found.
     * @return 单例 KitSetting 实例，如果未找到则返回 null。
     */
    <T extends KitSetting> T getSettingByClass(Class<T> clazz);

    /**
     * Applies all default settings to a given kit.
     * 将所有默认设置应用于给定的工具包。
     * @param kit The kit to apply settings to.
     * @param kit 要应用设置的工具包。
     */
    void applyAllSettingsToKit(Kit kit);
}