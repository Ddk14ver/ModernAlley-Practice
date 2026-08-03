package dev.revere.alley.feature.cosmetic.internal.repository;

import dev.revere.alley.feature.cosmetic.internal.repository.impl.killmessage.*;

/**
 * @author Remi
 * 作者 Remi
 * @project alley-practice
 * 项目 alley-practice
 * @date 27/06/2025
 * 日期 27/06/2025
 */
public class KillMessageRepository extends BaseCosmeticRepository<KillMessagePack> {
    public KillMessageRepository() {
        this.registerCosmetic(NoneKillMessages.class);
        this.registerCosmetic(SaltyKillMessages.class);
        this.registerCosmetic(YeetKillMessages.class);
        this.registerCosmetic(NerdKillMessages.class);
        this.registerCosmetic(SpigotCommunityKillMessages.class);
    }
}