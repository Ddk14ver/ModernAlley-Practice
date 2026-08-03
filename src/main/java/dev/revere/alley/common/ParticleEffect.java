package dev.revere.alley.common;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.List;

/**
 * ParticleEffect Library — rewritten for Minecraft 1.21.11 using the modern Bukkit Particle API.
 * ParticleEffect库 — 使用现代Bukkit Particle API为Minecraft 1.21.11重写。
 * <p>
 * Original library by @DarkBlade12; modernized for post-1.13 compatibility.
 * 原始库由@DarkBlade12编写；为1.13+版本兼容性进行了现代化改造。
 *
 * @author DarkBlade12 (original)
 * @version 2.0
 */
@Getter
public enum ParticleEffect {
    EXPLOSION_NORMAL(Particle.EXPLOSION),
    EXPLOSION_LARGE(Particle.EXPLOSION),
    EXPLOSION_HUGE(Particle.EXPLOSION),
    FIREWORKS_SPARK(Particle.FIREWORK),
    WATER_BUBBLE(Particle.BUBBLE),
    WATER_SPLASH(Particle.SPLASH),
    WATER_WAKE(Particle.FISHING),
    SUSPENDED(Particle.UNDERWATER),
    SUSPENDED_DEPTH(Particle.UNDERWATER),
    CRIT(Particle.CRIT),
    CRIT_MAGIC(Particle.ENCHANTED_HIT),
    SMOKE_NORMAL(Particle.SMOKE),
    SMOKE_LARGE(Particle.LARGE_SMOKE),
    SPELL(Particle.EFFECT),
    SPELL_INSTANT(Particle.INSTANT_EFFECT),
    SPELL_MOB(Particle.ENTITY_EFFECT),
    SPELL_MOB_AMBIENT(Particle.EFFECT),
    SPELL_WITCH(Particle.WITCH),
    DRIP_WATER(Particle.DRIPPING_WATER),
    DRIP_LAVA(Particle.DRIPPING_LAVA),
    VILLAGER_ANGRY(Particle.ANGRY_VILLAGER),
    VILLAGER_HAPPY(Particle.HAPPY_VILLAGER),
    TOWN_AURA(Particle.MYCELIUM),
    NOTE(Particle.NOTE),
    PORTAL(Particle.PORTAL),
    ENCHANTMENT_TABLE(Particle.ENCHANT),
    FLAME(Particle.FLAME),
    LAVA(Particle.LAVA),
    FOOTSTEP(Particle.POOF),
    CLOUD(Particle.CLOUD),
    REDSTONE(Particle.DUST),
    SNOWBALL(Particle.ITEM_SNOWBALL),
    SNOW_SHOVEL(Particle.POOF),
    SLIME(Particle.ITEM_SLIME),
    HEART(Particle.HEART),
    BARRIER(Particle.BLOCK),
    ITEM_CRACK(Particle.ITEM),
    BLOCK_CRACK(Particle.BLOCK),
    BLOCK_DUST(Particle.FALLING_DUST),
    WATER_DROP(Particle.DRIPPING_WATER),
    ITEM_TAKE(Particle.ITEM),
    MOB_APPEARANCE(Particle.ELDER_GUARDIAN);

    private final Particle particle;

    ParticleEffect(Particle particle) {
        this.particle = particle;
    }

    /**
     * Displays a particle effect visible to all players within range.
     * 显示一个对范围内所有玩家可见的粒子效果。
     */
    public void display(float offsetX, float offsetY, float offsetZ, float speed, int amount, Location center, double range) {
        World world = center.getWorld();
        if (world == null) return;

        if (amount == 0) {
            world.spawnParticle(this.particle, center, 0, offsetX, offsetY, offsetZ, speed, null);
        } else {
            world.spawnParticle(this.particle, center, amount, offsetX, offsetY, offsetZ, speed, null);
        }
    }

    /**
     * Displays a particle effect visible to specified players.
     * 显示一个对指定玩家可见的粒子效果。
     */
    public void display(float offsetX, float offsetY, float offsetZ, float speed, int amount, Location center, List<Player> players) {
        for (Player player : players) {
            if (amount == 0) {
                player.spawnParticle(this.particle, center, 0, offsetX, offsetY, offsetZ, speed, null);
            } else {
                player.spawnParticle(this.particle, center, amount, offsetX, offsetY, offsetZ, speed, null);
            }
        }
    }

