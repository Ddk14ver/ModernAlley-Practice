package dev.revere.alley.bootstrap.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Remi
 * @project alley-practice
 * @date 2/07/2025
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Service {
    /**
     * The interface that this service provides. This is crucial for dependency injection.
     * 此服务提供的接口。这对于依赖注入至关重要。
     */
    Class<? extends dev.revere.alley.bootstrap.lifecycle.Service> provides();

    /**
     * The priority for initialization (lower values run first).
     * Use this to manage dependencies, e.g., ConfigService = 0, MongoService = 10, ProfileService = 20.
     * 初始化优先级（值越小越先运行）。
     * 使用此属性来管理依赖关系，例如 ConfigService = 0, MongoService = 10, ProfileService = 20。
     */
    int priority() default 100;
}