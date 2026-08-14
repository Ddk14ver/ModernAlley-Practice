package dev.revere.alley.core.profile.progress.internal;

import dev.revere.alley.bootstrap.annotation.Service;
import dev.revere.alley.core.profile.progress.PlayerProgress;
import dev.revere.alley.core.profile.progress.ProgressService;
import dev.revere.alley.feature.division.Division;
import dev.revere.alley.feature.division.model.DivisionTier;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.data.ProfileData;
import dev.revere.alley.core.profile.data.types.ProfileUnrankedKitData;

import java.util.List;

/**
 * @author Remi
 * @project alley-practice
 * @date 2/07/2025
 * 进度服务实现类，计算玩家在特定套件中的进度。
 * Progress service implementation, calculating a player's progress in a specific kit.
 */
@Service(provides = ProgressService.class, priority = 410)
public class ProgressServiceImpl implements ProgressService {
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
    @Override
    public PlayerProgress calculateProgress(Profile profile, String kitName) {
        ProfileData profileData = profile.getProfileData();
        ProfileUnrankedKitData kitData = profileData.getUnrankedKitData().get(kitName);

        if (kitData == null) {
            return new PlayerProgress(0, 0, 0, "N/A", true);
        }

        // Division progression counts wins from every mode (unranked solo/duo, ranked, tournament).
        int wins = profileData.getKitWins(kitName);
        Division currentDivision = kitData.getDivision();

        if (currentDivision == null) {
            return new PlayerProgress(wins, 0, 0, "N/A", true);
        }

        DivisionTier currentTier = kitData.getTier();

        List<DivisionTier> tiers = currentDivision.getTiers();
        int tierIndex = tiers.indexOf(currentTier);

        // The floor is the required wins of the player's current tier; the bar/percentage measures
        // progress from this floor up to the next tier's required wins.
        // 起点是当前层级所需胜场；进度条/百分比衡量从该起点推进到下一层级所需胜场的进度。
        int currentTierWins = currentTier == null ? 0 : currentTier.getRequiredWins();

        int nextTierWins;
        boolean isMaxRank = false;

        if (tierIndex < tiers.size() - 1) {
            nextTierWins = tiers.get(tierIndex + 1).getRequiredWins();
        } else {
            Division nextDivision = profile.getNextDivision(kitName);
            if (nextDivision != null) {
                nextTierWins = nextDivision.getTiers().get(0).getRequiredWins();
            } else {
                nextTierWins = currentTierWins;
                isMaxRank = true;
            }
        }

        String nextRankName = isMaxRank ? "Max Rank" : profile.getNextDivisionAndTier(kitName);

        return new PlayerProgress(wins, currentTierWins, nextTierWins, nextRankName, isMaxRank);
    }
}