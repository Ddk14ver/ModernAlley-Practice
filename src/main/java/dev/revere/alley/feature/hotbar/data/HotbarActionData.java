package dev.revere.alley.feature.hotbar.data;

import dev.revere.alley.feature.hotbar.HotbarAction;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Emmy
 * @project alley-practice
 * @since 21/07/2025
 */
@Getter
@Setter
public class HotbarActionData {
    private HotbarAction action;
    private String command;
    private String menuName;

    /**
     * Constructor for the HotbarActionData class.
     * HotbarActionData 类的构造函数。
     *
     * @param action The action to be performed by the hotbar item.
     *               热键栏物品要执行的操作。
     */
    public HotbarActionData(HotbarAction action) {
        this.action = action;
        this.command = null;
        this.menuName = null;
    }
}
