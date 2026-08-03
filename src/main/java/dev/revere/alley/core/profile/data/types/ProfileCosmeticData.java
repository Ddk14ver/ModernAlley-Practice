package dev.revere.alley.core.profile.data.types;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.feature.cosmetic.model.BaseCosmetic;
import dev.revere.alley.feature.cosmetic.model.CosmeticType;
import lombok.Getter;
import lombok.Setter;

import java.util.EnumMap;
import java.util.Map;

/**
 * @author Remi
 * @project Alley
 * @date 6/1/2024
 */
@Getter
@Setter
public class ProfileCosmeticData {
    protected final AlleyPlugin plugin = AlleyPlugin.getInstance();
    private Map<CosmeticType, String> selectedCosmetics;
    private java.util.Set<String> purchasedCosmetics;

    public ProfileCosmeticData() {
        this.selectedCosmetics = new EnumMap<>(CosmeticType.class);
        this.purchasedCosmetics = new java.util.HashSet<>();

        for (CosmeticType type : CosmeticType.values()) {
            this.selectedCosmetics.put(type, "None");
        }
    }

    public boolean isPurchased(String cosmeticName) {
        return purchasedCosmetics.contains(cosmeticName.toLowerCase());
    }

    public void addPurchased(String cosmeticName) {
        purchasedCosmetics.add(cosmeticName.toLowerCase());
    }

    public java.util.Set<String> getPurchasedCosmetics() {
        return purchasedCosmetics;
    }

    public void setPurchasedCosmetics(java.util.Set<String> set) {
        this.purchasedCosmetics = set;
    }

    /**
     * Sets the active cosmetic for the correct category using its type.
     * 使用其类型为正确的类别设置当前激活的装饰品。
     *
     * @param cosmetic The cosmetic object to select.
     *                 要选择的装饰品对象。
     */
    public void setSelected(BaseCosmetic cosmetic) {
        if (cosmetic == null) return;
        this.selectedCosmetics.put(cosmetic.getType(), cosmetic.getName());
    }

    /**
     * Gets the name of the selected cosmetic for a given type.
     * 获取给定类型的已选择装饰品的名称。
     *
     * @param type The CosmeticType category to check.
     *             要检查的装饰品类型类别。
     * @return The name of the selected cosmetic, or "None" if not found.
     *         已选择装饰品的名称，如果未找到则返回"None"。
     */
    public String getSelected(CosmeticType type) {
        return this.selectedCosmetics.getOrDefault(type, "None");
    }

    /**
     * Checks if a specific cosmetic is currently selected.
     * 检查特定装饰品是否当前被选中。
     *
     * @param cosmetic The cosmetic to check.
     *                 要检查的装饰品。
     * @return true if it is the currently selected cosmetic for its type.
     *         如果它是其类型中当前选中的装饰品则返回true。
     */
    public boolean isSelected(BaseCosmetic cosmetic) {
        if (cosmetic == null) return false;
        String selectedName = getSelected(cosmetic.getType());
        return cosmetic.getName().equals(selectedName);
    }

    public String getSelectedKillEffect() {
        return getSelected(CosmeticType.KILL_EFFECT);
    }

    public String getSelectedSoundEffect() {
        return getSelected(CosmeticType.SOUND_EFFECT);
    }

    public String getSelectedProjectileTrail() {
        return getSelected(CosmeticType.PROJECTILE_TRAIL);
    }

    public String getSelectedKillMessage() {
        return getSelected(CosmeticType.KILL_MESSAGE);
    }

    public String getSelectedSuit() {
        return getSelected(CosmeticType.SUIT);
    }

    public String getSelectedCloak() {
        return getSelected(CosmeticType.CLOAK);
    }
}
