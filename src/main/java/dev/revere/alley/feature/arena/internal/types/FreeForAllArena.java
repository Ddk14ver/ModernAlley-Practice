package dev.revere.alley.feature.arena.internal.types;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.feature.arena.Arena;
import dev.revere.alley.feature.arena.ArenaService;
import dev.revere.alley.feature.arena.ArenaType;
import dev.revere.alley.core.config.ConfigService;
import dev.revere.alley.common.serializer.Serializer;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * @author Emmy
 * @project Alley
 * @date 20/05/2024 - 19:15
 */
public class FreeForAllArena extends Arena {
    protected final AlleyPlugin plugin = AlleyPlugin.getInstance();

    /**
     * Constructor for the FreeForAllArena class.
     * FreeForAllArena 类的构造函数。
     *
     * @param name    The name of the arena.
     *                竞技场的名称。
     * @param minimum The minimum location of the arena.
     *                竞技场的最小位置。
     * @param maximum The maximum location of the arena.
     *                竞技场的最大位置。
     */
    public FreeForAllArena(String name, Location minimum, Location maximum) {
        super(name, minimum, maximum);
    }

    @Override
    public ArenaType getType() {
        return ArenaType.FFA;
    }

    @Override
    public void createArena() {
        ArenaService arenaService = this.plugin.getService(ArenaService.class);
        arenaService.registerNewArena(this);
        this.saveArena();
    }

    @Override
    public void saveArena() {
        String name = "arenas." + this.getName();
        FileConfiguration config = this.plugin.getService(ConfigService.class).getArenasConfig();

        config.set(name, null);
        config.set(name + ".type", this.getType().name());
        config.set(name + ".safe-zone.pos1", Serializer.serializeLocation(this.getMinimum()));
        config.set(name + ".safe-zone.pos2", Serializer.serializeLocation(this.getMaximum()));
        config.set(name + ".center", Serializer.serializeLocation(this.getCenter()));
        config.set(name + ".pos1", Serializer.serializeLocation(this.getPos1()));
        config.set(name + ".enabled", this.isEnabled());
        config.set(name + ".display-name", this.getDisplayName());

        this.plugin.getService(ConfigService.class).saveConfig(this.plugin.getService(ConfigService.class).getConfigFile("storage/arenas.yml"), config);
    }

    @Override
    public void deleteArena() {
        FileConfiguration config = this.plugin.getService(ConfigService.class).getArenasConfig();
        config.set("arenas." + this.getName(), null);

        this.plugin.getService(ConfigService.class).saveConfig(this.plugin.getService(ConfigService.class).getConfigFile("storage/arenas.yml"), config);
    }
}