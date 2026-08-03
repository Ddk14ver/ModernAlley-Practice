package dev.revere.alley.core.profile.enums;

import lombok.Getter;

/**
 * @author Emmy
 * @project Alley
 * @date 22/10/2024 - 11:57
 * 聊天频道枚举，定义玩家可用的聊天频道类型。
 * Chat channel enum, defining the types of chat channels available to players.
 */
@Getter
public enum ChatChannel {
    GLOBAL("Global", "The players chat channel is set to default."),
    PARTY("Party", "The players chat channel is set to party."),
    CLAN("Clan", "The players chat channel is set to clan.");

    private final String name;
    private final String description;

    /**
     * Constructor for the EnumChatChannel class.
     * EnumChatChannel 类的构造函数。
     *
     * @param name        The name of the chat channel.
     *                    聊天频道的名称。
     * @param description The description of the chat channel.
     *                    聊天频道的描述。
     */
    ChatChannel(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * Get the chat channels sorted.
     * 获取排序后的聊天频道列表。
     *
     * @return The chat channels sorted.
     *         排序后的聊天频道。
     */
    public static String getChatChannelsSorted() {
        StringBuilder stringBuilder = new StringBuilder();
        for (ChatChannel chatChannel : ChatChannel.values()) {
            stringBuilder.append(chatChannel.getName()).append(", ");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 2);
        return stringBuilder.toString();
    }

    /**
     * Get the exact chat channel.
     * 获取精确的聊天频道。
     *
     * @param chatChannel The chat channel to get.
     *                    要获取的聊天频道。
     * @return The exact chat channel.
     *         精确的聊天频道。
     */
    public static String getExactChatChannel(String chatChannel, boolean upperCase) {
        for (ChatChannel enumChatChannel : ChatChannel.values()) {
            if (enumChatChannel.getName().equalsIgnoreCase(chatChannel)) {
                if (upperCase) {
                    return enumChatChannel.toString().toUpperCase();
                } else {
                    return enumChatChannel.getName();
                }
            }
        }
        return null;
    }
}