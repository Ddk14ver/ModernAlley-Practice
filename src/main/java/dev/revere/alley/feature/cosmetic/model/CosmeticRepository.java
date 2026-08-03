package dev.revere.alley.feature.cosmetic.model;

import java.util.List;

/**
 * @author Remi
 * @project Alley
 * @date 6/1/2024
 */
public interface CosmeticRepository<T extends Cosmetic> {
    List<T> getCosmetics();

    /**
     * Retrieves a cosmetic by its name.
     * 根据名称检索装饰品。
     *
     * @param name The name of the cosmetic to retrieve.
     *             要检索的装饰品的名称。
     * @return The cosmetic with the specified name, or null if not found.
     *         具有指定名称的装饰品，如果未找到则返回null。
     */
    T getCosmetic(String name);

    /**
     * Add this method to the interface. It declares the category of the repository.
     * 将此方法添加到接口中。它声明了仓库的类别。
     *
     * @return The CosmeticType that this repository manages.
     *         此仓库管理的装饰品类型。
     */
    CosmeticType getRepositoryType();
}