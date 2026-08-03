package dev.revere.alley.feature.cosmetic.internal.repository.impl.mvpmusic;

import dev.revere.alley.feature.cosmetic.model.BaseCosmetic;
import org.bukkit.entity.Player;

import java.util.Collection;

/**
 * Base class for MVP music cosmetics.
 * Resource pack must include the .ogg file registered in sounds.json.
 *
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 13/07/2026
 */
public abstract class BaseMVPMusic extends BaseCosmetic {

    private final String soundEventName;

    protected BaseMVPMusic(String soundEventName) {
        this.soundEventName = soundEventName;
    }

    public String getSoundEventName() {
        return soundEventName;
    }

    public void play(Collection<? extends Player> players) {
        if (players.isEmpty()) return;
        for (Player player : players) {
            if (player != null && player.isOnline()) {
                MVPMusicSession.play(player, soundEventName);
            }
        }
    }
}
