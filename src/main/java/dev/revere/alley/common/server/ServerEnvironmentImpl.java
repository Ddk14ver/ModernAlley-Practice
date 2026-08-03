package dev.revere.alley.common.server;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.server.listener.ServerEnvironmentListener;
import dev.revere.alley.bootstrap.AlleyContext;
import dev.revere.alley.bootstrap.annotation.Service;
import dev.revere.alley.common.text.CC;
import lombok.Getter;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

/**
 * This class is made for preparing the server environment.
 * 此类用于准备服务器环境。
 * Mainly during startup to pre-setup the server with specific settings.
 * 主要在启动期间使用特定设置预配置服务器。
 *
 * @author Emmy
 * @project Alley
 * @since 03/04/2025
 */
@Getter
@Service(provides = ServerEnvironment.class, priority = 10)
public class ServerEnvironmentImpl implements ServerEnvironment {
    private final AlleyPlugin plugin;

    private final boolean doDaylightCycle;
    private final boolean doWeatherCycle;
    private final boolean doMobSpawning;
    private final boolean doMobLoot;
    private final boolean removeDroppedItemsOnEnable;

    /**
     * Constructor for DI. Receives the main bootstrap instance.
     * 依赖注入构造函数。接收主引导实例。
     * Note for emmy: The boolean flags are hardcoded here to match the original instantiation logic.
     * 给emmy的注意事项：布尔标志在此处硬编码以匹配原始的实例化逻辑。
     */
    public ServerEnvironmentImpl(AlleyPlugin plugin) {
        this.plugin = plugin;
        this.doDaylightCycle = false;
        this.doWeatherCycle = false;
        this.doMobSpawning = false;
        this.doMobLoot = false;
        this.removeDroppedItemsOnEnable = true;
    }

    @Override
    public void initialize(AlleyContext context) {
        this.plugin.getServer().getPluginManager().registerEvents(new ServerEnvironmentListener(), this.plugin);
        this.setupWorldDefaults();
    }

    @Override
    public void shutdown(AlleyContext context) {
        this.disconnectPlayers();
        this.clearEntities(EntityType.ITEM);
    }

    /**
     * Applies default settings to all worlds on the server.
     * 将默认设置应用到服务器上的所有世界。
     */
    private void setupWorldDefaults() {
        for (World world : this.plugin.getServer().getWorlds()) {
            world.setDifficulty(Difficulty.HARD);
            world.setTime(6000);
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, this.doDaylightCycle);
            world.setGameRule(GameRule.DO_WEATHER_CYCLE, this.doWeatherCycle);
            world.setGameRule(GameRule.DO_MOB_SPAWNING, this.doMobSpawning);
            world.setGameRule(GameRule.DO_MOB_LOOT, this.doMobLoot);

            if (this.removeDroppedItemsOnEnable) {
                clearEntities(world, EntityType.ITEM);
            }
        }
    }

    @Override
    public void clearEntities(EntityType entityType) {
        for (World world : this.plugin.getServer().getWorlds()) {
            clearEntities(world, entityType);
        }
    }

    @Override
    public void clearAllEntities() {
        for (World world : this.plugin.getServer().getWorlds()) {
            world.getEntities().stream()
                    .filter(entity -> !(entity instanceof Player))
                    .forEach(Entity::remove);
        }
    }

    /**
     * Kicks all online players with a restart message.
     * 使用重启消息踢出所有在线玩家。
     */
    private void disconnectPlayers() {
        this.plugin.getServer().getOnlinePlayers().forEach(player ->
                player.kickPlayer(CC.translate("&cThe server is restarting."))
        );
    }

    /**
     * Private helper to clear entities from a single world.
     * 从单个世界清除实体的私有辅助方法。
     */
    private void clearEntities(World world, EntityType entityType) {
        world.getEntitiesByClass(entityType.getEntityClass()).forEach(Entity::remove);
    }
}