package dev.revere.alley.feature.queue;

import lombok.Getter;

/**
 * @author Emmy
 * @project Alley
 * @since 13/03/2025
 */
@Getter
public enum QueueType {
    UNRANKED("Solo Unranked Queues"),
    DUOS("Duo Unranked Queues"),
    RANKED("Solo Ranked Queues"),
    BOTS("Bots Unranked Queues"),
    FFA("FFA Unranked Queues"),

    ;

    private final String menuTitle;

    /**
     * Constructor for the EnumQueueType class.
     * EnumQueueType 枚举类的构造函数。
     *
     * @param menuTitle The title of the menu.
     *                  菜单的标题。
     */
    QueueType(String menuTitle) {
        this.menuTitle = menuTitle;
    }
}