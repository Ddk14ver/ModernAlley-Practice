package dev.revere.alley.feature.cosmetic.internal.repository;

import dev.revere.alley.feature.cosmetic.model.CosmeticRepository;
import dev.revere.alley.common.logger.Logger;
import dev.revere.alley.feature.cosmetic.model.BaseCosmetic;
import dev.revere.alley.feature.cosmetic.model.CosmeticType;
import lombok.Getter;

import java.util.*;

/**
 * @author Remi
 * 作者 Remi
 * @project Alley
 * 项目 Alley
 * @date 6/1/2024
 * 日期 6/1/2024
 */
@Getter
public abstract class BaseCosmeticRepository<T extends BaseCosmetic> implements CosmeticRepository<T> {
    private final Map<String, T> cosmeticsByName;

    public BaseCosmeticRepository() {
        this.cosmeticsByName = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    }

    /**
     * Register a cosmetic class to the repository
     * 向仓库注册一个装饰品类别
     *
     * @param clazz The class to register
     *         要注册的类
     */
    protected void registerCosmetic(Class<? extends T> clazz) {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            this.cosmeticsByName.put(instance.getName(), instance);
        } catch (Exception e) {
            Logger.error("Failed to register cosmetic class " + clazz.getSimpleName() + ": " + e.getMessage());
        }
    }

    @Override
    public CosmeticType getRepositoryType() {
        if (cosmeticsByName.isEmpty()) {
            return null;
        }
        return cosmeticsByName.values().iterator().next().getType();
    }

    @Override
    public List<T> getCosmetics() {
        return new ArrayList<>(this.cosmeticsByName.values());
    }

    @Override
    public T getCosmetic(String name) {
        return this.cosmeticsByName.get(name);
    }
}