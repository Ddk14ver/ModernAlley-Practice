package dev.revere.alley.feature.cosmetic.internal.repository.impl.killeffect;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.feature.cosmetic.annotation.CosmeticData;
import dev.revere.alley.feature.cosmetic.model.CosmeticType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.Warden;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 18/06/2026
 */
@CosmeticData(type = CosmeticType.KILL_EFFECT, name = "Warden", description = "Warden emerge effect at the victim.", permission = "", icon = Material.SCULK_SHRIEKER, slot = 16, price = 800)
public class WardenKillEffect extends BaseKillEffect {
    private static final double WARDEN_DISTANCE = 2.5;

    @Override public void execute(Player victim) {
        Location loc = victim.getLocation().clone();
        World world = loc.getWorld();
        if (world == null) return;

        for (int i = 0; i < 5; i++) {
            world.spawnParticle(Particle.SONIC_BOOM, loc.clone().add(0, i * 0.5, 0), 3, 0.3, 0, 0.3, 0);
        }
        world.playSound(loc, Sound.ENTITY_WARDEN_EMERGE, 1f, 1f);
        world.spawnParticle(Particle.SCULK_SOUL, loc, 20, 0.5, 0.5, 0.5, 0.1);

        List<Warden> wardens = new ArrayList<>(4);
        double[][] offsets = {
                {WARDEN_DISTANCE, 0.0},
                {-WARDEN_DISTANCE, 0.0},
                {0.0, WARDEN_DISTANCE},
                {0.0, -WARDEN_DISTANCE}
        };
        for (double[] offset : offsets) {
            Location spawn = loc.clone().add(offset[0], 0.0, offset[1]);
            spawn.setDirection(loc.toVector().subtract(spawn.toVector()));
            Warden warden = world.spawn(spawn, Warden.class, entity -> {
                entity.setAI(false);
                entity.setGravity(false);
                entity.setInvulnerable(true);
                entity.setSilent(true);
                entity.setCollidable(false);
                entity.setCanPickupItems(false);
                entity.setPersistent(false);
            });
            wardens.add(warden);
        }

        AlleyPlugin plugin = AlleyPlugin.getInstance();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            wardens.stream().filter(Warden::isValid).forEach(this::playAttackAnimation);
            world.playSound(loc, Sound.ENTITY_WARDEN_ATTACK_IMPACT, 1.2f, 0.8f);
        }, 2L);
        plugin.getServer().getScheduler().runTaskLater(
                plugin, () -> wardens.forEach(Warden::remove), 40L);
    }

    private void playAttackAnimation(Warden warden) {
        try {
            Object handle = warden.getClass().getMethod("getHandle").invoke(warden);
            Object level = handle.getClass().getMethod("level").invoke(handle);
            Method broadcastMethod = null;
            for (Method method : level.getClass().getMethods()) {
                if (method.getName().equals("broadcastEntityEvent")
                        && method.getParameterCount() == 2
                        && method.getParameterTypes()[1] == byte.class) {
                    broadcastMethod = method;
                    break;
                }
            }
            if (broadcastMethod == null) throw new NoSuchMethodException("broadcastEntityEvent");
            broadcastMethod.invoke(level, handle, (byte) 4);
        } catch (ReflectiveOperationException exception) {
            warden.swingMainHand();
        }
    }
}
