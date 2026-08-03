package dev.revere.alley.feature.knockback;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.bootstrap.AlleyContext;
import dev.revere.alley.bootstrap.annotation.Service;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.kit.setting.types.combat.KitSettingOldHitDelay;
import dev.revere.alley.feature.knockback.data.PlayerKnockbackData;
import dev.revere.alley.feature.knockback.listener.KnockbackListener;
import dev.revere.alley.feature.knockback.listener.PotionMotionListener;
import dev.revere.alley.feature.knockback.packet.MisplaceHandler;
import dev.revere.alley.feature.knockback.hitbox.HitDetection;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 05/07/2026
 *
 * Built-in knockback manager. Replaces the old KnockbackAdapter.
 */
@Service(provides = KnockbackManager.class, priority = 150)
public class KnockbackManager implements dev.revere.alley.bootstrap.lifecycle.Service {
    private static final int FIRE_DAMAGE_INTERVAL_TICKS = 20;

    private final Map<String, KnockbackProfile> profiles = new LinkedHashMap<>();
    private final Map<UUID, PlayerKnockbackData> playerData = new ConcurrentHashMap<>();
    private MisplaceHandler misplaceHandler;
    private KnockbackListener knockbackListener;
    private PotionMotionListener potionMotionListener;
    private File profilesDir;
    private long currentTick;

    @Override
    public void initialize(AlleyContext context) {
        profilesDir = new File(AlleyPlugin.getInstance().getDataFolder(), "knockback");
        if (!profilesDir.exists()) {
            profilesDir.mkdirs();
        }
        AlleyPlugin.getInstance().saveResource("knockback/default.yml", false);
        AlleyPlugin.getInstance().saveResource("knockback/1_8.yml", false);
        AlleyPlugin.getInstance().saveResource("knockback/1_7.yml", false);
        reloadProfiles();

        // Register listeners
        this.knockbackListener = new KnockbackListener(this);
        AlleyPlugin.getInstance().getServer().getPluginManager()
                .registerEvents(this.knockbackListener, AlleyPlugin.getInstance());
        AlleyPlugin.getInstance().getServer().getPluginManager()
                .registerEvents(new HitDetection(this), AlleyPlugin.getInstance());
        this.potionMotionListener = new PotionMotionListener(this);
        AlleyPlugin.getInstance().getServer().getPluginManager()
                .registerEvents(this.potionMotionListener, AlleyPlugin.getInstance());

        // Misplace handler (ProtocolLib)
        this.misplaceHandler = new MisplaceHandler(this);
        this.misplaceHandler.enable();

        // Tick loop for packet delay queue
        AlleyPlugin.getInstance().getServer().getScheduler().runTaskTimer(AlleyPlugin.getInstance(), () -> {
            currentTick++;
            prepareLegacyHazardFrames();
            if (misplaceHandler != null) misplaceHandler.tick();
        }, 1L, 1L);
    }

    @Override
    public void shutdown(AlleyContext context) { }

    // --- Profiles ---
    public void reloadProfiles() {
        profiles.clear();
        for (File f : profilesDir.listFiles((d, n) -> n.endsWith(".yml"))) {
            String name = f.getName().replace(".yml", "");
            YamlConfiguration config = YamlConfiguration.loadConfiguration(f);
            boolean configChanged = false;
            if (!config.isSet("potion.speed_compensation")) {
                boolean legacyBuiltIn = name.equalsIgnoreCase("1_7") || name.equalsIgnoreCase("1_8");
                config.set("potion.speed_compensation", legacyBuiltIn ? 0.0 : 1.0);
                configChanged = true;
            }
            if (!config.isSet("potion.horizontal_compensation")) {
                config.set("potion.horizontal_compensation", 0.0);
                configChanged = true;
            }
            if (configChanged) {
                try {
                    config.save(f);
                } catch (java.io.IOException exception) {
                    AlleyPlugin.getInstance().getLogger().warning(
                            "Unable to add missing potion compensation settings to " + f.getName());
                }
            }
            profiles.put(name.toLowerCase(), new KnockbackProfile(name, config));
        }
        if (profiles.isEmpty()) {
            AlleyPlugin.getInstance().getLogger().warning("No knockback profiles found in knockback/ folder!");
        }
    }

    public KnockbackProfile getProfile(String name) {
        return name != null ? profiles.get(name.toLowerCase()) : profiles.get("default");
    }

    public KnockbackProfile getDefaultProfile() {
        return profiles.get("default");
    }

    public Collection<KnockbackProfile> getProfiles() {
        return profiles.values();
    }

    // --- Player data ---
    public PlayerKnockbackData getPlayerData(UUID uuid) {
        return playerData.computeIfAbsent(uuid, k -> new PlayerKnockbackData());
    }

    public PlayerKnockbackData getPlayerData(Player p) {
        return getPlayerData(p.getUniqueId());
    }

    public void removePlayer(UUID uuid) {
        playerData.remove(uuid);
        if (misplaceHandler != null) misplaceHandler.onQuit(Bukkit.getPlayer(uuid));
    }

