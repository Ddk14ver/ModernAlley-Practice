package dev.revere.alley.feature.queue.listener;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.core.profile.enums.ProfileState;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.kit.KitService;
import dev.revere.alley.feature.match.MatchState;
import dev.revere.alley.feature.queue.Queue;
import dev.revere.alley.feature.queue.QueueService;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class PlayAgainListener implements Listener {
    public static final String KIT_KEY = "play_again_kit";

    @EventHandler(priority = EventPriority.HIGH)
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (handlePlayAgain(event.getPlayer(), event.getItem())) event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (handlePlayAgain(player, event.getCurrentItem())) event.setCancelled(true);
    }

    private boolean handlePlayAgain(Player player, ItemStack item) {
        if (item == null || item.getType() != Material.PAPER || !item.hasItemMeta()) return false;
        String displayName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
        if (!"Play Again".equals(displayName)) return false;

        NamespacedKey key = new NamespacedKey(AlleyPlugin.getInstance(), KIT_KEY);
        String kitName = item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (kitName == null || kitName.isBlank()) return false;

        Kit kit = AlleyPlugin.getInstance().getService(KitService.class).getKit(kitName);
        if (kit == null) {
            player.sendMessage(CC.translate("&cThat kit no longer exists."));
            return true;
        }

        Queue queue = AlleyPlugin.getInstance().getService(QueueService.class).getQueues().stream()
                .filter(candidate -> candidate.getKit().equals(kit) && !candidate.isRanked() && !candidate.isDuos())
                .findFirst()
                .orElse(null);
        if (queue == null) {
            player.sendMessage(CC.translate("&cThat queue is no longer available."));
            return true;
        }

        Profile profile = AlleyPlugin.getInstance().getService(ProfileService.class).getProfile(player.getUniqueId());
        boolean queued = false;
        if (profile != null && profile.getState() == ProfileState.LOBBY) {
            queue.addPlayer(player, 0);
            queued = profile.getQueueProfile() != null && profile.getQueueProfile().getQueue() == queue;
        } else if (profile != null && profile.getState() == ProfileState.PLAYING
                && profile.getMatch() != null && profile.getMatch().getState() == MatchState.ENDING_MATCH) {
            queued = queue.reserveAfterMatch(player, 0);
            if (queued) {
                player.sendMessage(CC.translate("&aJoined the &6" + kit.getDisplayName()
                        + " &aqueue. Matchmaking activates when you reach spawn."));
            }
        }

        if (!queued) {
            player.sendMessage(CC.translate("&cYou cannot use Play Again right now."));
            return true;
        }

        item.setAmount(0);
        return true;
    }
}
