package dev.revere.alley.feature.explosives;

import dev.revere.alley.bootstrap.lifecycle.Service;

/**
 * @author Remi
 * @作者 Remi
 * @project alley-practice
 * @项目 alley-practice
 * @date 2/07/2025
 * @日期 2/07/2025
 */
public interface ExplosiveService extends Service {
    /**
     * Gets the configured knockback range of TNT explosions.
     * 获取配置的TNT爆炸击退范围。
     *
     * @return The configured knockback range of explosions.
     *         配置的爆炸击退范围。
     */
    double getTntExplosionRange();

    /**
     * Gets the configured horizontal knockback strength for fireballs.
     * 获取配置的火球水平击退强度。
     *
     * @return The configured horizontal knockback strength.
     *         配置的水平击退强度。
     */
    double getHorizontalFireballKnockback();

    /**
     * Gets the configured vertical knockback strength for fireballs.
     * 获取配置的火球垂直击退强度。
     *
     * @return The configured vertical knockback strength.
     *         配置的垂直击退强度。
     */
    double getVerticalFireballKnockback();

    /**
     * Gets the configured range value, possibly for explosion radius or effect distance.
     * 获取配置的范围值，可能用于爆炸半径或效果距离。
     *
     * @return The configured range value (purpose may vary).
     *         配置的范围值（用途可能有所不同）。
     */
    double getFireballExplosionRange();

    /**
     * Gets the configured speed value, likely for projectiles.
     * 获取配置的速度值，可能用于投射物。
     *
     * @return The configured speed value, likely for projectiles.
     *         配置的速度值，可能用于投射物。
     */
    double getFireballThrowSpeed();

    /**
     * Gets the configured fuse time for TNT in ticks.
     * 获取配置的TNT引信时间（以tick为单位）。
     *
     * @return The configured fuse time for TNT in ticks.
     *         配置的TNT引信时间（以tick为单位）。
     */
    int getTntFuseTicks();

    /**
     * Method to update the horizontal knockback value for fireballs.
     * 更新火球水平击退值的方法。
     *
     * @param horizontalFireballKnockback The new horizontal knockback strength.
     *                                    新的水平击退强度。
     */
    void setHorizontalFireballKnockback(double horizontalFireballKnockback);

    /**
     * Method to update the vertical knockback value for fireballs.
     * 更新火球垂直击退值的方法。
     *
     * @param verticalFireballKnockback The new vertical knockback strength.
     *                                  新的垂直击退强度。
     */
    void setVerticalFireballKnockback(double verticalFireballKnockback);

    /**
     * Method to update the explosion range value for fireballs.
     * 更新火球爆炸范围值的方法。
     *
     * @param fireballExplosionRange The new explosion range for fireballs.
     *                               新的火球爆炸范围。
     */
    void setFireballExplosionRange(double fireballExplosionRange);

    /**
     * Method to update the throw speed value for fireballs.
     * 更新火球投掷速度值的方法。
     *
     * @param fireballThrowSpeed The new throw speed for fireballs.
     *                           新的火球投掷速度。
     */
    void setFireballThrowSpeed(double fireballThrowSpeed);

    /**
     * Method to update the affected range value for TNT explosions.
     * 更新TNT爆炸影响范围值的方法。
     *
     * @param tntExplosionRange The new knockback range for explosions.
     *                          新的爆炸击退范围。
     */
    void setTntExplosionRange(double tntExplosionRange);

    /**
     * Method to update the fuse time for TNT in ticks.
     * 更新TNT引信时间（以tick为单位）的方法。
     *
     * @param tntFuseTicks The new fuse time for TNT in ticks.
     *                     新的TNT引信时间（以tick为单位）。
     */
    void setTntFuseTicks(int tntFuseTicks);

    /**
     * Saves the current explosive settings to the configuration file.
     * 将当前的爆炸物设置保存到配置文件中。
     */
    void save();
}