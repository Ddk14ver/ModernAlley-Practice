package dev.revere.alley.feature.match.snapshot;

import dev.revere.alley.common.InventoryUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Remi
 * @project Alley
 * @date 5/26/2024
 * 快照 - 存储玩家比赛结束时的状态数据（物品栏、生命值、药水等）。
 */
@Getter
@Setter
public class Snapshot {
    private final String username;

    private final UUID uuid;
    private UUID opponent;

    private double health;
    private double absorption;
    private int foodLevel;
    private float saturation;

    private final ItemStack[] armor;
    private final ItemStack[] inventory;
    private ItemStack offhand;

    private final List<String> potionEffects;

    private int thrownPotions;
    private int missedPotions;

    private int longestCombo;

    private int totalHits;
    private int criticalHits;
    private int blockedHits;
    private double averageCombatCps;
    private int highestCombatCps;

    private int wTapAttempts;
    private int wTapSuccesses;
    private double regen;

    private long createdAt;

    /**
     * Constructor for the Snapshot class.
     * Snapshot 类的构造函数。
     *
     * @param player the player to create the snapshot for
     *        要为其创建快照的玩家
     * @param alive  whether the player is alive or not
     *        玩家是否存活
     */
    public Snapshot(Player player, boolean alive) {
        this.uuid = player.getUniqueId();
        this.username = player.getName();

        this.health = alive ? player.getHealth() : 0;
        this.absorption = player.getAbsorptionAmount();
        this.foodLevel = player.getFoodLevel();
        this.saturation = player.getSaturation();

        this.armor = InventoryUtil.cloneItemStackArray(player.getInventory().getArmorContents());
        this.inventory = InventoryUtil.cloneItemStackArray(player.getInventory().getContents());
        this.offhand = player.getInventory().getItemInOffHand();
        this.potionEffects = player.getActivePotionEffects().stream()
                .map(effect -> effect.getType().getName() + " " + effect.getAmplifier())
                .collect(Collectors.toList());

        this.thrownPotions = 0;
        this.missedPotions = 0;

        this.longestCombo = 0;

        this.totalHits = 0;
        this.criticalHits = 0;
        this.blockedHits = 0;
        this.averageCombatCps = 0.0D;
        this.highestCombatCps = 0;

        this.wTapAttempts = 0;
        this.wTapSuccesses = 0;
        this.regen = 0;

        this.createdAt = System.currentTimeMillis();
    }

    /**
     * Get the accuracy of potion throws as a percentage.
     * 以百分比形式获取药水投掷的命中率。
     *
     * @return the potion accuracy percentage
     *         药水命中率百分比
     */
    public double getPotionAccuracy() {
        if (this.missedPotions == 0) {
            return 100.0;
        } else if (this.thrownPotions == this.missedPotions) {
            return 50.0;
        }

        return Math.round(100.0D - (((double) this.missedPotions / (double) this.thrownPotions) * 100.0D));
    }

    /**
     * Get the amount of potions in the player's inventory.
     * 获取玩家物品栏中的药水数量。
     *
     * @return the amount of potions in the inventory
     *         物品栏中的药水数量
     */
    public int getAmountOfPotionsInInventory() {
        int amount = 0;
        for (ItemStack item : this.inventory) {
            if (item != null && item.getType() == Material.SPLASH_POTION) {
                amount += item.getAmount();
            }
        }
        return amount;
    }

    /**
     * W-tap rate as a percentage of all W-tap attempts (successful / attempts), matching the
     * reference PotPvP calcWTap calculation. Returns 0 when there were no attempts.
     * W-tap 命中率 = 成功 W-tap 次数 / W-tap 尝试次数；无尝试时返回 0。
     */
    public int getWTapPercentage() {
        if (this.wTapAttempts <= 0) return 0;
        return Math.round(100.0F * this.wTapSuccesses / this.wTapAttempts);
    }
}
