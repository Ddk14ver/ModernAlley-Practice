package dev.revere.alley.feature.cosmetic.internal.repository;

import dev.revere.alley.feature.cosmetic.internal.repository.impl.soundeffect.*;
import dev.revere.alley.feature.cosmetic.model.BaseCosmetic;
import lombok.Getter;

/**
 * @author Remi
 * @project Alley
 * @date 01/06/2024
 */
@Getter
public class SoundEffectRepository extends BaseCosmeticRepository<BaseSoundEffect> {
    public SoundEffectRepository() {
        this.registerCosmetic(NoneSoundEffect.class);
        this.registerCosmetic(StepSoundEffect.class);
        this.registerCosmetic(ExplosionSoundEffect.class);
        this.registerCosmetic(LevelSoundEffect.class);
        this.registerCosmetic(AmethystSoundEffect.class);
        this.registerCosmetic(BeaconSoundEffect.class);
        this.registerCosmetic(ChainSoundEffect.class);
        this.registerCosmetic(DecoratedPotSoundEffect.class);
        this.registerCosmetic(EndPortalSoundEffect.class);
        this.registerCosmetic(FrogspawnSoundEffect.class);
        this.registerCosmetic(BatSoundEffect.class);
    }
}