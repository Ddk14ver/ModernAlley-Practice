package dev.revere.alley.core.database.model;

import dev.revere.alley.core.profile.Profile;

/**
 * @author Remi
 * @project Alley
 * @date 5/22/2024
 *
 * 数据库 Profile 接口，定义了保存、加载和存档 Profile 的操作。
 * Database Profile interface, defining operations for saving, loading and archiving profiles.
 */
public interface DatabaseProfile {
    /**
     * Gets the type of the database profile.
     *
     * 获取数据库 Profile 的类型。
     *
     * @return The type as a string.
     *         类型字符串。
     */
    DatabaseType getType();

    /**
     * Saves a profile to the database.
     *
     * 将 Profile 保存到数据库。
     *
     * @param profile The profile to save.
     *                要保存的 Profile。
     */
    void saveProfile(Profile profile);

    /**
     * Loads a profile from the database.
     *
     * 从数据库中加载 Profile。
     *
     * @param profile The profile to load.
     *                要加载的 Profile。
     */
    void loadProfile(Profile profile);

    /**
     * Archives a profile in the database.
     *
     * 将 Profile 存档到数据库。
     *
     * @param profile The profile to archive.
     *                要存档的 Profile。
     */
    void archiveProfile(Profile profile);
}
