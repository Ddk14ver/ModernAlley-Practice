package dev.revere.alley.feature.layout.listener;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.feature.kit.KitService;
import dev.revere.alley.feature.kit.setting.types.combat.KitSettingOldOffhand;
import dev.revere.alley.feature.layout.data.LayoutData;
import dev.revere.alley.feature.match.MatchService;
import dev.revere.alley.feature.match.internal.MatchServiceImpl;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.common.text.CC;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Listener for layout book interactions.
 * 布局书交互的监听器。
 * @author Emmy
 * @project Alley
 * @since 04/05/2025
 */
public class LayoutListener implements Listener {
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getType() != Material.BOOK) return;
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;

        String clickedName = ChatColor.stripColor(item.getItemMeta().getDisplayName());

        for (java.util.Map.Entry<String, List<LayoutData>> entry : AlleyPlugin.getInstance().getService(ProfileService.class)
                .getProfile(player.getUniqueId())
                .getProfileData().getLayoutData().getLayouts().entrySet()) {

            String kitName = entry.getKey();
            for (LayoutData layout : entry.getValue()) {
                if (layout == null) continue;
                if (ChatColor.stripColor(layout.getDisplayName()).equalsIgnoreCase(clickedName)) {
                    player.getInventory().setContents(layout.getItems());

                    // Apply the kit's armor
                    dev.revere.alley.feature.kit.Kit kit = AlleyPlugin.getInstance().getService(KitService.class).getKit(kitName);
                    if (kit != null) {
                        player.getInventory().setArmorContents(kit.getArmor());
                        if (!kit.isSettingEnabled(KitSettingOldOffhand.class)) {
                            player.getInventory().setItemInOffHand(layout.getOffhand());
                        }
                    }

                    player.sendMessage(CC.translate("&aYou have selected the layout &6" + layout.getDisplayName() + "&a."));

                    // Apply sword blocking NBT if legacy combat is active for this player
                    MatchService matchService = AlleyPlugin.getInstance().getService(MatchService.class);
                    if (matchService instanceof MatchServiceImpl impl && impl.getLegacyCombatService() != null) {
                        impl.getLegacyCombatService().applyBlockableToSwords(player);
                    }

                    event.setCancelled(true);
                    return;
                }
            }
        }
    }
}