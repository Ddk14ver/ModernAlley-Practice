package dev.revere.alley.feature.leaderboard.hologram;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.text.CC;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Alley
 * @project Alley
 * @since 02/07/2025
 *
 * Base class for all holograms. Manages a list of HologramLine objects.
 * 所有全息图的基类。管理一组HologramLine对象。
 */
@Getter
@Setter
public abstract class Hologram {
    protected final String name;
    protected Location baseLocation;
    protected boolean enabled;
    protected int showStat = 10;
    protected final List<HologramLine> lines = new ArrayList<>();
    protected final AtomicBoolean isUpdating = new AtomicBoolean(false);

    /** Spacing between lines in blocks (0.25 = tight, 0.3 = normal). */
    protected static final double LINE_SPACING = 0.28;

    /** Height offset from base to first visible line. */
    protected static final double BASE_HEIGHT_OFFSET = 2.2;

    /**
     * Creates a hologram at a specific location.
     */
    public Hologram(String name, Location baseLocation) {
        this.name = name;
        // Store base 2 blocks below visible hologram
        this.baseLocation = baseLocation.clone().subtract(0, 2, 0);
        this.enabled = true;
    }

    /**
     * Creates a hologram from config (location loaded later).
     */
    public Hologram(String name) {
        this.name = name;
        this.enabled = false;
    }

    // ========================
    // Abstract methods
    // ========================

    /**
     * Gets the formatted text lines for this hologram.
     * Must run on main thread.
     */
    public abstract List<String> getTextLines();

    /**
     * Called periodically to refresh the hologram content.
     */
    public abstract void updateContent();

    // ========================
    // Core methods
    // ========================

    /**
     * Moves the base location to a new position.
     */
    public void moveTo(Location location) {
        this.baseLocation = location.clone().subtract(0, 2, 0);
    }

    /**
     * Spawns all hologram lines at the calculated positions.
     */
    public void spawn(List<String> textLines) {
        runOnMain(() -> {
            despawn();
            for (int i = 0; i < textLines.size(); i++) {
                Location lineLoc = calculatePosition(i, textLines.size());
                HologramLine line = new HologramLine();
                line.spawn(lineLoc, textLines.get(i));
                this.lines.add(line);
            }
        });
    }

    /**
     * Despawns all hologram lines and clears the list.
     */
    public void despawn() {
        runOnMain(() -> {
            for (HologramLine line : this.lines) {
                line.despawn();
            }
            this.lines.clear();
        });
    }

    /**
     * Smart update: only changes lines that actually differ.
     * Reduces flicker and improves performance.
     */
    public void updateSmartly(List<String> textLines) {
        if (!isUpdating.compareAndSet(false, true)) return;

        runOnMain(() -> {
            try {
                if (this.lines.isEmpty()) {
                    spawn(textLines);
                } else if (textLines.isEmpty()) {
                    despawn();
                } else {
                    // Update existing lines, spawn new ones, remove extras
                    int existingCount = this.lines.size();
                    int newCount = textLines.size();

                    for (int i = 0; i < Math.min(existingCount, newCount); i++) {
                        Location loc = calculatePosition(i, newCount);
                        this.lines.get(i).update(loc, textLines.get(i));
                    }

                    // Spawn extra lines if content grew
                    for (int i = existingCount; i < newCount; i++) {
                        Location loc = calculatePosition(i, newCount);
                        HologramLine line = new HologramLine();
                        line.spawn(loc, textLines.get(i));
                        this.lines.add(line);
                    }

                    // Remove extra lines if content shrank
                    while (this.lines.size() > newCount) {
                        HologramLine removed = this.lines.remove(this.lines.size() - 1);
                        removed.despawn();
                    }
                }
            } finally {
                isUpdating.set(false);
            }
        });
    }

    /**
     * Shows a setup-mode hologram (when not configured yet).
     */
    public void showSetup(String message) {
        runOnMain(() -> {
            despawn();
            Location loc = this.baseLocation.clone().add(0, BASE_HEIGHT_OFFSET, 0);
            HologramLine line = new HologramLine();
            line.spawn(loc, CC.translate("&e&l⚡ &6Hologram Setup &e&l⚡"));
            this.lines.add(line);

            Location infoLoc = loc.clone().subtract(0, LINE_SPACING, 0);
            HologramLine infoLine = new HologramLine();
            infoLine.spawn(infoLoc, CC.translate(message));
            this.lines.add(infoLine);
        });
    }

    // ========================
    // Positioning helpers
    // ========================

    /**
     * Calculates the Location for a hologram line at the given index.
     * Lines extend downward from the base + offset.
     */
    protected Location calculatePosition(int index, int totalLines) {
        double y = this.baseLocation.getY() + BASE_HEIGHT_OFFSET + (LINE_SPACING * 0.5);
        // Move down for each line
        y -= index * LINE_SPACING;
        return this.baseLocation.clone().add(0, y - this.baseLocation.getY(), 0);
    }

    /**
     * Ensures code runs on the main thread.
     */
    protected void runOnMain(Runnable task) {
        if (Bukkit.isPrimaryThread()) {
            task.run();
        } else {
            Bukkit.getScheduler().runTask(AlleyPlugin.getInstance(), task);
        }
    }

    /**
     * Sets hologram enabled/disabled state.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            despawn();
        }
    }
}
