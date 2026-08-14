package dev.revere.alley.feature.arena.schematic;

import dev.revere.alley.feature.arena.Arena;
import dev.revere.alley.feature.arena.internal.types.StandAloneArena;
import dev.revere.alley.bootstrap.lifecycle.Service;
import org.bukkit.Location;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author Remi
 * @project alley-practice
 * @date 2/07/2025
 */
public interface ArenaSchematicService extends Service {
    /**
     * Checks for missing schematic files for a given list of arenas and creates them.
     * This is intended to be called once on startup.
     * 检查给定竞技场列表中缺失的原理图文件并创建它们。
     * 此方法旨在启动时调用一次。
     * @param arenas The list of all loaded arenas to check.
     *               所有已加载竞技场的列表，用于检查。
     */
    void generateMissingSchematics(List<Arena> arenas);

    /**
     * Saves the schematic of a given arena to a file.
     * 将给定竞技场的原理图保存到文件中。
     *
     * @param arena         The arena to save.
     *                      要保存的竞技场。
     * @param schematicFile The file to save the schematic to.
     *                      用于保存原理图的文件。
     */
    void save(Arena arena, File schematicFile);

    /**
     * Updates the schematic file for a given arena by re-saving it.
     * 通过重新保存来更新给定竞技场的原理图文件。
     *
     * @param arena The arena whose schematic needs updating.
     *              需要更新原理图的竞技场。
     */
    void updateSchematic(Arena arena);

    /**
     * Pastes a schematic into the world at a specific location.
     * 将原理图粘贴到世界中的指定位置。
     *
     * @param location      The location to paste the schematic.
     *                      粘贴原理图的位置。
     * @param schematicFile The file containing the schematic data.
     *                      包含原理图数据的文件。
     */
    void paste(Location location, File schematicFile);

    /**
     * Asynchronously pastes a schematic without blocking the server thread.
     *
     * @param location destination corresponding to the clipboard minimum
     * @param schematicFile schematic file
     * @return a future completed when FAWE has finished queuing/flushing the edit
     */
    CompletableFuture<Void> pasteAsync(Location location, File schematicFile);

    /**
     * Asynchronously pastes only a clipped source region. This is used to make
     * the player spawn area available before the rest of a large arena is copied.
     */
    CompletableFuture<Void> pasteRegionAsync(Location location, File schematicFile,
                                             Location targetMinimum, Location targetMaximum);

    /**
     * Deletes the physical blocks of a temporary arena from the world.
     * 从世界中删除临时竞技场的物理方块。
     *
     * @param arena The temporary StandAloneArena to delete.
     *              要删除的临时 StandAloneArena。
     */
    void delete(StandAloneArena arena);

    /**
     * Deletes a temporary arena on the bounded FAWE executor.
     */
    CompletableFuture<Void> deleteAsync(StandAloneArena arena);

    /**
     * Gets the schematic file for an arena by its name.
     * 通过名称获取竞技场的原理图文件。
     *
     * @param name The name of the schematic (typically the arena name).
     *             原理图的名称（通常是竞技场名称）。
     * @return The File object pointing to the schematic.
     *         指向原理图的 File 对象。
     */
    File getSchematicFile(String name);

    /**
     * Gets the schematic file for a given arena instance.
     * 获取给定竞技场实例的原理图文件。
     *
     * @param arena The arena.
     *              竞技场实例。
     * @return The File object pointing to the schematic.
     *         指向原理图的 File 对象。
     */
    File getSchematicFile(Arena arena);
}
