package dev.revere.alley.feature.abilities;

import dev.revere.alley.bootstrap.lifecycle.Service;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Set;

/**
 * @author Remi
 * 作者 Remi
 * @project alley-practice
 * 项目 alley-practice
 * @date 2/07/2025
 * 日期 2/07/2025
 */
public interface AbilityService extends Service {
    /**
     * Retrieves a specific ability instance by its class.
     * 根据其类检索特定的能力实例。
     *
     * @param abilityClass The class of the ability to get.
     *                    要获取的能力的类。
     * @param <T>          The type of the ability.
     *                     能力的类型。
     * @return The singleton instance of the ability, or null if not found.
     *         能力的单例实例，如果未找到则返回 null。
     */
    <T extends Ability> T getAbility(Class<T> abilityClass);

    /**
     * Creates an ItemStack for a given ability, configured with its name, lore, and material.
     * 为指定的能力创建一个 ItemStack，配置其名称、描述和材质。
     *
     * @param abilityKey The key of the ability in the config (e.g., "GUARDIAN_ANGEL").
     *                   配置中能力的键（例如 "GUARDIAN_ANGEL"）。
     * @param amount     The amount for the ItemStack.
     *                   ItemStack 的数量。
     * @return The configured ItemStack for the ability.
     *         为能力配置的 ItemStack。
     */
    ItemStack getAbilityItem(String abilityKey, int amount);

    /**
     * Gets the configured display name for an ability.
     * 获取能力的配置显示名称。
     *
     * @param abilityKey The key of the ability in the config.
     *                   配置中能力的键。
     * @return The formatted display name.
     *         格式化后的显示名称。
     */
    String getDisplayName(String abilityKey);

    /**
     * Gets the configured description lore for an ability.
     * 获取能力的配置描述文本。
     *
     * @param abilityKey The key of the ability in the config.
     *                   配置中能力的键。
     * @return A list of lore strings.
     *         描述字符串列表。
     */
    List<String> getDescription(String abilityKey);

    /**
     * Gets a set of all ability keys from the configuration.
     * 从配置中获取所有能力键的集合。
     *
     * @return A set of ability config keys.
     *         能力配置键的集合。
     */
    Set<String> getAbilityKeys();

    /**
     * Gives a player a specified amount of an ability item.
     * 向玩家给予指定数量的能力物品。
     */
    void giveAbility(CommandSender sender, Player player, String key, String abilityName, int amount);

    /**
     * Sends the configured "used ability" message to a player.
     * 向玩家发送配置的"已使用能力"消息。
     */
    void sendPlayerMessage(Player player, String abilityKey);

    /**
     * Sends the configured "hit by ability" message to a target player.
     * 向目标玩家发送配置的"被能力命中"消息。
     */
    void sendTargetMessage(Player target, Player player, String abilityKey);

    /**
     * Sends the "cooldown active" message to a player.
     * 向玩家发送"冷却中"消息。
     */
    void sendCooldownMessage(Player player, String abilityName, String cooldown);

    /**
     * Schedules and sends the "cooldown expired" message to a player.
     * 安排并向玩家发送"冷却已过期"消息。
     */
    void sendCooldownExpiredMessage(Player player, String abilityName, String abilityKey);
}