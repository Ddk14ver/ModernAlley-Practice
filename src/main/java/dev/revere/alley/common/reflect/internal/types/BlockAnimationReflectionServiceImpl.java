package dev.revere.alley.common.reflect.internal.types;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.reflect.Reflection;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

/**
 * @author Emmy
 * @project Alley
 * @since 25/04/2025
 */
public class BlockAnimationReflectionServiceImpl implements Reflection {
    /**
     * Animates a block being broken over time (0 → 9).
     * 随时间动画显示方块被破坏的过程（0 → 9）。
     *
     * @param block       The block to animate.
     *                    要动画显示方块。
     * @param animationId A unique ID for this animation.
     *                    此动画的唯一 ID。
     * @param duration    Duration in ticks (e.g. 40 = 2 seconds).
     *                    持续时间（以刻为单位，例如 40 = 2 秒）。
     * @param players     List of players to send the animation to.
     *                    要发送动画的玩家列表。
     */
    public void sendBreakAnimationSequence(List<Player> players, Block block, int animationId, int duration) {
        int maxStage = 9;
        int interval = duration / (maxStage + 1);

        new BukkitRunnable() {
            int stage = 0;

            @Override
            public void run() {
                if (this.stage > maxStage || block.getType().equals(Material.AIR)) {
                    this.cancel();
                    return;
                }

                players.forEach(
                        player -> sendBreakAnimation(player, block, animationId, this.stage)
                );

                this.stage++;
            }
        }.runTaskTimer(AlleyPlugin.getInstance(), 0L, interval);
    }

    /**
     * Sends a block break animation to a specific player using modern Bukkit API.
     * 使用现代 Bukkit API 向特定玩家发送方块破坏动画。
     *
     * @param player      The player to send the animation to.
     *                    要发送动画的玩家。
     * @param block       The block to animate.
     *                    要动画显示的方块。
     * @param animationId Unique animation ID (used as entity ID for break effect).
     *                    唯一的动画 ID（用作破坏效果的实体 ID）。
     * @param stage       Break stage (0-9).
     *                    破坏阶段（0-9）。
     */
    public void sendBreakAnimation(Player player, Block block, int animationId, int stage) {
        player.sendBlockDamage(block.getLocation(), (float) stage / 9.0f, animationId);
    }
}