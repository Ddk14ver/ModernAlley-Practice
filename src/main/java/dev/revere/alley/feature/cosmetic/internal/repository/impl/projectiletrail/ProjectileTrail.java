package dev.revere.alley.feature.cosmetic.internal.repository.impl.projectiletrail;

import dev.revere.alley.feature.cosmetic.model.BaseCosmetic;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * @author Remi
 * 作者：Remi
 * @project Alley
 * 项目：Alley
 * @date 6/23/2025
 * 日期：2025年6月23日
 */
public abstract class ProjectileTrail extends BaseCosmetic {
    /**
     * This method is called repeatedly by a trail-tracking task.
     * 此方法由轨迹跟踪任务重复调用。
     * Implementations should define what particle to spawn at the projectile's location.
     * 实现类应定义在弹射物位置生成的粒子。
     *
     * @param location The current location of the projectile.
     *                弹射物的当前位置。
     */
    public abstract void spawnTrailParticle(Location location);
}