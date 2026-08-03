package dev.revere.alley.feature.cosmetic.internal.repository.impl.suit;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.feature.cosmetic.CosmeticService;
import dev.revere.alley.feature.cosmetic.internal.repository.SuitRepository;
import dev.revere.alley.feature.cosmetic.model.BaseCosmetic;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;

/**
 * @author Remi
 * 作者：Remi
 * @project alley-practice
 * 项目：alley-practice
 * @date 4/08/2025
 * 日期：2025年4月8日
 */
public abstract class BaseSuit extends BaseCosmetic {
    /**
     * Concrete suit classes must implement this method to provide their armor pieces.
     * 具体的套装类必须实现此方法以提供其护甲部件。
     *
     * @return A map of equipment slots to ItemStacks representing the armor pieces.
     *        一个装备槽位到ItemStack的映射，表示护甲部件。
     */
    public abstract Map<EquipmentSlot, ItemStack> getArmorPieces();

    /**
     * Concrete suit classes must implement this method to provide their passive effects.
     * 具体的套装类必须实现此方法以提供其被动效果。
     *
     * @return A map of PotionEffectTypes to their amplifier levels.
     *        一个药水效果类型到其增幅等级的映射。
     */
    public abstract Map<PotionEffectType, Integer> getPassiveEffects();

    /**
     * Equips the suit by setting the armor pieces in the player's inventory.
     * 通过设置玩家背包中的护甲部件来装备套装。
     *
     * @param player The player whose armor will be equipped.
     *              将要装备护甲的玩家。
     */
    public void equip(Player player) {
        PlayerInventory inventory = player.getInventory();
        getArmorPieces().forEach((slot, item) -> {
            switch (slot) {
                case FEET:
                    inventory.setBoots(item);
                    break;
                case LEGS:
                    inventory.setLeggings(item);
                    break;
                case CHEST:
                    inventory.setChestplate(item);
                    break;
                case HEAD:
                    inventory.setHelmet(item);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported equipment slot: " + slot);
            }
        });

        getPassiveEffects().forEach((type, amplifier) -> {
            PotionEffect effect = new PotionEffect(type, Integer.MAX_VALUE, amplifier, false, false);
            player.addPotionEffect(effect);
        });
    }

    /**
     * Removes the suit by clearing the armor pieces and potion effects from the player.
     * 通过清除玩家的护甲部件和药水效果来移除套装。
     *
     * @param player The player whose armor will be removed.
     *              将要移除护甲的玩家。
     */
    public void remove(Player player) {
        getPassiveEffects().keySet().forEach(player::removePotionEffect);

        PlayerInventory inventory = player.getInventory();
        inventory.setBoots(null);
        inventory.setLeggings(null);
        inventory.setChestplate(null);
        inventory.setHelmet(null);
    }

    /**
     * Creates a colored leather armor piece.
     * 创建一个染色的皮革护甲部件。
     *
     * @param leatherArmor The material of the leather armor piece.
     *                    皮革护甲部件的材质。
     * @param color        The color to apply to the armor.
     *                    要应用到护甲的颜色。
     * @return An ItemStack representing the colored leather armor.
     *        表示染色皮革护甲的ItemStack。
     */
    public ItemStack createColoredArmor(Material leatherArmor, Color color) {
        ItemStack item = new ItemStack(leatherArmor);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        if (meta != null) {
            meta.setColor(color);
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Called when the player selects this suit.
     * 当玩家选择此套装时调用。
     * Removes the old suit and equips the new one.
     * 移除旧套装并装备新套装。
     *
     * @param player The player who selected the suit.
     *              选择套装的玩家。
     */
    public void onSelect(Player player) {
        ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);
        Profile profile = profileService.getProfile(player.getUniqueId());
        CosmeticService cosmeticService = AlleyPlugin.getInstance().getService(CosmeticService.class);
        if (profile == null || cosmeticService == null) return;

        String oldSuitName = profile.getProfileData().getCosmeticData().getSelected(getType());
        SuitRepository repo = cosmeticService.getRepository(getType(), SuitRepository.class);

        if (repo != null && oldSuitName != null && !oldSuitName.equalsIgnoreCase("None")) {
            BaseSuit oldSuit = repo.getCosmetic(oldSuitName);
            if (oldSuit != null) {
                oldSuit.remove(player);
            }
        }
        this.equip(player);
    }
}