package dev.revere.alley.adapter.placeholder.internal;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.adapter.placeholder.PlaceholderService;
import dev.revere.alley.bootstrap.AlleyContext;
import dev.revere.alley.bootstrap.annotation.Service;
import dev.revere.alley.common.logger.Logger;

/**
 * @author Emmy
 * @project alley-practice
 * @since 17/07/2025
 */
@Service(provides = PlaceholderService.class, priority = 430)
public class PlaceholderServiceImpl implements PlaceholderService {

    @Override
    public void initialize(AlleyContext context) {
        this.registerExpansion(context.getPlugin());
    }

    @Override
    public void registerExpansion(AlleyPlugin plugin) {
        if (plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            Logger.info("PlaceholderAPI is not installed! Alley Placeholder Expansion will not be registered.");
            return;
        }

        Logger.logTime(AlleyPlaceholderExpansion.class.getSimpleName(), () -> {
            AlleyPlaceholderExpansion expansion = new AlleyPlaceholderExpansion(plugin);
            expansion.register();
        });
    }
}