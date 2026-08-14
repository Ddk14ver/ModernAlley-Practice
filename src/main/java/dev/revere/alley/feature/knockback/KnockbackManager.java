package dev.revere.alley.feature.knockback;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.bootstrap.AlleyContext;
import dev.revere.alley.bootstrap.annotation.Service;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.kit.KitService;
import dev.revere.alley.feature.kit.setting.types.combat.KitSettingOldHitDelay;
import dev.revere.alley.feature.knockback.data.PlayerKnockbackData;
import dev.revere.alley.feature.knockback.listener.KnockbackListener;
import dev.revere.alley.feature.knockback.listener.PotionMotionListener;
import dev.revere.alley.feature.knockback.packet.MisplaceHandler;
import dev.revere.alley.feature.knockback.hitbox.HitDetection;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

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
    private static final String HIT_DELAY_FORMAT_KEY = "hit_delay_format";
    private static final String HIT_DELAY_FORMAT_NMS_WINDOW = "nms_window";

    private final Map<String, KnockbackProfile> profiles = new LinkedHashMap<>();
    private final Map<UUID, PlayerKnockbackData> playerData = new ConcurrentHashMap<>();
    private HitDetection hitDetection;
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
        synchronizeHitDelayConfiguration();

        // Register listeners
        this.knockbackListener = new KnockbackListener(this);
        AlleyPlugin.getInstance().getServer().getPluginManager()
                .registerEvents(this.knockbackListener, AlleyPlugin.getInstance());
        this.hitDetection = new HitDetection(this);
        AlleyPlugin.getInstance().getServer().getPluginManager()
                .registerEvents(this.hitDetection, AlleyPlugin.getInstance());
        this.potionMotionListener = new PotionMotionListener(this);
        AlleyPlugin.getInstance().getServer().getPluginManager()
                .registerEvents(this.potionMotionListener, AlleyPlugin.getInstance());

        // Misplace handler (ProtocolLib)
        this.misplaceHandler = new MisplaceHandler(this);
        this.misplaceHandler.enable();

        // Tick loop for packet delay queue
        AlleyPlugin.getInstance().getServer().getScheduler().runTaskTimer(AlleyPlugin.getInstance(), () -> {
            currentTick++;
            if (hitDetection != null) hitDetection.tick();
            if (misplaceHandler != null) misplaceHandler.tick();
        }, 1L, 1L);
    }

    @Override
    public void shutdown(AlleyContext context) {
        if (this.hitDetection != null) {
            this.hitDetection.clear();
        }
        if (this.misplaceHandler != null) {
            this.misplaceHandler.disable();
        }
    }

    // --- Profiles ---
    public void reloadProfiles() {
        profiles.clear();
        for (File f : profilesDir.listFiles((d, n) -> n.endsWith(".yml"))) {
            String name = f.getName().replace(".yml", "");
            YamlConfiguration config = YamlConfiguration.loadConfiguration(f);
            boolean configChanged = false;
            boolean hitDelayMigrated = false;
            if (!config.isSet(HIT_DELAY_FORMAT_KEY)) {
                int legacyDelay = Math.max(0, config.getInt("hit_delay", 10));
                int nmsWindow = (int) Math.min((long) legacyDelay * 2L, Integer.MAX_VALUE);
                config.set("hit_delay", nmsWindow);
                config.set(HIT_DELAY_FORMAT_KEY, HIT_DELAY_FORMAT_NMS_WINDOW);
                configChanged = true;
                hitDelayMigrated = true;
            } else if (!HIT_DELAY_FORMAT_NMS_WINDOW.equalsIgnoreCase(
                    config.getString(HIT_DELAY_FORMAT_KEY, ""))) {
                AlleyPlugin.getInstance().getLogger().warning(
                        "[HitDelay] Unknown hit-delay format in " + f.getName()
                                + "; values were left unchanged.");
            }
            if (!config.isSet("potion.speed_compensation")) {
                boolean legacyBuiltIn = name.equalsIgnoreCase("1_7") || name.equalsIgnoreCase("1_8");
                config.set("potion.speed_compensation", legacyBuiltIn ? 0.0 : 1.0);
                configChanged = true;
            }
            if (!config.isSet("potion.horizontal_compensation")) {
                config.set("potion.horizontal_compensation", 0.0);
                configChanged = true;
            }
            if (name.equalsIgnoreCase("1_8")
                    && Math.abs(config.getDouble("hitbox.length", 0.7D) - 0.7D) < 1.0E-6D
                    && Math.abs(config.getDouble("hitbox.height", 1.8D) - 1.8D) < 1.0E-6D) {
                config.set("hitbox.length", 0.8D);
                config.set("hitbox.height", 2.0D);
                configChanged = true;
            }
            if (configChanged) {
                try {
                    config.save(f);
                    if (hitDelayMigrated) {
                        AlleyPlugin.getInstance().getLogger().info(
                                "[HitDelay] Migrated " + f.getName()
                                        + " to the NMS hurt-window format.");
                    }
                } catch (java.io.IOException exception) {
                    AlleyPlugin.getInstance().getLogger().warning(
                            "Unable to save migrated knockback settings to " + f.getName()
                                    + ": " + exception.getMessage());
                }
            }
            profiles.put(name.toLowerCase(), new KnockbackProfile(name, config));
        }
        if (profiles.isEmpty()) {
            AlleyPlugin.getInstance().getLogger().warning("No knockback profiles found in knockback/ folder!");
        }

        // A live reload must update the NMS window immediately; otherwise the
        // next damage event is evaluated against the previous profile's half.
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerKnockbackData data = playerData.get(player.getUniqueId());
            if (data != null && data.getProfileName() != null) {
                applyHitDelayWindow(player);
                KnockbackProfile profile = getProfile(data.getProfileName());
                if (profile == null) profile = getDefaultProfile();
                applyEntityInteractionRange(player, profile);
            }
        }
    }

    public KnockbackProfile getProfile(String name) {
        if (name == null) return profiles.get("default");
        String key = name.toLowerCase();
        // Kits may store the profile with or without the ".yml" extension; strip it so
        // both "1_8" and "1_8.yml" resolve to the same loaded profile.
        if (key.endsWith(".yml")) {
            key = key.substring(0, key.length() - 4);
        }
        return profiles.get(key);
    }

    public KnockbackProfile getDefaultProfile() {
        return profiles.get("default");
    }

    public Collection<KnockbackProfile> getProfiles() {
        return profiles.values();
    }

    private void synchronizeHitDelayConfiguration() {
        KitService kitService = AlleyPlugin.getInstance().getService(KitService.class);
        if (kitService.getKits().isEmpty()) return;

        int synchronizedKits = 0;
        for (Kit kit : kitService.getKits()) {
            if (synchronizeKitHitDelay(kit)) {
                kitService.saveKit(kit);
                synchronizedKits++;
            }
        }

        if (synchronizedKits > 0) {
            AlleyPlugin.getInstance().getLogger().info(
                    "[HitDelay] Synchronized " + synchronizedKits
                            + " enabled kit setting(s) to their knockback profiles.");
        }
    }

    public boolean synchronizeKitHitDelay(Kit kit) {
        KitSettingOldHitDelay setting = kit.getSetting(KitSettingOldHitDelay.class);
        if (setting == null || !setting.isEnabled()) return false;

        String profileName = kit.getKnockbackProfile();
        if (profileName == null || profileName.isBlank()) profileName = "default";
        KnockbackProfile profile = getProfile(profileName);
        if (profile == null) {
            AlleyPlugin.getInstance().getLogger().warning(
                    "[HitDelay] Enabled kit '" + kit.getName()
                            + "' references missing knockback profile '" + profileName
                            + "'; its oldHitDelay could not be synchronized.");
            return false;
        }

        int profileDelay = Math.max(0, profile.getHitDelay());
        int kitDelay = Math.max(0, setting.getValue());
        if (kitDelay == profileDelay) return false;

        AlleyPlugin.getInstance().getLogger().warning(
                "[HitDelay] Kit '" + kit.getName() + "' oldHitDelay=" + kitDelay
                        + " conflicts with profile '" + profile.getName() + "' hit_delay="
                        + profileDelay + "; the kit value was overwritten with the profile value.");
        setting.setValue(profileDelay);
        return true;
    }

    // --- Player data ---
    public PlayerKnockbackData getPlayerData(UUID uuid) {
        return playerData.computeIfAbsent(uuid, k -> new PlayerKnockbackData());
    }

    public PlayerKnockbackData getPlayerData(Player p) {
        return getPlayerData(p.getUniqueId());
    }

    /**
     * Synchronizes the movement state used to select ground/air KB values.
     * Client-backed players normally reach this through PlayerMoveEvent; server-side
     * players call it from their tick adapter because they have no movement packets.
     */
    public void updateMovementState(Player player) {
        PlayerKnockbackData data = getPlayerData(player);
        boolean onGround = player.isOnGround();
        data.setOnGround(onGround);
        if (onGround) data.setLastGroundY(player.getLocation().getY());
    }

    public void removePlayer(UUID uuid) {
        PlayerKnockbackData data = playerData.remove(uuid);
        if (data != null) {
            // Cancel any one-tick legacy velocity delivery that still references
            // this player's old data object.
            data.setVelocity(null);
            data.clearLegacyState();
        }
        if (hitDetection != null) hitDetection.clearPlayer(uuid);
        if (misplaceHandler != null) misplaceHandler.clearPlayer(uuid);
    }

    public void clearKnockback(Player player) {
        PlayerKnockbackData data = getPlayerData(player);
        data.setProfileName(null);
        data.setConfiguredHitDelayWindow(-1);
        applyEntityInteractionRange(player, null);
        resetHitDelayState(player);
        if (misplaceHandler != null) misplaceHandler.clearPlayer(player.getUniqueId());
    }

    /** Clears the live/category state while retaining the player's assigned profile. */
    public void resetHitDelayState(Player player) {
        PlayerKnockbackData data = getPlayerData(player);
        data.clearLegacyState();
        data.setVelocity(null);
        player.setNoDamageTicks(0);
        if (hitDetection != null) hitDetection.clearPlayer(player.getUniqueId());

        if (data.getProfileName() == null) {
            // Do not let a match's hurt window leak into the lobby or next kit.
            data.setConfiguredHitDelayWindow(-1);
            player.setMaximumNoDamageTicks(KitSettingOldHitDelay.DEFAULT_DELAY);
        } else {
            applyHitDelayWindow(player);
        }
    }

    public MisplaceHandler getMisplaceHandler() { return misplaceHandler; }
    public PotionMotionListener getPotionMotionListener() { return potionMotionListener; }
    public long getCurrentTick() { return currentTick; }

    /**
     * Consumes the same pending profile velocity used by PlayerVelocityEvent.
     * This is also the supported bridge for Player implementations without a
     * real client connection, such as the built-in Bot player.
     */
    public Vector consumePendingKnockback(Player player, Vector nativeVelocity) {
        return this.knockbackListener == null
                ? null
                : this.knockbackListener.consumePendingKnockback(player, nativeVelocity);
    }

    public Vector consumeServerControlledVelocity(Player player) {
        return getPlayerData(player).consumeServerControlledVelocity();
    }

    /**
     * Delivers a pending profile velocity for a server-side Player implementation
     * that does not emit a client velocity event (for example the built-in Bot).
     * Returns null when another event already consumed the pending state.
     */
    public Vector deliverPendingKnockback(Player player) {
        Vector applied = consumeServerControlledVelocity(player);
        if (applied == null) applied = consumePendingKnockback(player, player.getVelocity());
        if (applied != null) {
            player.setVelocity(applied);
            return applied;
        }
        return null;
    }

    public void applyLegacyPearlKnockback(Player attacker, Player victim, Vector impactVelocity) {
        if (this.knockbackListener != null) {
            this.knockbackListener.applyLegacyPearlKnockback(attacker, victim, impactVelocity);
        }
    }

    public void applyHitDelayWindow(Player player) {
        int window = getEffectiveNoDamageWindow(getPlayerData(player));
        if (player.getMaximumNoDamageTicks() != window) {
            player.setMaximumNoDamageTicks(window);
        }
    }

    /**
     * Returns whether the player is in NMS's upper hurt-resistance half. This
     * is the branch where a larger incoming amount can only add its damage
     * delta; it must not be treated as a fresh hit for legacy side effects.
     */
    public boolean isInsideHurtResistanceWindow(Player player) {
        int maximumNoDamageTicks = player.getMaximumNoDamageTicks();
        return player.getNoDamageTicks() > maximumNoDamageTicks / 2.0F;
    }

    public void beginLegacyDamage(Player player) {
        PlayerKnockbackData data = getPlayerData(player);
        // Capture the pre-listener state. No-damage modes arm the live counter
        // later in the same event, which must not turn that fresh hit into a
        // false damage-supplement hit during MONITOR processing.
        data.setLegacyDamageWindowActive(
                isInsideHurtResistanceWindow(player), Bukkit.getCurrentTick());
    }

    public boolean wasInsideHurtResistanceWindow(Player player) {
        return getPlayerData(player).isLegacyDamageWindowActive(Bukkit.getCurrentTick());
    }

    private int getEffectiveNoDamageWindow(PlayerKnockbackData data) {
        if (data.getConfiguredHitDelayWindow() >= 0) {
            return data.getConfiguredHitDelayWindow();
        }

        KnockbackProfile profile = getProfile(data.getProfileName());
        if (profile == null) profile = getDefaultProfile();
        return profile == null ? KitSettingOldHitDelay.DEFAULT_DELAY : Math.max(0, profile.getHitDelay());
    }

    private void applyEntityInteractionRange(Player player, KnockbackProfile profile) {
        AttributeInstance attribute = player.getAttribute(Attribute.ENTITY_INTERACTION_RANGE);
        if (attribute == null) return;

        double range = profile == null
                ? attribute.getDefaultValue()
                : Math.max(0.0D, profile.getEntityInteractionRange());
        attribute.setBaseValue(range);
    }

    /**
     * Apply a KB profile to a player (called by Match/FFA when entering a game).
     */
    public void applyKnockback(Player player, String profileName) {
        applyKnockback(player, profileName, -1);
    }

    /**
     * Enabled oldHitDelay uses the profile's NMS window. Disabled always uses the vanilla
     * window of 20, independently of the selected profile.
     */
    public void applyKnockback(Player player, Kit kit) {
        boolean oldHitDelayEnabled = kit.getKitSettings().stream()
                .filter(KitSettingOldHitDelay.class::isInstance)
                .anyMatch(setting -> setting.isEnabled());
        int hitDelayWindow = oldHitDelayEnabled ? -1 : KitSettingOldHitDelay.DEFAULT_DELAY;
        applyKnockback(player, kit.getKnockbackProfile(), hitDelayWindow);
    }

    /** Returns whether the player is still using the profile selected by this kit. */
    public boolean isKnockbackApplied(Player player, Kit kit) {
        KnockbackProfile expected = getProfile(kit.getKnockbackProfile());
        if (expected == null) expected = getDefaultProfile();

        PlayerKnockbackData data = getPlayerData(player);
        KnockbackProfile applied = getProfile(data.getProfileName());
        if (applied == null) applied = getDefaultProfile();
        return data.getProfileName() != null && applied == expected;
    }

    private void applyKnockback(Player player, String profileName, int hitDelayWindow) {
        if (profileName == null || profileName.isEmpty()) {
            profileName = "default";
        }
        if (misplaceHandler != null) {
            // Never carry delayed movement packets across kit/round transitions.
            misplaceHandler.clearPlayer(player.getUniqueId());
        }
        PlayerKnockbackData data = getPlayerData(player);
        data.setProfileName(profileName);
        data.setConfiguredHitDelayWindow(hitDelayWindow);
        data.setOnGround(player.isOnGround());
        data.setLastGroundY(player.getLocation().getY());
        data.clearLegacyState();
        data.setVelocity(null);
        KnockbackProfile profile = getProfile(profileName);
        if (profile == null) profile = getDefaultProfile();
        applyEntityInteractionRange(player, profile);
        // A profile transition starts a new hurt-resistance timeline. Reset the
        // live counter before changing the window, otherwise the first hit of a
        // new round/kit can be mistaken for a delta hit.
        player.setNoDamageTicks(0);
        applyHitDelayWindow(player);
    }
}
