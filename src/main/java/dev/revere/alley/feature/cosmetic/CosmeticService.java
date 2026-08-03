package dev.revere.alley.feature.cosmetic;

import dev.revere.alley.bootstrap.lifecycle.Service;
import dev.revere.alley.feature.cosmetic.internal.repository.BaseCosmeticRepository;
import dev.revere.alley.feature.cosmetic.model.CosmeticType;
import dev.revere.alley.feature.cosmetic.preview.CosmeticPreviewManager;

import java.util.Map;

/**
 * @author Remi
 *         雷米
 * @project alley-practice
 *         alley-practice 项目
 * @date 2/07/2025
 *         2025年7月2日
 */
public interface CosmeticService extends Service {
    Map<CosmeticType, BaseCosmeticRepository<?>> getRepositories();

    CosmeticPreviewManager getPreviewManager();

    /**
     * Gets a specific cosmetic repository by its type.
     * 根据类型获取特定的装饰品仓库。
     *
     * @param type The CosmeticType of the repository to retrieve.
     * @param type 要检索的仓库的装饰品类型。
     * @return The repository instance, or null if it's not registered.
     * @return 仓库实例，如果未注册则返回 null。
     */
    BaseCosmeticRepository<?> getRepository(CosmeticType type);

    /**
     * A type-safe helper to get a specific cosmetic repository.
     * 类型安全的辅助方法，用于获取特定的装饰品仓库。
     *
     * @param type The CosmeticType of the repository.
     * @param type 仓库的装饰品类型。
     * @param repositoryClass The class of the repository for type casting.
     * @param repositoryClass 用于类型转换的仓库类。
     * @return The repository cast to its specific type, or null.
     * @return 转换为特定类型的仓库，如果未找到则返回 null。
     */
    <T extends BaseCosmeticRepository<?>> T getRepository(CosmeticType type, Class<T> repositoryClass);
}
