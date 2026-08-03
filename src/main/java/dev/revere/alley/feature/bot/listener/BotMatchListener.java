package dev.revere.alley.feature.bot.listener;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.feature.bot.BotAiMode;
import dev.revere.alley.feature.bot.internal.BotServiceImpl;
import dev.revere.alley.feature.bot.match.BotMatchSession;
import dev.revere.alley.feature.match.internal.types.GomokuItems;
import dev.revere.alley.feature.knockback.KnockbackManager;
import dev.revere.alley.feature.knockback.data.PlayerKnockbackData;
import lombok.RequiredArgsConstructor;
import net.citizensnpcs.api.event.NPCDeathEvent;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.block.Action;
import org.bukkit.projectiles.ProjectileSource;

@RequiredArgsConstructor
public class BotMatchListener implements Listener {
    private final BotServiceImpl service;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageEvent event) {
        BotMatchSession session = service.getSession(event.getEntity());
        if (session == null) return;

        if (session.getAiMode() == BotAiMode.GOMOKU) {
            event.setCancelled(true);
            return;
        }

        if (!session.isRunning() || session.isEnded()) {
            event.setCancelled(true);
            return;
        }

        if (event instanceof EntityDamageByEntityEvent damageByEntity) {
            Entity attacker = resolveAttacker(damageByEntity.getDamager());
            boolean pearlSelfDamage = damageByEntity.getDamager() instanceof EnderPearl
                    && attacker != null
                    && attacker.getUniqueId().equals(event.getEntity().getUniqueId());
            if (!pearlSelfDamage && !isExpectedOpponent(session, event.getEntity(), attacker)) {
                event.setCancelled(true);
                return;
            }
        }

        // Lethal hits must reach the native death pipeline. Bot match cleanup is
        // started from PlayerDeathEvent after damage and the death pose exist.
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAcceptedBotMeleeDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)
                || !(event.getDamager() instanceof Player attacker)) return;

        BotMatchSession session = service.getSession(victim);
        if (session == null || !session.isRunning() || session.isEnded()
                || session.getBotId() == null
                || !victim.getUniqueId().equals(session.getBotId())
                || !attacker.getUniqueId().equals(session.getPlayerId())) return;

        session.markIncomingMeleeKnockback();
        KnockbackManager knockbackManager = AlleyPlugin.getInstance().getService(KnockbackManager.class);
        PlayerKnockbackData knockbackData = knockbackManager.getPlayerData(victim);
        if (knockbackData.getVelocity() != null) {
            knockbackData.setVelocity(session.applyCombatInputKnockbackReduction(knockbackData.getVelocity()));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBotVelocity(PlayerVelocityEvent event) {
        BotMatchSession session = service.getSession(event.getPlayer());
        if (session == null || session.isEnded() || session.getBotId() == null
                || !event.getPlayer().getUniqueId().equals(session.getBotId())) return;

        event.setVelocity(session.applyCombatInputKnockbackReduction(event.getVelocity()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBotDeath(NPCDeathEvent event) {
        if (!(event.getEvent().getEntity() instanceof Player deadBot)) return;
        BotMatchSession session = service.getSession(deadBot);
        if (session == null || session.isEnded()) return;

        event.getDrops().clear();
        event.setDroppedExp(0);
        session.handleNaturalDeath(deadBot);
    }

    private Entity resolveAttacker(Entity damager) {
        if (!(damager instanceof Projectile projectile)) return damager;
        ProjectileSource shooter = projectile.getShooter();
        return shooter instanceof Entity entity ? entity : damager;
    }

    private boolean isExpectedOpponent(BotMatchSession session, Entity victim, Entity attacker) {
        if (attacker == null) return false;
        if (victim.getUniqueId().equals(session.getPlayerId())) {
            return attacker.getUniqueId().equals(session.getBotId());
        }
        if (victim.getUniqueId().equals(session.getBotId())) {
            return attacker.getUniqueId().equals(session.getPlayerId());
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        BotMatchSession session = service.getSession(event.getPlayer());
        if (session == null) return;
        if (session.getAiMode() == BotAiMode.GOMOKU || !session.isRunning() || !session.canBuild()) {
            event.setCancelled(true);
            return;
        }
        session.recordPlacedBlock(event.getBlockReplacedState());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        BotMatchSession session = service.getSession(event.getPlayer());
        if (session == null) return;
        if (session.getAiMode() == BotAiMode.GOMOKU
                || !session.isRunning() || !session.canBreak(event.getBlock())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        BotMatchSession session = service.getSession(event.getPlayer());
        if (session == null) return;
        if (session.getAiMode() == BotAiMode.GOMOKU || !session.isRunning() || !session.canBuild()) {
            event.setCancelled(true);
            return;
        }
        Block changed = event.getBlockClicked().getRelative(event.getBlockFace());
        session.recordPlacedBlock(changed.getState());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onGomokuPlace(PlayerInteractEvent event) {
        BotMatchSession session = service.getSession(event.getPlayer());
        if (session == null || session.getAiMode() != BotAiMode.GOMOKU) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getItem() == null || event.getItem().getType() != Material.ENDER_PEARL) return;

        event.setCancelled(true);
        session.handleGomokuPlacement(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onGomokuSurrender(PlayerItemConsumeEvent event) {
        BotMatchSession session = service.getSession(event.getPlayer());
        if (session == null || session.getAiMode() != BotAiMode.GOMOKU
                || !GomokuItems.isSurrenderPotion(event.getItem())) return;

        event.setCancelled(true);
        session.surrenderGomoku();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onGomokuProjectile(ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() instanceof Player player && isGomoku(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onGomokuDrop(PlayerDropItemEvent event) {
        if (isGomoku(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onGomokuSwap(PlayerSwapHandItemsEvent event) {
        if (isGomoku(event.getPlayer())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onGomokuFlight(PlayerToggleFlightEvent event) {
        if (isGomoku(event.getPlayer()) && !event.isFlying()) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onGomokuFood(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player player && isGomoku(player)) event.setCancelled(true);
    }

    private boolean isGomoku(Player player) {
        BotMatchSession session = service.getSession((Entity) player);
        return session != null && session.getAiMode() == BotAiMode.GOMOKU;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        BotMatchSession session = service.getSession(event.getPlayer());
        if (session != null) session.shutdown();
    }
}
