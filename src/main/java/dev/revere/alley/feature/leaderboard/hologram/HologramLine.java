package dev.revere.alley.feature.leaderboard.hologram;

import org.bukkit.Location;
import org.bukkit.entity.TextDisplay;

/**
 * @author Alley
 * @project Alley
 * @since 02/07/2025
 *
 * Represents a single line of a hologram, wrapping one TextDisplay entity.
 * 表示全息图的单行，包装一个TextDisplay实体。
 */
public class HologramLine {
    private TextDisplay entity;
    private Location location;
    private String text;
    private boolean spawned;

    public HologramLine() {
        this.spawned = false;
    }

    /**
     * Spawns the TextDisplay entity at the given location with the given text.
     */
    public void spawn(Location location, String text) {
        if (this.spawned && TextDisplayFactory.isAlive(this.entity)) {
            return;
        }

        if (this.entity == null || this.entity.isDead()) {
            this.entity = TextDisplayFactory.create(location, text);
        }
        this.location = location.clone();
        this.text = text;
        this.spawned = true;
    }

    /**
     * Despawns the TextDisplay entity.
     */
    public void despawn() {
        if (this.spawned) {
            TextDisplayFactory.safeRemove(this.entity);
            this.entity = null;
            this.spawned = false;
        }
    }

    /**
     * Updates only the text of this line.
     */
    public void updateText(String text) {
        if (TextDisplayFactory.isAlive(this.entity)) {
            TextDisplayFactory.updateText(this.entity, text);
            this.text = text;
        } else if (this.spawned && this.location != null) {
            // Respawn if entity was somehow removed
            this.entity = TextDisplayFactory.create(this.location, text);
            this.text = text;
        }
    }

    /**
     * Teleports this line to a new location.
     */
    public void teleport(Location location) {
        if (TextDisplayFactory.isAlive(this.entity)) {
            this.entity.teleport(location);
        }
        this.location = location.clone();
    }

    /**
     * Atomically updates location and text.
     */
    public void update(Location location, String text) {
        this.teleport(location);
        this.updateText(text);
    }

    public boolean isValid() {
        return this.spawned && TextDisplayFactory.isAlive(this.entity);
    }

    public double getY() {
        return this.location != null ? this.location.getY() : 0;
    }

    public TextDisplay getEntity() {
        return this.entity;
    }
}
