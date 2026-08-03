package dev.revere.alley.common.item;

import dev.revere.alley.common.reflect.utility.ReflectionUtility;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 26/06/2026
 */
public class ItemBuilder implements Listener {

    private final ItemStack itemStack;
    private String command;
    private boolean commandEnabled = true;

    public ItemBuilder(Material mat) {
        itemStack = new ItemStack(mat);
    }

    public ItemBuilder(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public ItemBuilder amount(int amount) {
        itemStack.setAmount(amount);
        return this;
    }

    public ItemBuilder name(String name) {
        ItemMeta meta = itemStack.getItemMeta();

        // Check if meta is null and create a new one if needed
        // 检查 meta 是否为 null，如有需要则创建一个新的
        if (meta == null) {
            meta = Bukkit.getItemFactory().getItemMeta(itemStack.getType());
        }

        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        itemStack.setItemMeta(meta);

        return this;
    }

    public ItemBuilder lore(String name) {
        ItemMeta meta = itemStack.getItemMeta();
        List<String> lore = meta.getLore();

        if (lore == null) {
            lore = new ArrayList<>();
        }

        lore.add(ChatColor.translateAlternateColorCodes('&', name));
        meta.setLore(lore);

        itemStack.setItemMeta(meta);

        return this;
    }

    public ItemBuilder lore(String... lore) {
        List<String> toSet = new ArrayList<>();
        ItemMeta meta = itemStack.getItemMeta();

        for (String string : lore) {
            toSet.add(ChatColor.translateAlternateColorCodes('&', string));
        }

        meta.setLore(toSet);
        itemStack.setItemMeta(meta);

        return this;
    }

    public ItemBuilder lore(List<String> lore) {
        List<String> toSet = new ArrayList<>();
        ItemMeta meta = itemStack.getItemMeta();

        for (String string : lore) {
            toSet.add(ChatColor.translateAlternateColorCodes('&', string));
        }

        meta.setLore(toSet);
        itemStack.setItemMeta(meta);

        return this;
    }

    public ItemBuilder durability(int durability) {
        // In 1.21, durability for non-damageable items (wool, glass, etc.) is handled by the Material itself.
        // 在 1.21 中，不可损坏物品（羊毛、玻璃等）的耐久度由 Material 本身处理。
        // For damageable items, use ItemMeta.setDamage().
        // 对于可损坏的物品，使用 ItemMeta.setDamage()。
        if (itemStack.getItemMeta() instanceof org.bukkit.inventory.meta.Damageable damageable) {
            damageable.setDamage(durability);
            itemStack.setItemMeta(damageable);
        }
        return this;
    }

    public ItemBuilder enchantment(Enchantment enchantment, int level) {
        itemStack.addUnsafeEnchantment(enchantment, level);
        return this;
    }

    public ItemBuilder enchantment(Enchantment enchantment) {
        itemStack.addUnsafeEnchantment(enchantment, 1);
        return this;
    }

    public ItemBuilder type(Material material) {
        itemStack.setType(material);
        return this;
    }

    public ItemBuilder clearLore() {
        ItemMeta meta = itemStack.getItemMeta();

        meta.setLore(new ArrayList<>());
        itemStack.setItemMeta(meta);

        return this;
    }

    public ItemBuilder glow(boolean glow) {
        ItemMeta meta = itemStack.getItemMeta();

        if (glow) {
            meta.addEnchant(Enchantment.LURE, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        } else {
            meta.removeEnchant(Enchantment.LURE);
            meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        itemStack.setItemMeta(meta);

        return this;
    }

    public ItemBuilder clearEnchantments() {
        for (Enchantment e : itemStack.getEnchantments().keySet()) {
            itemStack.removeEnchantment(e);
        }

        return this;
    }

    public ItemBuilder hideMeta() {
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.addItemFlags(ItemFlag.values());

        itemStack.setItemMeta(itemMeta);

        return this;
    }

    public ItemBuilder command(String command) {
        this.command = command;
        return this;
    }

    public ItemBuilder commandEnabled(boolean enabled) {
        this.commandEnabled = enabled;
        return this;
    }

    /**
     * Sets the skull's texture based on a Base64 string.
     * 根据 Base64 字符串设置头颅的纹理。
     * This method uses reflect to apply custom textures.
     * 此方法使用反射来应用自定义纹理。
     *
     * @param base64Texture The Base64 encoded texture string.
     *                      Base64 编码的纹理字符串。
     * @return The ItemBuilder instance.
     *         ItemBuilder 实例。
     * @throws IllegalArgumentException if the ItemStack is not a player skull.
     *                                  如果 ItemStack 不是玩家头颅。
     */
    public ItemBuilder setSkullTexture(String base64Texture) {
        if (itemStack.getType() != Material.PLAYER_HEAD) {
            throw new IllegalArgumentException("ItemStack must be a player skull to set a custom texture.");
        }

        SkullMeta meta = ReflectionUtility.createSkullMeta(this.itemStack, base64Texture);

        this.itemStack.setItemMeta(meta);
        return this;
    }

    public ItemBuilder setSkull(String owner) {
        if (itemStack.getType() != Material.PLAYER_HEAD) {
            throw new IllegalArgumentException("ItemStack must be a player head to set an owner.");
        }

        SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(owner));
        itemStack.setItemMeta(meta);

        return this;
    }

    public ItemStack build() {
        ItemMeta meta = itemStack.getItemMeta();

        if (commandEnabled && command != null) {
            List<String> lore = meta.getLore();

            if (lore == null) {
                lore = new ArrayList<>();
            }

            lore.add("command:" + command);
            meta.setLore(lore);
        }

        itemStack.setItemMeta(meta);
        return itemStack;
    }
}