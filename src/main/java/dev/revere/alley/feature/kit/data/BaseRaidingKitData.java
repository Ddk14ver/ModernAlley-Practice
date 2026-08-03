package dev.revere.alley.feature.kit.data;

import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.match.model.BaseRaiderRole;
import lombok.Getter;

/**
 * @author Emmy
 * @project Alley
 * @since 16/06/2025
 */
@Getter
public class BaseRaidingKitData {
    private Kit raiderKit;
    private Kit trapperKit;

    public BaseRaidingKitData() {
        this.raiderKit = null;
        this.trapperKit = null;
    }

    /**
     * Sets the kit for a specific raider role.
     * 为特定的掠夺者角色设置工具包。
     *
     * @param kit  The kit to set.
     * @param kit  要设置的工具包。
     * @param role The role of the raider (RAIDER or TRAPPER).
     * @param role 掠夺者的角色（RAIDER 或 TRAPPER）。
     * @throws IllegalArgumentException if the role is not recognized.
     * @throws IllegalArgumentException 如果角色未被识别。
     */
    public void setKit(Kit kit, BaseRaiderRole role) {
        if (role == BaseRaiderRole.RAIDER) {
            this.raiderKit = kit;
        } else if (role == BaseRaiderRole.TRAPPER) {
            this.trapperKit = kit;
        } else {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
    }

    /**
     * Gets the kit for a specific raider role.
     * 获取特定掠夺者角色的工具包。
     *
     * @param role The role of the raider (RAIDER or TRAPPER).
     * @param role 掠夺者的角色（RAIDER 或 TRAPPER）。
     * @return The kit associated with the specified role.
     * @return 与指定角色关联的工具包。
     * @throws IllegalArgumentException if the role is not recognized.
     * @throws IllegalArgumentException 如果角色未被识别。
     */
    public Kit getKitAssociatedWithRole(BaseRaiderRole role) {
        if (role == BaseRaiderRole.RAIDER) {
            return this.raiderKit;
        } else if (role == BaseRaiderRole.TRAPPER) {
            return this.trapperKit;
        } else {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
    }
}