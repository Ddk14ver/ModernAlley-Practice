package dev.revere.alley.common.reflect.internal.types;

import dev.revere.alley.common.reflect.Reflection;

/**
 * @author Remi
 * @project alley-practice
 * @date 27/06/2025
 */
public class DefaultReflectionImpl implements Reflection {
    /**
     * A single, shared, immutable instance of this reflect utility.
     * 此反射工具类的单一、共享、不可变实例。
     * This prevents the creation of unnecessary objects.
     * 这可以防止创建不必要的对象。
     */
    public static final Reflection INSTANCE = new DefaultReflectionImpl();

    /**
     * Constructor for reflect-based instantiation by `ReflectionRepository`.
     * 用于通过 `ReflectionRepository` 进行基于反射的实例化的构造函数。
     * This constructor must be public for `ReflectionRepository` to successfully
     * 此构造函数必须是 public 的，以便 `ReflectionRepository` 能够成功
     * create an instance using `getDeclaredConstructor().newInstance()`.
     * 使用 `getDeclaredConstructor().newInstance()` 创建实例。
     * The `ReflectionRepository` will manage the lifecycle of this service.
     * `ReflectionRepository` 将管理此服务的生命周期。
     */
    public DefaultReflectionImpl() {}
}