    /**
     * Displays a particle effect visible to specified players (varargs).
     * 显示一个对指定玩家可见的粒子效果（可变参数）。
     */
    public void display(float offsetX, float offsetY, float offsetZ, float speed, int amount, Location center, Player... players) {
        display(offsetX, offsetY, offsetZ, speed, amount, center, Arrays.asList(players));
    }

    /**
     * Displays a single directional particle visible to all players within range.
     * 显示一个对范围内所有玩家可见的单个定向粒子。
     */
    public void display(Vector direction, float speed, Location center, double range) {
        World world = center.getWorld();
        if (world == null) return;
        world.spawnParticle(this.particle, center, 0, (float) direction.getX(), (float) direction.getY(), (float) direction.getZ(), speed, null);
    }

    /**
     * Displays a single directional particle visible to specified players.
     * 显示一个对指定玩家可见的单个定向粒子。
     */
    public void display(Vector direction, float speed, Location center, List<Player> players) {
        for (Player player : players) {
            player.spawnParticle(this.particle, center, 0, (float) direction.getX(), (float) direction.getY(), (float) direction.getZ(), speed, null);
        }
    }

    /**
     * Displays a single directional particle (varargs).
     * 显示一个单个定向粒子（可变参数）。
     */
    public void display(Vector direction, float speed, Location center, Player... players) {
        display(direction, speed, center, Arrays.asList(players));
    }

    /**
     * Displays a colored particle visible to all players within range.
     * 显示一个对范围内所有玩家可见的彩色粒子。
     */
    public void display(ParticleColor color, Location center, double range) {
        World world = center.getWorld();
        if (world == null) return;

        if (this.particle == Particle.DUST) {
            Particle.DustOptions options = new Particle.DustOptions(
                org.bukkit.Color.fromRGB(
                    (int) (color.getValueX() * 255),
                    (int) (color.getValueY() * 255),
                    (int) (color.getValueZ() * 255)
                ), 1.0f);
            world.spawnParticle(this.particle, center, 1, 0, 0, 0, 0, options);
        } else {
            world.spawnParticle(this.particle, center, 0, color.getValueX(), color.getValueY(), color.getValueZ(), 1, null);
        }
    }

    /**
     * Displays a colored particle visible to specified players.
     * 显示一个对指定玩家可见的彩色粒子。
     */
    public void display(ParticleColor color, Location center, List<Player> players) {
        for (Player player : players) {
            if (this.particle == Particle.DUST) {
                Particle.DustOptions options = new Particle.DustOptions(
                    org.bukkit.Color.fromRGB(
                        (int) (color.getValueX() * 255),
                        (int) (color.getValueY() * 255),
                        (int) (color.getValueZ() * 255)
                    ), 1.0f);
                player.spawnParticle(this.particle, center, 1, 0, 0, 0, 0, options);
            } else {
                player.spawnParticle(this.particle, center, 0, color.getValueX(), color.getValueY(), color.getValueZ(), 1, null);
            }
        }
    }

    /**
     * Displays a colored particle (varargs).
     * 显示一个彩色粒子（可变参数）。
     */
    public void display(ParticleColor color, Location center, Player... players) {
        display(color, center, Arrays.asList(players));
    }

    /**
     * Displays a particle effect requiring additional data (like item/block).
     * 显示一个需要额外数据（如物品/方块）的粒子效果。
     */
    public void display(ParticleData data, float offsetX, float offsetY, float offsetZ, float speed, int amount, Location center, double range) {
        World world = center.getWorld();
        if (world == null) return;

        Object bukkitData = data.toBukkitData(this.particle);
        world.spawnParticle(this.particle, center, amount, offsetX, offsetY, offsetZ, speed, bukkitData);
    }

    /**
     * Displays a particle effect with data visible to specified players.
     * 显示一个对指定玩家可见的带数据的粒子效果。
     */
    public void display(ParticleData data, float offsetX, float offsetY, float offsetZ, float speed, int amount, Location center, List<Player> players) {
        Object bukkitData = data.toBukkitData(this.particle);
        for (Player player : players) {
            player.spawnParticle(this.particle, center, amount, offsetX, offsetY, offsetZ, speed, bukkitData);
        }
    }

    /**
     * Displays a particle effect with data (varargs).
     * 显示一个带数据的粒子效果（可变参数）。
     */
    public void display(ParticleData data, float offsetX, float offsetY, float offsetZ, float speed, int amount, Location center, Player... players) {
        display(data, offsetX, offsetY, offsetZ, speed, amount, center, Arrays.asList(players));
    }

