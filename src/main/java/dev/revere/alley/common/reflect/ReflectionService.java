package dev.revere.alley.common.reflect;

import dev.revere.alley.bootstrap.lifecycle.Service;

import java.util.List;

/**
 * @author Remi
 * @project alley-practice
 * @date 2/07/2025
 */
public interface ReflectionService extends Service {
    /**
     * Gets a list of all discovered reflect service instances.
     * 获取所有已发现的反射服务实例的列表。
     * @return An unmodifiable list of reflect services.
     *         不可修改的反射服务列表。
     */
    List<Reflection> getReflectionServices();

    /**
     * Retrieves a specific reflect service by its class type.
     * 根据类类型检索特定的反射服务。
     *
     * @param serviceClass The class type of the service to retrieve.
     *                     要检索的服务的类类型。
     * @param <T> The type of the reflect service.
     *            反射服务的类型。
     * @return The service instance, or null if not found.
     *         服务实例，如果未找到则返回 null。
     */
    <T extends Reflection> T getReflectionService(Class<T> serviceClass);
}