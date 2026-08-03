package dev.revere.alley.feature.cosmetic.model;

import dev.revere.alley.feature.cosmetic.annotation.CosmeticData;
import lombok.Data;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * @author Remi
 * @project Alley
 * @date 6/23/2025
 */
@Data
public abstract class BaseCosmetic implements Cosmetic {
    private final CosmeticType type;
    private final String name;
    private final String description;
    private final String permission;
    private final Material icon;
    private final int slot;
    private final int price;

    public BaseCosmetic() {
        CosmeticData data = getClass().getAnnotation(CosmeticData.class);
        if (data != null) {
            this.type = data.type();
            this.name = data.name();
            this.description = data.description();
            this.permission = !data.permission().isEmpty() ? data.permission() : data.name().toLowerCase();
            this.icon = data.icon();
            this.slot = data.slot();
            this.price = data.price();
        } else {
            throw new IllegalStateException("CosmeticData annotation missing");
        }
    }

    /**
     * Gets the fully formed permission node for this cosmetic.
     * 获取此装饰品的完整权限节点。
     *
     * @return The full permission string.
     *         完整权限字符串。
     */
    @Override
    public String getPermission() {
        return String.format("alley.cosmetic.%s.%s", this.type.getPermissionKey(), this.permission);
    }

    /**
     * Gets the description lore for display in a menu.
     * 获取在菜单中显示的描述文本(lore)。
     * Subclasses can override this to provide custom lore.
     * 子类可以重写此方法以提供自定义的文本(lore)。
     *
     * @return A list of strings representing the description part of an item's lore.
     *         一个字符串列表，表示物品文本(lore)的描述部分。
     */
    public List<String> getDisplayLore() {
        return Collections.singletonList("&7" + this.getDescription());
    }
}