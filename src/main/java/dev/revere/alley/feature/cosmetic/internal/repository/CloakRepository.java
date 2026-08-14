package dev.revere.alley.feature.cosmetic.internal.repository;

import dev.revere.alley.feature.cosmetic.internal.repository.impl.cloak.*;

/**
 * @author Remi
 * @project alley-practice
 * @date 4/08/2025
 */
public class CloakRepository extends BaseCosmeticRepository<BaseCloak> {
    public CloakRepository() {
        registerCosmetic(NoneCloak.class);
        registerCosmetic(AngelWingsCloak.class);
        registerCosmetic(HaloCloak.class);
        registerCosmetic(VailCloak.class);
    }
}
