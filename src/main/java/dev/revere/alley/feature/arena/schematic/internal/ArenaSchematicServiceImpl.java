package dev.revere.alley.feature.arena.schematic.internal;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.mask.InverseSingleBlockTypeMask;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockTypes;
import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.feature.arena.Arena;
import dev.revere.alley.feature.arena.internal.types.StandAloneArena;
import dev.revere.alley.bootstrap.AlleyContext;
import dev.revere.alley.bootstrap.annotation.Service;
import dev.revere.alley.common.logger.Logger;
import dev.revere.alley.feature.arena.schematic.ArenaSchematicService;
import org.bukkit.Location;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Remi
 * @project alley-practice
 * @since 02/07/2025
 */
@Service(provides = ArenaSchematicService.class, priority = 120)
public class ArenaSchematicServiceImpl implements ArenaSchematicService {
    private final AlleyPlugin plugin;
    private File schematicsDirectory;

    /** Arena name → cached clipboard. Avoids disk I/O on every match start. */
    private final Map<String, Clipboard> clipboardCache = new ConcurrentHashMap<>();

    /** Serialize large background edits while keeping them off the server thread. */
    private final ExecutorService editExecutor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "alley-arena-fawe");
        thread.setDaemon(true);
        return thread;
    });

    /** Small high-priority pool so a new spawn region is not queued behind a full map paste. */
    private final ExecutorService priorityExecutor = Executors.newFixedThreadPool(2, runnable -> {
        Thread thread = new Thread(runnable, "alley-arena-fawe-priority");
        thread.setDaemon(true);
        return thread;
    });

    /**
     * Constructor for the ArenaSchematicServiceImpl class.
     * ArenaSchematicServiceImpl 类的构造函数。
     *
     * @param plugin The main Alley bootstrap instance.
     *               Alley 主启动实例。
     */
    public ArenaSchematicServiceImpl(AlleyPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void setup(AlleyContext context) {
        this.schematicsDirectory = this.getSchematicsDirectory();
        this.createSchematicsFolder();
    }

    @Override
    public void shutdown(AlleyContext context) {
        this.editExecutor.shutdown();
        this.priorityExecutor.shutdown();
        this.clipboardCache.clear();
    }

    private void createSchematicsFolder() {
        File schematicsDir = this.getSchematicsDirectory();
        if (!schematicsDir.exists()) {
            if (schematicsDir.mkdirs()) {
                Logger.info("Created schematics directory: " + schematicsDir.getPath());
            } else {
                Logger.error("Failed to create schematics directory: " + schematicsDir.getPath());
            }
        }
    }

    @Override
    public void generateMissingSchematics(List<Arena> arenas) {
        for (Arena arena : arenas) {
            File schematicFile = getSchematicFile(arena);
            if (!schematicFile.exists()) {
                Logger.info("Schematic for " + arena.getName() + " not found, creating...");
                save(arena, schematicFile);
            }
        }
    }

    @Override
    public void save(Arena arena, File schematicFile) {
        try {
            Location min = arena.getMinimum();
            Location max = arena.getMaximum();

            World weWorld = BukkitAdapter.adapt(min.getWorld());

            BlockVector3 minVector = BukkitAdapter.asBlockVector(min);
            BlockVector3 maxVector = BukkitAdapter.asBlockVector(max);

            CuboidRegion region = new CuboidRegion(minVector, maxVector);
            BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
            clipboard.setOrigin(minVector);

            try (EditSession session = WorldEdit.getInstance().newEditSessionBuilder()
                    .world(weWorld)
                    .build()) {
                session.setFastMode(true);

                ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(
                        session, region, clipboard, region.getMinimumPoint());
                Operations.complete(forwardExtentCopy);

                try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_SCHEMATIC
                        .getWriter(Files.newOutputStream(schematicFile.toPath()))) {
                    writer.write(clipboard);
                }
            }

            this.clipboardCache.remove(schematicFile.getName().toLowerCase(java.util.Locale.ROOT));
            Logger.info("Saved schematic for arena: " + arena.getName());
        } catch (Exception exception) {
            Logger.logException("Failed to save schematic for arena " + arena.getName(), exception);
        }
    }

    @Override
    public void updateSchematic(Arena arena) {
        File schematicFile = getSchematicFile(arena.getName());
        this.save(arena, schematicFile);
    }

    /**
     * Pastes the schematic at the specified location.
     * 在指定位置粘贴原理图。
     *
     * @param location      The location to paste the schematic.
     *                      粘贴原理图的位置。
     * @param schematicFile The file containing the schematic to paste.
     *                      包含要粘贴的原理图的文件。
     */
    public void paste(Location location, File schematicFile) {
        if (!schematicFile.exists()) {
            Logger.error("Cannot paste schematic, file does not exist: " + schematicFile.getPath());
            return;
        }

        try {
            BlockVector3 toVector = BukkitAdapter.asBlockVector(location);

            // Cache the clipboard — disk I/O only once per arena
            String cacheKey = schematicFile.getName();
            Clipboard clipboard = clipboardCache.computeIfAbsent(cacheKey, k -> {
                try {
                    return ClipboardFormats.findByFile(schematicFile).load(schematicFile);
                } catch (java.io.IOException e) {
                    throw new RuntimeException(e);
                }
            });

            // FAWE async paste — block placement runs off the main thread
            com.sk89q.worldedit.world.World weWorld =
                    BukkitAdapter.adapt(location.getWorld());
            try (EditSession session = WorldEdit.getInstance().newEditSessionBuilder()
                    .world(weWorld)
                    .fastMode(true)
                    .limitUnlimited()
                    .build()) {
                ForwardExtentCopy pasteCopy = new ForwardExtentCopy(
                        clipboard, clipboard.getRegion(), session, toVector);
                pasteCopy.setSourceMask(new InverseSingleBlockTypeMask(clipboard, BlockTypes.AIR));
                Operations.complete(pasteCopy);
                session.flushQueue();
            }
        } catch (Exception exception) {
            Logger.logException("Failed to paste schematic at " + location, exception);
        }
    }

    @Override
    public CompletableFuture<Void> pasteAsync(Location location, File schematicFile) {
        return CompletableFuture.runAsync(
                () -> pasteBlocking(location, schematicFile, null, null), this.editExecutor);
    }

    @Override
    public CompletableFuture<Void> pasteRegionAsync(Location location, File schematicFile,
                                                    Location targetMinimum, Location targetMaximum) {
        return CompletableFuture.runAsync(
                () -> pasteBlocking(location, schematicFile, targetMinimum, targetMaximum), this.priorityExecutor);
    }

    private Clipboard loadClipboard(File schematicFile) throws java.io.IOException {
        String cacheKey = schematicFile.getName().toLowerCase(java.util.Locale.ROOT);
        Clipboard cached = this.clipboardCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        Clipboard loaded = ClipboardFormats.findByFile(schematicFile).load(schematicFile);
        Clipboard previous = this.clipboardCache.putIfAbsent(cacheKey, loaded);
        return previous == null ? loaded : previous;
    }

    private void pasteBlocking(Location location, File schematicFile,
                               Location targetMinimum, Location targetMaximum) {
        if (!schematicFile.exists()) {
            throw new IllegalArgumentException("Schematic does not exist: " + schematicFile.getPath());
        }

        try {
            Clipboard clipboard = loadClipboard(schematicFile);
            BlockVector3 clipboardMinimum = clipboard.getRegion().getMinimumPoint();
            BlockVector3 clipboardMaximum = clipboard.getRegion().getMaximumPoint();
            BlockVector3 destinationOrigin = BukkitAdapter.asBlockVector(location);

            BlockVector3 sourceMinimum = clipboardMinimum;
            BlockVector3 sourceMaximum = clipboardMaximum;
            if (targetMinimum != null && targetMaximum != null) {
                BlockVector3 targetMin = BukkitAdapter.asBlockVector(targetMinimum);
                BlockVector3 targetMax = BukkitAdapter.asBlockVector(targetMaximum);
                int offsetX = destinationOrigin.x() - clipboardMinimum.x();
                int offsetY = destinationOrigin.y() - clipboardMinimum.y();
                int offsetZ = destinationOrigin.z() - clipboardMinimum.z();
                sourceMinimum = BlockVector3.at(
                        Math.max(clipboardMinimum.x(), targetMin.x() - offsetX),
                        Math.max(clipboardMinimum.y(), targetMin.y() - offsetY),
                        Math.max(clipboardMinimum.z(), targetMin.z() - offsetZ));
                sourceMaximum = BlockVector3.at(
                        Math.min(clipboardMaximum.x(), targetMax.x() - offsetX),
                        Math.min(clipboardMaximum.y(), targetMax.y() - offsetY),
                        Math.min(clipboardMaximum.z(), targetMax.z() - offsetZ));
                if (sourceMinimum.x() > sourceMaximum.x()
                        || sourceMinimum.y() > sourceMaximum.y()
                        || sourceMinimum.z() > sourceMaximum.z()) {
                    return;
                }
                destinationOrigin = BlockVector3.at(
                        destinationOrigin.x() + sourceMinimum.x() - clipboardMinimum.x(),
                        destinationOrigin.y() + sourceMinimum.y() - clipboardMinimum.y(),
                        destinationOrigin.z() + sourceMinimum.z() - clipboardMinimum.z());
            }

            com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(location.getWorld());
            try (EditSession session = WorldEdit.getInstance().newEditSessionBuilder()
                    .world(weWorld)
                    .fastMode(true)
                    .limitUnlimited()
                    .build()) {
                ForwardExtentCopy pasteCopy = new ForwardExtentCopy(
                        clipboard,
                        new CuboidRegion(sourceMinimum, sourceMaximum),
                        session,
                        destinationOrigin);
                pasteCopy.setSourceMask(new InverseSingleBlockTypeMask(clipboard, BlockTypes.AIR));
                Operations.complete(pasteCopy);
                session.flushQueue();
            }
        } catch (Exception exception) {
            throw new CompletionException(exception);
        }
    }

    @Override
    public void delete(StandAloneArena arena) {
        deleteAsync(arena).exceptionally(throwable -> {
            Logger.logException("Failed to delete arena " + arena.getName(),
                    throwable instanceof Exception exception ? exception : new Exception(throwable));
            return null;
        });
    }

    @Override
    public CompletableFuture<Void> deleteAsync(StandAloneArena arena) {
        if (arena == null || !arena.isTemporaryCopy()) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.runAsync(() -> deleteBlocking(arena), this.editExecutor);
    }

    private void deleteBlocking(StandAloneArena arena) {
        if (!arena.isTemporaryCopy()) {
            return;
        }

        try {
            Location min = arena.getMinimum();
            Location max = arena.getMaximum();

            if (min == null || max == null || min.getWorld() == null) {
                Logger.error("Cannot delete arena '" + arena.getName() + "': Invalid bounds.");
                return;
            }

            World weWorld = BukkitAdapter.adapt(min.getWorld());

            BlockVector3 minVector = BukkitAdapter.asBlockVector(min);
            BlockVector3 maxVector = BukkitAdapter.asBlockVector(max);

            // FAWE async delete — all blocks → air, off the main thread
            try (EditSession session = WorldEdit.getInstance().newEditSessionBuilder()
                    .world(weWorld)
                    .fastMode(true)
                    .limitUnlimited()
                    .build()) {
                session.setBlocks((Region) new CuboidRegion(minVector, maxVector),
                        BlockTypes.AIR.getDefaultState());
                session.flushQueue();
            }
        } catch (Exception exception) {
            Logger.logException("Failed to delete arena " + arena.getName(), exception);
        }
    }

    @Override
    public File getSchematicFile(String name) {
        return new File(getSchematicsDirectory() + File.separator + name.toLowerCase().replace(" ", "_") + ".schematic");
    }

    @Override
    public File getSchematicFile(Arena arena) {
        return this.getSchematicFile(arena.getName());
    }

    private File getSchematicsDirectory() {
        return new File(this.plugin.getDataFolder(), "schematics");
    }
}
