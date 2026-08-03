package dev.revere.alley.feature.cosmetic.internal.repository;

import dev.revere.alley.feature.cosmetic.internal.repository.impl.projectiletrail.ProjectileTrail;
import dev.revere.alley.feature.cosmetic.internal.repository.impl.projectiletrail.FlameTrail;

/**
 * @author Remi
 * 作者 Remi
 * @project Alley
 * 项目 Alley
 * @date 6/23/2025
 * 日期 6/23/2025
 */
public class ProjectileTrailRepository extends BaseCosmeticRepository<ProjectileTrail> {
    public ProjectileTrailRepository() {
        this.registerCosmetic(FlameTrail.class);
    }
}