package dev.revere.alley.bootstrap.listener;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.bootstrap.lifecycle.Service;

/**
 * @author Emmy
 * @project alley-practice
 * @since 16/07/2025
 */
public interface ListenerService extends Service {

    /**
     * Registers all listeners for the service.
     * This method should be called during the service initialization phase
     * 注册该服务的所有监听器。
     * 此方法应在服务初始化阶段调用。
     */
    void registerListeners(AlleyPlugin plugin);
}