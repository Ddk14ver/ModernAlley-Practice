package dev.revere.alley.core.profile.progress;

import dev.revere.alley.bootstrap.lifecycle.Service;
import dev.revere.alley.core.profile.Profile;

/**
 * @author Remi
 * @project alley-practice
 * @date 3/07/2025
 * 进度服务接口，用于计算玩家在特定套件中的晋级进度。
 * Progress service interface for calculating a player's rank progression in a specific kit.
 */
public interface ProgressService extends Service {
    /**
     * Calculates a player's progress for a given kit.
     * 计算玩家在特定套件中的进度。
     *
     * @param profile The player's profile, containing their current stats.
     *                玩家的档案，包含其当前统计数据。
     * @param kitName The name of the kit to check progress for.
     *                要检查进度的套件名称。
     * @return A PlayerProgress object containing all calculated data.
     *         包含所有计算数据的 PlayerProgress 对象。
     */
    PlayerProgress calculateProgress(Profile profile, String kitName);
}