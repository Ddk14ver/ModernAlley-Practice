package dev.revere.alley.feature.abilities.types;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.feature.abilities.Ability;
import dev.revere.alley.feature.abilities.AbilityService;
import dev.revere.alley.common.time.DurationFormatter;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.enums.GlobalCooldown;
import dev.revere.alley.common.PlayerUtil;
import dev.revere.alley.common.time.TimeUtil;
import dev.revere.alley.common.text.CC;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;
/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 26/06/2026
 */
public class AntiTrapper extends Ability {

    private final AlleyPlugin plugin = AlleyPlugin.getInstance();
    public static Map<String, Long> cooldownvic;
    public int count;

    public AntiTrapper() {
        super("ANTI_TRAPPER");
        AntiTrapper.cooldownvic = new HashMap<>();
        this.count = 0;
    }

    static {
        AntiTrapper.cooldownvic = new HashMap<>();
    }

    public static boolean isOnCooldownVic(Player player) {
        return AntiTrapper.cooldownvic.containsKey(player.getName())
                && AntiTrapper.cooldownvic.get(player.getName()) > System.currentTimeMillis();
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player damager = (Player) event.getDamager();
            Player victim = (Player) event.getEntity();

            ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);
            AbilityService abilityService = AlleyPlugin.getInstance().getService(AbilityService.class);

            Profile profile = profileService.getProfile(damager.getUniqueId());

            if (!isAbility(damager.getInventory().getItemInMainHand())) {
                return;
            }
            if (isAbility(damager.getInventory().getItemInMainHand())) {
                if (profile.getCooldown(AntiTrapper.class).onCooldown(damager)) {
                    damager.sendMessage(CC.translate("&fYou are on &6&lAntiTrapper &7cooldown for &4" + DurationFormatter.getRemaining(profile.getCooldown(AntiTrapper.class).getRemainingMillis(damager), true, true)));
                    damager.updateInventory();
                    return;
                }

                if (profile.getGlobalCooldown(GlobalCooldown.PARTNER_ITEM).onCooldown(damager)) {
                    damager.sendMessage(CC.translate("&fYou are on &6&lPartner Item &fcooldown for &6" + DurationFormatter.getRemaining(profile.getGlobalCooldown(GlobalCooldown.PARTNER_ITEM).getRemainingMillis(damager), true, true)));
                    damager.updateInventory();
                    return;
                }

                count = count + 1;
                if (count >= 3) {
                    count = 0;

                    // Apply cooldown on third hit
                    // 在第三次命中时应用冷却
                    profile.getCooldown(AntiTrapper.class).applyCooldown(damager, 60 * 1000);
                    profile.getGlobalCooldown(GlobalCooldown.PARTNER_ITEM).applyCooldown(damager,  10 * 1000);

                    // Apply cooldown on victim to prevent interaction
                    // 对被命中者应用冷却以防止交互
                    AntiTrapper.cooldownvic.put(victim.getName(), System.currentTimeMillis() + (15 * 1000));

                    abilityService.sendPlayerMessage(damager, this.getAbility());
                    abilityService.sendTargetMessage(victim, damager, this.getAbility());

                    PlayerUtil.decrement(damager);
                }
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        Profile profile = AlleyPlugin.getInstance().getService(ProfileService.class).getProfile(player.getUniqueId());
        if (action.equals(Action.LEFT_CLICK_AIR) || action.equals(Action.LEFT_CLICK_BLOCK)) {
            if (!isAbility(player.getInventory().getItemInMainHand())) {
                return;
            }
            if (isAbility(player.getInventory().getItemInMainHand())) {
                if (this.hasCooldown(player)) {
                    player.sendMessage(CC.translate("&fYou are on cooldown for &4" + DurationFormatter.getRemaining(profile.getCooldown(AntiTrapper.class).getRemainingMillis(player), true)));
                    event.setCancelled(true);
                    player.updateInventory();
                }
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (AntiTrapper.isOnCooldownVic(player)) {
            long millisLeft = AntiTrapper.cooldownvic.get(event.getPlayer().getName()) - System.currentTimeMillis();
            player.sendMessage(CC.translate("&7You can't place blocks for another &4" + TimeUtil.formatLongMin(millisLeft) + " &7seconds"));
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (AntiTrapper.isOnCooldownVic(player)) {
            long millisLeft = AntiTrapper.cooldownvic.get(event.getPlayer().getName()) - System.currentTimeMillis();
            event.setCancelled(true);
            player.sendMessage(CC.translate("&7You can't break blocks for another &4" + TimeUtil.formatLongMin(millisLeft) + " &7seconds"));
        }
    }

    @EventHandler
    public void onFenceInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            if (block.getType().equals(Material.OAK_FENCE_GATE) || block.getType().equals(Material.CHEST)) {
                if (AntiTrapper.isOnCooldownVic(player)) {
                    long millisLeft = AntiTrapper.cooldownvic.get(event.getPlayer().getName()) - System.currentTimeMillis();
                    event.setCancelled(true);
                    player.sendMessage(CC.translate("&7You can't interact with blocks for another &4" + TimeUtil.formatLongMin(millisLeft) + " &7seconds"));
                }
            }
        }
    }
}
