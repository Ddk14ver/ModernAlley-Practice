package dev.revere.alley.feature.cosmetic.internal.repository.impl.killeffect;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.feature.cosmetic.annotation.CosmeticData;
import dev.revere.alley.feature.cosmetic.model.CosmeticType;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;
/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 18/06/2026
 */
@CosmeticData(type = CosmeticType.KILL_EFFECT, name = "Neon Wave", description = "Colorful neon particle wave around the victim.", permission = "", icon = Material.LIGHT_BLUE_DYE, slot = 19, price = 800)
public class NeonWaveKillEffect extends BaseKillEffect {
    private static final Random random = new Random();
    private static final Color[] NEON = { Color.RED, Color.LIME, Color.BLUE, Color.YELLOW, Color.PURPLE, Color.AQUA, Color.FUCHSIA, Color.ORANGE };

    @Override public void execute(Player victim) {
        Location center = victim.getLocation();
        new BukkitRunnable() {
            int ticks = 0;
            @Override public void run() {
                if (ticks >= 60) { cancel(); return; }
                if (ticks % 3 == 0) {
                    Color c = NEON[ticks / 3 % NEON.length];
                    for (int i = 0; i < 30; i++) {
                        double phi = Math.acos(1 - 2 * random.nextDouble());
                        double theta = 2 * Math.PI * random.nextDouble();
                        double x = Math.sin(phi) * Math.cos(theta) * 1.5;
                        double y = Math.cos(phi) * 1.5;
                        double z = Math.sin(phi) * Math.sin(theta) * 1.5;
                        victim.getWorld().spawnParticle(Particle.DUST, center.clone().add(x, y + 2, z), 1, 0, 0, 0, 0, new Particle.DustOptions(c, 1.0f));
                    }
                    for (int b = 0; b < 6; b++) {
                        double angle = (2 * Math.PI * b) / 6 + (ticks * 0.1);
                        for (double r = 1; r <= 4; r += 0.5) {
                            Color bc = NEON[(b + ticks / 5) % NEON.length];
                            victim.getWorld().spawnParticle(Particle.DUST, center.clone().add(Math.cos(angle) * r, 0.1, Math.sin(angle) * r), 2, 0.1, 0, 0.1, 0, new Particle.DustOptions(bc, 2.0f));
                        }
                    }
                }
                ticks++;
            }
        }.runTaskTimer(AlleyPlugin.getInstance(), 0, 1);
    }
}
