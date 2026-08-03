package dev.revere.alley.common.serializer;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@UtilityClass
public class Serializer {
    /**
     * Serialize a location to a string.
     * 将位置序列化为字符串。
     *
     * @param location The location to serialize.
     *                 要序列化的位置。
     * @return The serialized location.
     *         序列化后的位置。
     */
    public String serializeLocation(Location location) {
        if (location == null) {
            return "null";
        }

        return location.getWorld().getName() + ":" + location.getX() + ":" + location.getY() + ":" + location.getZ() +
                ":" + location.getYaw() + ":" + location.getPitch();
    }

    /**
     * Deserialize a location from a string.
     * 从字符串反序列化位置。
     *
     * @param source The source to deserialize.
     *               要反序列化的源字符串。
     * @return The deserialized location.
     *         反序列化后的位置。
     */
    public Location deserializeLocation(String source) {
        if (source == null || source.equalsIgnoreCase("null")) {
            return null;
        }

        String[] split = source.split(":");
        World world = Bukkit.getServer().getWorld(split[0]);

        if (world == null) {
            return null;
        }

        return new Location(world, Double.parseDouble(split[1]), Double.parseDouble(split[2]),
                Double.parseDouble(split[3]), Float.parseFloat(split[4]), Float.parseFloat(split[5]));
    }

    /**
     * Serialize an ItemStack array to a Base64 string
     * 将ItemStack数组序列化为Base64字符串。
     *
     * @param items the ItemStack array to serialize
     *              要序列化的ItemStack数组
     * @return the serialized ItemStack array
     *         序列化后的ItemStack数组
     */
    public String serializeItemStack(ItemStack[] items) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeInt(items.length);
            for (ItemStack item : items) {
                dataOutput.writeObject(item);
            }

            dataOutput.close();
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception exception) {
            throw new RuntimeException("Unable to serialize item stacks.", exception);
        }
    }

    /**
     * Deserialize an ItemStack array from a Base64 string
     * 从Base64字符串反序列化ItemStack数组。
     *
     * @param data the Base64 string to deserialize
     *             要反序列化的Base64字符串
     * @return the deserialized ItemStack array
     *         反序列化后的ItemStack数组
     */
    public ItemStack[] deserializeItemStack(String data) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

            ItemStack[] items = new ItemStack[dataInput.readInt()];
            for (int i = 0; i < items.length; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }

            dataInput.close();
            return items;
        } catch (Exception exception) {
            throw new RuntimeException("Unable to deserialize item stacks.", exception);
        }
    }

    /**
     * Serialize a list of potion effects.
     * 序列化药水效果列表。
     *
     * @param potionEffects The potion effects.
     *                      药水效果。
     * @return The serialized potion effects.
     *         序列化后的药水效果。
     */
    public List<String> serializePotionEffects(List<PotionEffect> potionEffects) {
        return potionEffects.stream()
                .map(effect -> effect.getType().getName() + ":" + effect.getDuration() + ":" + effect.getAmplifier())
                .collect(Collectors.toList());
    }

    /**
     * Deserialize a list of potion effects.
     * 反序列化药水效果列表。
     *
     * @param serializedEffects The serialized potion effects.
     *                          序列化后的药水效果。
     * @return The potion effects.
     *         药水效果。
     */
    public List<PotionEffect> deserializePotionEffects(List<String> serializedEffects) {
        return serializedEffects.stream()
                .map(s -> {
                    String[] parts = s.split(":");
                    if (parts.length < 3) return null;
                    PotionEffectType type = PotionEffectType.getByName(parts[0]);
                    int duration = Integer.parseInt(parts[1]);
                    int amplifier = Integer.parseInt(parts[2]);
                    return type != null ? new PotionEffect(type, duration, amplifier) : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}