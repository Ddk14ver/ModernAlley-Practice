package dev.revere.alley.feature.hotbar.data;

import dev.revere.alley.feature.hotbar.HotbarType;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Emmy
 * @project alley-practice
 * @since 21/07/2025
 */
@Getter
@Setter
public class HotbarTypeData {
    private HotbarType type;
    private int slot;

    private boolean enabled = false;

    /**
     * Constructor for the HotbarState class.
     * HotbarState 类的构造函数。
     *
     * @param type The name of the hotbar item type.
     *             热键栏物品类型的名称。
     * @param slot The hotbar slot of the type in the hotbar.
     *             该类型在热键栏中的槽位。
     */
    public HotbarTypeData(HotbarType type, int slot) {
        this.type = type;
        this.slot = slot;
    }
}