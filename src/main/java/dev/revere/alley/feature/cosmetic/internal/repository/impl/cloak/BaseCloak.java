package dev.revere.alley.feature.cosmetic.internal.repository.impl.cloak;

import dev.revere.alley.feature.cosmetic.model.BaseCosmetic;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * @author Remi
 * @author 雷米
 * @project alley-practice
 * @project alley-practice 项目
 * @date 4/08/2025
 * @date 2025年4月8日
 */
public abstract class BaseCloak extends BaseCosmetic {
    /**
     * Renders the particle effect for this specific cloak.
     * 为此特定斗篷渲染粒子效果。
     * This method is called repeatedly by the CloakService while the player stands still.
     * 当玩家站立不动时，此方法由 CloakService 重复调用。
     *
     * @param player The player to render the cloak for.
     *               要为其渲染斗篷的玩家。
     */
    public abstract void render(Player player);

    /**
     * Rotates a vector around the Y axis based on a given yaw angle.
     * 根据给定的偏航角绕 Y 轴旋转向量。
     * The original vector is modified and returned.
     * 原始向量被修改并返回。
     *
     * @param v   The vector to rotate.
     *            要旋转的向量。
     * @param yaw The yaw angle in degrees to rotate by.
     *            要旋转的偏航角（以度为单位）。
     * @return The same vector instance, now rotated.
     *         相同的向量实例，已旋转。
     */
    protected Vector rotateAroundAxisY(Vector v, float yaw) {
        double angle = Math.toRadians(yaw);
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double x = v.getX() * cos - v.getZ() * sin;
        double z = v.getX() * sin + v.getZ() * cos;
        return v.setX(x).setZ(z);
    }
}
