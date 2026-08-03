package dev.revere.alley.library.command;

import dev.revere.alley.bootstrap.lifecycle.Service;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * 命令框架接口
 * @author Remi
 * @project alley-practice
 * @date 2/07/2025
 */
public interface CommandFramework extends Service {
    /**
     * Manually registers all command methods within a given object instance.
     * 手动注册给定对象实例中的所有命令方法。
     * @param commandContainerObject The object instance containing @CommandData methods.
     *                               包含 @CommandData 方法的对象实例。
     */
    void registerCommands(Object commandContainerObject);

    /**
     * Manually unregisters all command methods from a given object instance.
     * 手动从给定对象实例中取消注册所有命令方法。
     * @param commandContainerObject The object instance to unregister.
     *                               要取消注册的对象实例。
     */
    void unregisterCommands(Object commandContainerObject);

    /**
     * Generates and registers the main help topic for the bootstrap.
     * 生成并注册插件框架的主帮助主题。
     */
    void registerHelp();

    Map<String, Map.Entry<Method, Object>> getCommandMap();
}