package dev.revere.alley.feature.match.task.other;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author Emmy
 * @project Alley
 * @date 15/09/2024 - 19:24
 * 箭矢移除任务 - 定期清除地面上所有已落地的箭矢。
 */
public class ArrowRemovalTask extends BukkitRunnable {
    @Override
    public void run() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Arrow) {
                    Arrow arrow = (Arrow) entity;
                    if (arrow.isOnGround()) {
                        arrow.remove();
                    }
                }
            }
        }
    }
}