package dev.revere.alley.feature.cosmetic.internal.repository.impl.killeffect;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.feature.cosmetic.annotation.CosmeticData;
import dev.revere.alley.feature.cosmetic.model.CosmeticType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 18/06/2026
 */
@CosmeticData(type = CosmeticType.KILL_EFFECT, name = "Pigstep", description = "A flying pig with pigstep music.", permission = "", icon = Material.PIG_SPAWN_EGG, slot = 20, price = 800)
public class PigstepKillEffect extends BaseKillEffect {
    private static final int ASCENT_TICKS = 50;
    private static final double INITIAL_ASCENT_SPEED = 0.55;

    @Override public void execute(Player victim) {
        Location loc = victim.getLocation().clone();
        World world = loc.getWorld();
        if (world == null) return;

        Pig pig = world.spawn(loc, Pig.class, entity -> {
            entity.setAI(false);
            entity.setInvulnerable(true);
            entity.setGravity(false);
            entity.setSilent(true);
            entity.setCollidable(false);
            entity.setPersistent(false);
        });
        world.playSound(loc, Sound.MUSIC_DISC_PIGSTEP, 1f, 1f);

        new BukkitRunnable() {
            private int ticks;
            private double height;

            @Override
            public void run() {
                if (!pig.isValid() || this.ticks >= ASCENT_TICKS) {
                    pig.remove();
                    cancel();
                    return;
                }

                double progress = this.ticks / (double) (ASCENT_TICKS - 1);
                double ascentSpeed = INITIAL_ASCENT_SPEED * (1.0 - progress);
                this.height += ascentSpeed;
                pig.teleport(loc.clone().add(0.0, this.height, 0.0));

                Location trail = pig.getLocation().subtract(0.0, 0.55, 0.0);
                world.spawnParticle(Particle.FLAME, trail, 7, 0.18, 0.15, 0.18, 0.025);
                world.spawnParticle(Particle.SMALL_FLAME, trail, 4, 0.12, 0.1, 0.12, 0.015);

                if (this.ticks % 2 == 0) {
                    float pitch = (float) (0.5 + progress * 1.5);
                    world.playSound(pig.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.45f, pitch);
                }
                this.ticks++;
            }
        }.runTaskTimer(AlleyPlugin.getInstance(), 0L, 1L);
    }
}
