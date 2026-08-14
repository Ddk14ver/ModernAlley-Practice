package dev.revere.alley.feature.cosmetic.internal.repository;

import dev.revere.alley.feature.cosmetic.internal.repository.impl.suit.*;

/**
 * @author Remi
 * @project alley-practice
 * @date 4/08/2025
 */
public class SuitRepository extends BaseCosmeticRepository<BaseSuit> {
    public SuitRepository() {
        registerCosmetic(NoneSuit.class);
        registerCosmetic(SpacemanSuit.class);
        registerCosmetic(SpidermanSuit.class);
        registerCosmetic(HulkSuit.class);
        registerCosmetic(GarfieldSuit.class);
        registerCosmetic(OdieSuit.class);
        registerCosmetic(VailSuit.class);
        registerCosmetic(MonkeySuit.class);
    }
}
