package dev.revere.alley.feature.cosmetic.internal.repository;

import dev.revere.alley.feature.cosmetic.internal.repository.impl.mvpmusic.*;
import dev.revere.alley.feature.cosmetic.model.BaseCosmetic;
/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 13/07/2026
 */
public class MVPMusicRepository extends BaseCosmeticRepository<BaseCosmetic> {
    public MVPMusicRepository() {
        registerCosmetic(NoneMVPMusic.class);
        registerCosmetic(OnMyOwnMusic.class);
        registerCosmetic(EZ4ENCEMusic.class);
        registerCosmetic(HaruhikageMusic.class);
        registerCosmetic(DashstarMusic.class);
        registerCosmetic(FlashbangMusic.class);
        registerCosmetic(InhumanMusic.class);
        registerCosmetic(BoundByLoveMusic.class);
        registerCosmetic(GirlBandCryMusic.class);
    }
}
