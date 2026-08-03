package dev.revere.alley.adapter.core.internal;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.adapter.core.Core;
import dev.revere.alley.adapter.core.CoreAdapter;
import dev.revere.alley.adapter.core.CoreType;
import dev.revere.alley.bootstrap.AlleyContext;
import dev.revere.alley.bootstrap.annotation.Service;
import lombok.Getter;
import me.activated.core.plugin.AquaCoreAPI;
import services.plasma.helium.api.HeliumAPI;
import xyz.refinedev.phoenix.SharedAPI;

/**
 * @author Emmy
 * @project Alley
 * @since 26/04/2025
 */
@Getter
@Service(provides = CoreAdapter.class, priority = 60)
public class CoreAdapterImpl implements CoreAdapter {

    private final AlleyPlugin plugin;
    private Core core;

    /**
     * Constructor for DI. Receives the main bootstrap instance.
     * DI 构造函数。接收主要的引导程序实例。
     */
    public CoreAdapterImpl(AlleyPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initialize(AlleyContext context) {
        this.core = this.determineCore();
    }

    @Override
    public Core getCore() {
        if (this.core == null) {
            throw new IllegalStateException("CoreAdapter has not been initialized yet.");
        }
        return this.core;
    }

    /**
     * Determines the server implementation to use based on the enabled plugins.
     * 根据启用的插件确定要使用的服务器实现。
     *
     * @return The selected server implementation.
     *         选定的服务器实现。
     */
    private Core determineCore() {
        Core selectedCore = new DefaultCoreImpl(this.plugin);

        for (CoreType coreType : CoreType.values()) {
            if (this.plugin.getServer().getPluginManager().isPluginEnabled(coreType.getPluginName())) {
                switch (coreType) {
                    case PHOENIX:
                        selectedCore = new PhoenixCoreImpl(SharedAPI.getInstance());
                        break;
                    case AQUA:
                        selectedCore = new AquaCoreImpl(AquaCoreAPI.INSTANCE, this.plugin);
                        break;
                    case HELIUM:
                        selectedCore = new HeliumCoreImpl(HeliumAPI.INSTANCE);
                        break;
                }
                break;
            }
        }
        return selectedCore;
    }
}