package dev.revere.alley.feature.layout.data;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

/**
 * Data class representing a saved kit layout.
 * 表示已保存套件布局的数据类。
 * @author Emmy
 * @project Alley
 * @since 03/05/2025
 */
@Getter
@Setter
public class LayoutData {
    private String name;
    private String displayName;
    private ItemStack[] items;

    /**
     * Constructor for the LayoutData class.
     * LayoutData 类的构造方法。
     *
     * @param name        The name of the layout.
     *                    布局的名称。
     * @param displayName The display name of the layout.
     *                    布局的显示名称。
     * @param items       The items in the layout.
     *                    布局中的物品。
     */
    public LayoutData(String name, String displayName, ItemStack[] items) {
        this.name = name;
        this.displayName = displayName;
        this.items = items;
    }
}