    public void clearKnockback(Player player) {
        PlayerKnockbackData data = getPlayerData(player);
        data.setProfileName(null);
        data.setConfiguredHitDelay(-1);
        data.setLastAcceptedCombatHitTick(Long.MIN_VALUE);
        data.setLastHazardDamageTick(Long.MIN_VALUE);
        data.setLastFireHazardPreparationTick(Long.MIN_VALUE);
        data.setLastPoisonHazardPreparationTick(Long.MIN_VALUE);
        data.clearLegacyResidual();
    }

    public MisplaceHandler getMisplaceHandler() { return misplaceHandler; }
    public PotionMotionListener getPotionMotionListener() { return potionMotionListener; }
    public long getCurrentTick() { return currentTick; }

    public void applyLegacyPearlKnockback(Player attacker, Player victim) {
        if (this.knockbackListener != null) {
            this.knockbackListener.applyLegacyPearlKnockback(attacker, victim);
        }
    }

    private void prepareLegacyHazardFrames() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerKnockbackData data = getPlayerData(player);

            boolean canTakeFireDamage = player.getFireTicks() > 0
                    && !player.hasPotionEffect(PotionEffectType.FIRE_RESISTANCE);
            if (!canTakeFireDamage) {
                data.setLastFireHazardPreparationTick(Long.MIN_VALUE);
            }
            long lastFirePreparation = data.getLastFireHazardPreparationTick();
            if (canTakeFireDamage && lastFirePreparation == Long.MIN_VALUE) {
                data.setLastFireHazardPreparationTick(this.currentTick);
                lastFirePreparation = this.currentTick;
            }
            boolean fireDue = canTakeFireDamage
                    && player.getFireTicks() % FIRE_DAMAGE_INTERVAL_TICKS == 0
                    && this.currentTick - lastFirePreparation >= FIRE_DAMAGE_INTERVAL_TICKS;

            PotionEffect poison = player.getPotionEffect(PotionEffectType.POISON);
            boolean poisonDue = false;
            if (poison != null && player.getHealth() > 1.0) {
                int rawInterval = 25 >> Math.min(poison.getAmplifier(), 30);
                int interval = Math.max(1, rawInterval);
                boolean nativePoisonDue = rawInterval <= 0
                        || (poison.getDuration() < 0
                        ? this.currentTick % interval == 0
                        : poison.getDuration() % interval == 0);
                long lastPoisonPreparation = data.getLastPoisonHazardPreparationTick();
                poisonDue = nativePoisonDue
                        && (lastPoisonPreparation == Long.MIN_VALUE
                        || this.currentTick - lastPoisonPreparation >= interval);
            } else {
                data.setLastPoisonHazardPreparationTick(Long.MIN_VALUE);
            }

            if (!fireDue && !poisonDue) continue;
            if (fireDue) data.setLastFireHazardPreparationTick(this.currentTick);
            if (poisonDue) data.setLastPoisonHazardPreparationTick(this.currentTick);
            player.setNoDamageTicks(0);
            data.setLastHazardDamageTick(this.currentTick);
        }
    }

    /**
     * Maps the kit's logical hit delay to Bukkit's half-window damage gate.
     * A logical delay of 10 must write 20 so the next full hit is admitted after 10 ticks.
     */
    public static int toVanillaNoDamageWindow(int hitDelay) {
        return (int) Math.min((long) Math.max(0, hitDelay) * 2L, Integer.MAX_VALUE);
    }

    /**
     * Apply a KB profile to a player (called by Match/FFA when entering a game).
     */
    public void applyKnockback(Player player, String profileName) {
        applyKnockback(player, profileName, -1);
    }

    /**
     * Applies a kit's knockback profile and its independent hit delay.
     */
    public void applyKnockback(Player player, Kit kit) {
        int hitDelay = kit.getKitSettings().stream()
                .filter(KitSettingOldHitDelay.class::isInstance)
                .filter(setting -> setting.isEnabled())
                .mapToInt(setting -> Math.max(0, setting.getValue()))
                .findFirst()
                .orElse(-1);
        applyKnockback(player, kit.getKnockbackProfile(), hitDelay);
    }

    private void applyKnockback(Player player, String profileName, int hitDelay) {
        if (profileName == null || profileName.isEmpty()) {
            profileName = "default";
        }
        PlayerKnockbackData data = getPlayerData(player);
        data.setProfileName(profileName);
        data.setConfiguredHitDelay(hitDelay);
        data.setOnGround(player.isOnGround());
        data.setLastGroundY(player.getLocation().getY());
        data.setLastAcceptedCombatHitTick(Long.MIN_VALUE);
        data.setLastHazardDamageTick(Long.MIN_VALUE);
        data.setLastFireHazardPreparationTick(Long.MIN_VALUE);
        data.setLastPoisonHazardPreparationTick(Long.MIN_VALUE);
        data.clearLegacyResidual();
        if (hitDelay >= 0) {
            player.setMaximumNoDamageTicks(toVanillaNoDamageWindow(hitDelay));
        }
    }
}
