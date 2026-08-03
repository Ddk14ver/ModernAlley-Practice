package dev.revere.alley.common.reflect;

import org.bukkit.entity.Player;

import java.lang.reflect.Field;

/**
 * @author Emmy
 * @project Alley
 * @since 03/04/2025
 */
public interface Reflection {
    /**
     * Get a field from a class using reflect.
     * 使用反射从类中获取字段。
     *
     * @param clazz     the class to search in
     *                  要搜索的类
     * @param fieldName the field's name
     *                  字段名称
     * @return the field object
     *         字段对象
     * @throws NoSuchFieldException if the field doesn't exist
     *                              如果字段不存在
     */
    default Field getField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field;
    }

    /**
     * Set the value of a field via reflect.
     * 通过反射设置字段的值。
     *
     * @param field    the field to set
     *                 要设置的字段
     * @param instance the object instance
     *                 对象实例
     * @param value    the value to set
     *                 要设置的值
     * @throws IllegalAccessException if the field is not accessible
     *                                如果字段不可访问
     */
    default void setField(Field field, Object instance, Object value) throws IllegalAccessException {
        field.set(instance, value);
    }

    /**
     * Sends a packet to a player via ProtocolLib or reflect.
     * 通过 ProtocolLib 或反射向玩家发送数据包。
     * This default implementation uses ProtocolLib's PacketContainer.
     * 此默认实现使用 ProtocolLib 的 PacketContainer。
     *
     * @param player the player to send the packet to
     *               要发送数据包的玩家
     * @param packet the packet to send (ProtocolLib PacketContainer or NMS packet)
     *               要发送的数据包（ProtocolLib PacketContainer 或 NMS 数据包）
     */
    default void sendPacket(Player player, Object packet) {
        try {
            com.comphenix.protocol.ProtocolLibrary.getProtocolManager().sendServerPacket(player,
                (com.comphenix.protocol.events.PacketContainer) packet);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send packet to player " + player.getName(), e);
        }
    }
}