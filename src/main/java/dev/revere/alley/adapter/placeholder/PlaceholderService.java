package dev.revere.alley.adapter.placeholder;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.bootstrap.lifecycle.Service;

/**
 * @author Emmy
 * @project alley-practice
 * @since 17/07/2025
 */
public interface PlaceholderService extends Service {
    /**
     * Registers a papi expansion bootstrap with the Alley bootstrap.
     * 向 Alley 引导程序注册 papi 扩展引导程序。
     *
     * @param plugin The Alley bootstrap instance to register.
     *               要注册的 Alley 引导程序实例。
     */
    void registerExpansion(AlleyPlugin plugin);
}
