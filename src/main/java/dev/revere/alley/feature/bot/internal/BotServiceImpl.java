package dev.revere.alley.feature.bot.internal;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.bootstrap.AlleyContext;
import dev.revere.alley.bootstrap.annotation.Service;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.config.ConfigService;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.core.profile.enums.ProfileState;
import dev.revere.alley.feature.arena.Arena;
import dev.revere.alley.feature.arena.ArenaService;
import dev.revere.alley.feature.bot.BotDifficultyProfile;
import dev.revere.alley.feature.bot.BotAiMode;
import dev.revere.alley.feature.bot.BotService;
import dev.revere.alley.feature.bot.listener.BotMatchListener;
import dev.revere.alley.feature.bot.match.BotMatchSession;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.kit.setting.types.mode.KitSettingBotQueue;
import dev.revere.alley.feature.kit.setting.types.mode.KitSettingGomoku;
import dev.revere.alley.feature.party.PartyService;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service(provides = BotService.class, priority = 230)
public class BotServiceImpl implements BotService {
    private final Map<String, BotDifficultyProfile> profiles = new LinkedHashMap<>();
    private final Map<UUID, BotMatchSession> playerSessions = new ConcurrentHashMap<>();
    private final Map<UUID, BotMatchSession> entitySessions = new ConcurrentHashMap<>();

    @Override
    public void initialize(AlleyContext context) {
        loadProfiles();
        cleanupStaleBots();
        context.getPlugin().getServer().getScheduler().runTaskLater(
                context.getPlugin(), this::cleanupStaleBots, 40L);
        context.getPlugin().getServer().getPluginManager()
                .registerEvents(new BotMatchListener(this), context.getPlugin());
    }

    @Override
    public void shutdown(AlleyContext context) {
        for (BotMatchSession session : new ArrayList<>(playerSessions.values())) {
            session.shutdown();
        }
        playerSessions.clear();
        entitySessions.clear();
    }

    private void loadProfiles() {
        profiles.clear();
        FileConfiguration config = AlleyPlugin.getInstance().getService(ConfigService.class).getBotConfig();
        if (config == null) return;

        ConfigurationSection section = config.getConfigurationSection("profiles");
        if (section == null) return;
        for (String id : section.getKeys(false)) {
            ConfigurationSection profileSection = section.getConfigurationSection(id);
            if (profileSection != null) profiles.put(id.toLowerCase(), BotDifficultyProfile.fromConfig(id, profileSection));
        }
    }

    private void cleanupStaleBots() {
        Set<String> botNames = new HashSet<>();
        for (BotDifficultyProfile profile : profiles.values()) {
            String name = "Bot_" + profile.getId();
            botNames.add((name.length() > 16 ? name.substring(0, 16) : name).toLowerCase());
        }

        List<NPC> staleBots = new ArrayList<>();
        for (NPC npc : CitizensAPI.getNPCRegistry().sorted()) {
            boolean alleyBot = npc.data().get("alley-bot", false);
            if (alleyBot || botNames.contains(npc.getName().toLowerCase())) staleBots.add(npc);
        }
        staleBots.forEach(NPC::destroy);
    }

    @Override
    public Map<String, BotDifficultyProfile> getProfiles() {
        return Collections.unmodifiableMap(profiles);
    }

    @Override
    public int getActivePlayerCount() {
        return playerSessions.size();
    }

    @Override
    public BotMatchSession getSession(Player player) {
        return player == null ? null : playerSessions.get(player.getUniqueId());
    }

    @Override
    public BotMatchSession getSession(Entity entity) {
        if (entity == null) return null;
        BotMatchSession session = playerSessions.get(entity.getUniqueId());
        return session != null ? session : entitySessions.get(entity.getUniqueId());
    }

    @Override
    public boolean isKitSupported(Kit kit) {
        return kit != null && kit.isEnabled() && kit.isSettingEnabled(KitSettingBotQueue.class);
    }

    @Override
    public boolean startMatch(Player player, Kit kit, String difficultyId) {
        FileConfiguration config = AlleyPlugin.getInstance().getService(ConfigService.class).getBotConfig();
        if (config == null || !config.getBoolean("enabled", true)) {
            player.sendMessage(CC.translate("&cBot matches are disabled."));
            return false;
        }
        if (!AlleyPlugin.getInstance().getServer().getPluginManager().isPluginEnabled("Citizens")) {
            player.sendMessage(CC.translate("&cCitizens is required for bot matches."));
            return false;
        }

        Profile profile = AlleyPlugin.getInstance().getService(ProfileService.class).getProfile(player.getUniqueId());
        if (profile == null || profile.getState() != ProfileState.LOBBY || getSession(player) != null) {
            player.sendMessage(CC.translate("&cYou cannot start a bot match right now."));
            return false;
        }
        if (AlleyPlugin.getInstance().getService(PartyService.class).getParty(player) != null) {
            player.sendMessage(CC.translate("&cLeave your party before starting a bot match."));
            return false;
        }
        if (!isKitSupported(kit)) {
            player.sendMessage(CC.translate("&cThat kit does not support bot matches."));
            return false;
        }
        boolean gomokuKit = kit.isSettingEnabled(KitSettingGomoku.class);
        if (gomokuKit != (kit.getBotAiMode() == BotAiMode.GOMOKU)) {
            player.sendMessage(CC.translate("&cThis kit has an incompatible bot AI mode. "
                    + "Gomoku kits must use GOMOKU, and other kits must not use it."));
            return false;
        }

        BotDifficultyProfile difficulty = profiles.get(difficultyId.toLowerCase());
        if (difficulty == null) {
            player.sendMessage(CC.translate("&cThat bot difficulty does not exist."));
            return false;
        }

        Arena arena = AlleyPlugin.getInstance().getService(ArenaService.class).getRandomArena(kit);
        if (arena == null || arena.getPos1() == null || arena.getPos2() == null) {
            player.sendMessage(CC.translate("&cThere are no available arenas for this kit."));
            return false;
        }

        BotMatchSession session = new BotMatchSession(this, player, kit, arena, difficulty, config);
        playerSessions.put(player.getUniqueId(), session);
        boolean started;
        try {
            started = session.start();
        } catch (RuntimeException exception) {
            AlleyPlugin.getInstance().getLogger().severe(
                    "Could not start a bot match for " + player.getName() + ": " + exception.getMessage());
            started = false;
        }
        if (!started) {
            session.abortStart();
            player.sendMessage(CC.translate("&cThe bot match could not be started."));
            return false;
        }
        return true;
    }

    @Override
    public void endMatch(Player player, boolean playerWon) {
        BotMatchSession session = getSession(player);
        if (session != null) session.finish(playerWon);
    }

    public void registerBot(BotMatchSession session, Player bot) {
        entitySessions.put(bot.getUniqueId(), session);
    }

    public void complete(BotMatchSession session) {
        playerSessions.remove(session.getPlayerId(), session);
        if (session.getBotId() != null) entitySessions.remove(session.getBotId(), session);
    }
}
