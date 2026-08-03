package dev.revere.alley.common;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.core.locale.LocaleService;
import dev.revere.alley.core.locale.internal.impl.SettingsLocaleImpl;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.core.profile.enums.ProfileState;
import dev.revere.alley.feature.cosmetic.CosmeticService;
import dev.revere.alley.feature.cosmetic.internal.repository.SuitRepository;
import dev.revere.alley.feature.cosmetic.internal.repository.impl.suit.BaseSuit;
import dev.revere.alley.feature.cosmetic.model.CosmeticType;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * @author Emmy
 * @project Alley
 * @date 29/04/2024 - 18:53
 */
@UtilityClass
public class PlayerUtil {
    /**
     * Reset a player's state to default values.
     * 将玩家状态重置为默认值。
     *
     * @param player         the player to reset.
     *                       要重置的玩家。
     * @param closeInventory whether to close the player's inventory after resetting.
     *                       重置后是否关闭玩家的背包。
     */
    public void reset(Player player, boolean closeInventory, boolean resetHealth) {
        if (resetHealth) {
            player.setMaxHealth(20.0D);
            player.setHealth(player.getMaxHealth());
        }
        player.setSaturation(5.0F);
        player.setFallDistance(0.0F);
        player.setFoodLevel(20);
        player.setFireTicks(0);
        player.setMaximumNoDamageTicks(20);

        if (canFly(player)) {
            player.setAllowFlight(true);
            player.setFlying(true);
        } else {
            player.setAllowFlight(false);
            player.setFlying(false);
        }

        player.setExp(0.0F);
        player.setLevel(0);
        player.setGameMode(GameMode.SURVIVAL);

        player.getInventory().setArmorContents(new ItemStack[4]);
        player.getInventory().setContents(new ItemStack[36]);

        // Remove potion effects using modern API
        // 使用现代API移除药水效果
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }

        // Clear invisibility/entity flags using modern API
        // 使用现代API清除隐身/实体标志
        player.setInvisible(false);

        if (inLobby(player)) {
            equipSelectedSuit(player);
        }

        player.updateInventory();

        if (closeInventory) {
            player.closeInventory();
        }
    }

    /**
     * Starts flying for the player if they have the required permission.
     * 如果玩家拥有所需权限，则允许其飞行。
     *
     * @param player the player to start flying.
     *               要开始飞行的玩家。
     */
    public boolean canFly(Player player) {
        return inLobby(player) && player.hasPermission(AlleyPlugin.getInstance().getService(LocaleService.class).getString(SettingsLocaleImpl.PERMISSION_DONATOR_LOBBY_FLIGHT_BYPASS));
    }

    /**
     * Checks if the player is in the lobby or waiting state.
     * 检查玩家是否在大厅或等待状态。
     *
     * @param player the player to check.
     *               要检查的玩家。
     * @return true if the player is in the lobby or waiting state, false otherwise.
     *         如果玩家在大厅或等待状态返回true，否则返回false。
     */
    public boolean inLobby(Player player) {
        Profile profile = AlleyPlugin.getInstance().getService(ProfileService.class).getProfile(player.getUniqueId());
        return profile != null && (profile.getState() == ProfileState.LOBBY || profile.getState() == ProfileState.WAITING);
    }

    private void equipSelectedSuit(Player player) {
        ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);
        Profile profile = profileService.getProfile(player.getUniqueId());
        if (profile == null) {
            return;
        }

        CosmeticService cosmeticService = AlleyPlugin.getInstance().getService(CosmeticService.class);
        if (cosmeticService == null) {
            return;
        }

        String selectedSuitName = profile.getProfileData().getCosmeticData().getSelectedSuit();
        if (selectedSuitName == null || selectedSuitName.equalsIgnoreCase("None")) {
            return;
        }

        SuitRepository suitRepository = cosmeticService.getRepository(CosmeticType.SUIT, SuitRepository.class);
        if (suitRepository == null) {
            return;
        }

        BaseSuit suit = suitRepository.getCosmetic(selectedSuitName);
        if (suit == null) {
            return;
        }

        suit.equip(player);
    }

    public static void decrement(Player player) {
        ItemStack itemStack = player.getInventory().getItemInMainHand();
        if (itemStack.getAmount() <= 1) player.setItemInHand(new ItemStack(Material.AIR, 1));
        else itemStack.setAmount(itemStack.getAmount() - 1);
        player.updateInventory();
    }

    /**
     * Get an offline player by their name
     * 通过名称获取离线玩家
     *
     * @param name the name of the player
     *             玩家名称
     * @return the offline player
     *         离线玩家
     */
    public OfflinePlayer getOfflinePlayerByName(String name) {
        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            if (offlinePlayer.getName() != null && offlinePlayer.getName().equalsIgnoreCase(name)) {
                return offlinePlayer;
            }
        }
        return null;
    }
}
