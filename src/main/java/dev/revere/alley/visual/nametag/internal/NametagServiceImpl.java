package dev.revere.alley.visual.nametag.internal;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.bootstrap.AlleyContext;
import dev.revere.alley.bootstrap.annotation.Service;
import dev.revere.alley.visual.nametag.model.NametagPerspective;
import dev.revere.alley.visual.nametag.NametagService;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 名字标签服务实现，负责管理玩家视角和名字标签的更新。
 * @author Remi
 * @project alley-practice
 * @date 27/06/2025
 */
@Getter
@Service(provides = NametagService.class, priority = 390)
public class NametagServiceImpl implements NametagService, Listener {
    private final AlleyPlugin plugin;

    private final Map<UUID, NametagPerspective> playerPerspectives = new ConcurrentHashMap<>();
    private final NametagRegistry nametagRegistry;

    /**
     * Constructor for DI.
     * 依赖注入的构造函数。
     */
    public NametagServiceImpl(AlleyPlugin plugin) {
        this.plugin = plugin;
        this.nametagRegistry = new NametagRegistry(this);
    }

    @Override
    public void initialize(AlleyContext context) {
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
        this.plugin.getServer().getPluginManager().registerEvents(
                new NametagScoreboardListener(this), this.plugin);
    }

    /**
     * This is the main method to call when a player's state changes (e.g., joining/leaving a match).
     * It triggers a full, two-way re-evaluation of nametags.
     * 当玩家状态发生变化时（如加入/离开比赛）调用的主要方法。
     * 触发名字标签的完整双向重新评估。
     *
     * @param player The player whose state has changed.
     *        状态发生变化的玩家。
     */
    public void updatePlayerState(Player player) {
        if (player == null) return;

        // Scoreboard/team mutations are world-affecting Bukkit operations on
        // 1.21.11 (they also rebuild waypoint connections). Always marshal the
        // complete nametag update back to the primary tick thread.
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(plugin, () -> updatePlayerState(player));
            return;
        }

        NametagPerspective changedPlayerPerspective = playerPerspectives.get(player.getUniqueId());

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (changedPlayerPerspective != null) {
                changedPlayerPerspective.updateNametagFor(onlinePlayer);
            }

            NametagPerspective otherPlayerPerspective = playerPerspectives.get(onlinePlayer.getUniqueId());
            if (otherPlayerPerspective != null) {
                otherPlayerPerspective.updateNametagFor(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        NametagPerspective newPerspective = new NametagPerspective(this, player, this.nametagRegistry);
        playerPerspectives.put(player.getUniqueId(), newPerspective);

        // Run next tick so the joining player's scoreboard is fully attached,
        // but keep the operation synchronous with the server tick thread.
        Bukkit.getScheduler().runTask(plugin, () -> {
            nametagRegistry.sendAllAdapters(player);
            updatePlayerState(player);
        });
        Bukkit.getScheduler().runTaskLater(plugin,
                () -> refreshAfterScoreboardChange(player), 2L);
    }

    /** Rebuild nametag teams after another subsystem replaces the player's scoreboard. */
    public void refreshAfterScoreboardChange(Player player) {
        if (player == null || !player.isOnline()) return;
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(plugin, () -> refreshAfterScoreboardChange(player));
            return;
        }

        NametagPerspective perspective = playerPerspectives.get(player.getUniqueId());
        if (perspective == null) return;

        perspective.getDisplayedAdapters().clear();
        nametagRegistry.sendAllAdapters(player);
        updatePlayerState(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerPerspectives.remove(event.getPlayer().getUniqueId());
        nametagRegistry.cleanupPlayer(event.getPlayer());
    }
}