    /**
     * Displays a directional particle with data.
     * 显示一个带数据的定向粒子。
     */
    public void display(ParticleData data, Vector direction, float speed, Location center, double range) {
        World world = center.getWorld();
        if (world == null) return;

        Object bukkitData = data.toBukkitData(this.particle);
        world.spawnParticle(this.particle, center, 0, (float) direction.getX(), (float) direction.getY(), (float) direction.getZ(), speed, bukkitData);
    }

    /**
     * Displays a directional particle with data for specific players.
     * 为指定玩家显示一个带数据的定向粒子。
     */
    public void display(ParticleData data, Vector direction, float speed, Location center, List<Player> players) {
        Object bukkitData = data.toBukkitData(this.particle);
        for (Player player : players) {
            player.spawnParticle(this.particle, center, 0, (float) direction.getX(), (float) direction.getY(), (float) direction.getZ(), speed, bukkitData);
        }
    }

    /**
     * Displays a directional particle with data (varargs).
     * 显示一个带数据的定向粒子（可变参数）。
     */
    public void display(ParticleData data, Vector direction, float speed, Location center, Player... players) {
        display(data, direction, speed, center, Arrays.asList(players));
    }

    // --- Inner types maintained for backward compatibility ---
    // --- 为向后兼容而保留的内部类型 ---

    /**
     * Represents particle color data.
     * 表示粒子颜色数据。
     */
    public abstract static class ParticleColor {
        public abstract float getValueX();
        public abstract float getValueY();
        public abstract float getValueZ();
    }

    /**
     * Ordinary RGB color for particles.
     * 粒子的普通RGB颜色。
     */
    public static final class OrdinaryColor extends ParticleColor {
        private final int red;
        private final int green;
        private final int blue;

        public OrdinaryColor(int red, int green, int blue) {
            this.red = Math.max(0, Math.min(255, red));
            this.green = Math.max(0, Math.min(255, green));
            this.blue = Math.max(0, Math.min(255, blue));
        }

        public OrdinaryColor(org.bukkit.Color color) {
            this(color.getRed(), color.getGreen(), color.getBlue());
        }

        public int getRed() { return red; }
        public int getGreen() { return green; }
        public int getBlue() { return blue; }

        @Override
        public float getValueX() { return red / 255F; }
        @Override
        public float getValueY() { return green / 255F; }
        @Override
        public float getValueZ() { return blue / 255F; }
    }

    /**
     * Note color for note particles (0-24).
     * 音符粒子的音符颜色（0-24）。
     */
    public static final class NoteColor extends ParticleColor {
        private final int note;

        public NoteColor(int note) {
            this.note = Math.max(0, Math.min(24, note));
        }

        @Override
        public float getValueX() { return note / 24F; }
        @Override
        public float getValueY() { return 0; }
        @Override
        public float getValueZ() { return 0; }
    }

    /**
     * Abstract particle data for item/block particles.
     * 物品/方块粒子的抽象粒子数据。
     */
    public abstract static class ParticleData {
        private final Material material;
        private final byte data;

        @SuppressWarnings("deprecation")
        public ParticleData(Material material, byte data) {
            this.material = material;
            this.data = data;
        }

        public Material getMaterial() { return material; }
        public byte getData() { return data; }

        /**
         * Converts this legacy particle data to modern Bukkit particle data object.
         * 将此旧版粒子数据转换为现代Bukkit粒子数据对象。
         */
        public Object toBukkitData(Particle particle) {
            if (particle == Particle.ITEM) {
                return new ItemStack(material);
            } else if (particle == Particle.BLOCK || particle == Particle.FALLING_DUST) {
                return material.createBlockData();
            }
            return null;
        }
    }

    /**
     * Item data for ITEM_CRACK effect.
     * ITEM_CRACK效果的物品数据。
     */
    public static final class ItemData extends ParticleData {
        public ItemData(Material material, byte data) {
            super(material, data);
        }
    }

    /**
     * Block data for BLOCK_CRACK / BLOCK_DUST effects.
     * BLOCK_CRACK / BLOCK_DUST效果的方块数据。
     */
    public static final class BlockData extends ParticleData {
        public BlockData(Material material, byte data) {
            super(material, data);
            if (!material.isBlock()) {
                throw new IllegalArgumentException("The material is not a block");
            }
        }
    }
}