package dev.revere.alley.common.reflect.internal.types;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.logger.Logger;
import dev.revere.alley.common.reflect.Reflection;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.locale.LocaleService;
import dev.revere.alley.core.locale.internal.impl.VisualsLocaleImpl;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * @author Emmy
 * @project Alley
 * @since 03/04/2025
 */
public class ActionBarReflectionServiceImpl implements Reflection {
    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.legacyAmpersand();

    /**
     * Method to send an action bar message to a player in a specific interval.
     * 在指定的时间间隔内向玩家发送操作栏消息。
     *
     * @param player          The player.
     *                        玩家。
     * @param message         The message.
     *                        消息。
     * @param durationSeconds The duration to show the message (in seconds).
     *                        显示消息的持续时间（以秒为单位）。
     */
    public void sendMessage(Player player, String message, int durationSeconds) {
        try {
            Component component = SERIALIZER.deserialize(CC.translate(message));
            player.sendActionBar(component);

            if (durationSeconds > 0) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.sendActionBar(Component.empty());
                    }
                }.runTaskLater(AlleyPlugin.getInstance(), durationSeconds * 20L);
            }
        } catch (Exception exception) {
            Logger.logException("An error occurred while trying to send an action bar message to " + player.getName(), exception);
        }
    }

    /**
     * Method to send an action bar message to a player.
     * 向玩家发送操作栏消息。
     *
     * @param player  The player to send the message to.
     *                要发送消息的玩家。
     * @param message The message to send.
     *                要发送的消息。
     */
    public void sendMessage(Player player, String message) {
        try {
            Component component = SERIALIZER.deserialize(CC.translate(message));
            player.sendActionBar(component);
        } catch (Exception exception) {
            Logger.logException("An error occurred while trying to send an action bar message to " + player.getName(), exception);
        }
    }

    /**
     * Sends a death message to the killer.
     * 向击杀者发送死亡消息。
     *
     * @param killer The player who killed the victim.
     *               击杀受害者的玩家。
     * @param victim The player who died.
     *               死亡的玩家。
     */
    public void sendDeathMessage(Player killer, Player victim) {
        LocaleService localeService = AlleyPlugin.getInstance().getService(LocaleService.class);

        if (localeService.getBoolean(VisualsLocaleImpl.ACTIONBAR_DEATH_MESSAGE_ENABLED_BOOLEAN)) {
            Profile victimProfile = AlleyPlugin.getInstance().getService(ProfileService.class).getProfile(victim.getUniqueId());
            Profile killerProfile = AlleyPlugin.getInstance().getService(ProfileService.class).getProfile(killer.getUniqueId());

            String deathMessage = localeService.getString(VisualsLocaleImpl.ACTIONBAR_DEATH_MESSAGE_FORMAT)
                    .replace("{victim}", victim.getName())
                    .replace("{killer}", killer.getName())
                    .replace("{victim-name-color}", String.valueOf(victimProfile.getNameColor()))
                    .replace("{killer-name-color}", String.valueOf(killerProfile.getNameColor()));
            this.sendMessage(killer, deathMessage, 3);
        }
    }

    /**
     * Visualizes the target's health in the action bar for a player.
     * 在玩家的操作栏中可视化目标的生命值。
     *
     * @param player The player who will see the target's health.
     *               将看到目标生命值的玩家。
     * @param target The player whose health will be visualized.
     *               其生命值将被可视化的玩家。
     */
    public void visualizeTargetHealth(Player player, Player target) {
        LocaleService localeService = AlleyPlugin.getInstance().getService(LocaleService.class);

        String message = localeService.getString(VisualsLocaleImpl.ACTIONBAR_HEALTH_INDICATOR_MESSAGE_FORMAT)
                .replace("{target}", target.getName())
                .replace("{name-color}", AlleyPlugin.getInstance().getService(ProfileService.class).getProfile(target.getUniqueId()).getNameColor().toString());

        String symbol = localeService.getString(VisualsLocaleImpl.ACTIONBAR_HEALTH_INDICATOR_SYMBOL_APPEARANCE);
        String fullColor = localeService.getString(VisualsLocaleImpl.ACTIONBAR_HEALTH_INDICATOR_SYMBOL_COLOR_FULL);
        String halfColor = localeService.getString(VisualsLocaleImpl.ACTIONBAR_HEALTH_INDICATOR_SYMBOL_COLOR_HALF);
        String emptyColor = localeService.getString(VisualsLocaleImpl.ACTIONBAR_HEALTH_INDICATOR_SYMBOL_COLOR_EMPTY);

        int maxHealth = (int) target.getMaxHealth() / 2;
        double rawHealth = target.getHealth() / 2;
        int currentHealth = (int) Math.ceil(rawHealth);

        StringBuilder healthBar = new StringBuilder();
        for (int i = 0; i < maxHealth; i++) {
            if (i < currentHealth) {
                healthBar.append(CC.translate(fullColor + symbol));
            } else if (i == currentHealth && rawHealth % 1 != 0) {
                healthBar.append(CC.translate(halfColor + symbol));
            } else {
                healthBar.append(CC.translate(emptyColor + symbol));
            }
        }

        String finalMessage = CC.translate(message.replace("{health-bar}", healthBar.toString()));
        this.sendMessage(player, finalMessage);
    }
}