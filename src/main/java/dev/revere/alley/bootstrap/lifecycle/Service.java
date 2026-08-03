package dev.revere.alley.bootstrap.lifecycle;

import dev.revere.alley.bootstrap.AlleyContext;

/**
 * @author Remi
 * @project alley-practice
 * @date 2/07/2025
 */
public interface Service {
    /**
     * Called by the AlleyContext *after* the service instance has been created
     * and its server dependencies are available, but not necessarily fully initialized.
     * 由 AlleyContext 在服务实例创建之后、其服务器依赖可用但未必完全初始化时调用。
     *
     * @param context The application context, for access to the bootstrap instance or other services.
     *        应用程序上下文，用于访问引导实例或其他服务。
     */
    default void setup(AlleyContext context) {
        // Default implementation: no-op
        // 默认实现：空操作
    }

    /**
     * Called by the AlleyContext *after* all services have been created and setup.
     * Use this for logic that requires other services to be fully operational,
     * such as registering listeners or loading data from other services.
     * 由 AlleyContext 在所有服务已创建并完成 setup 之后调用。
     * 用于需要其他服务完全可操作的逻辑，例如注册监听器或从其他服务加载数据。
     *
     * @param context The application context.
     *        应用程序上下文。
     */
    default void initialize(AlleyContext context) {
        // Default implementation: no-op
        // 默认实现：空操作
    }

    /**
     * Called by the AlleyContext during bootstrap shutdown.
     * Should be used to release resources, save data, etc.
     * 由 AlleyContext 在引导关闭期间调用。
     * 应用于释放资源、保存数据等。
     *
     * @param context The application context.
     *        应用程序上下文。
     */
    default void shutdown(AlleyContext context) {
        // Default implementation: no-op
        // 默认实现：空操作
    }
}