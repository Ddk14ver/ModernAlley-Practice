package dev.revere.alley.feature.cosmetic.internal.repository.impl.soundeffect;

import dev.revere.alley.feature.cosmetic.model.BaseCosmetic;
import org.bukkit.entity.Player;

/**
 * @author Remi
 * @project alley-practice
 * @date 6/08/2025
 */
public abstract class BaseSoundEffect extends BaseCosmetic {
    /**
     * Executes the sound effect for the specified player on kill.
     */
    public abstract void execute(Player player);

    /**
     * Executes the death sound effect for the specified player.
     * Default is no-op — override for kill+death sound packs.
     */
    public void executeDeath(Player player) {}
}