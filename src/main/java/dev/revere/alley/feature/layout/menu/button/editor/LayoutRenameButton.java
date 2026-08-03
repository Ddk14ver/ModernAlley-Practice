package dev.revere.alley.feature.layout.menu.button.editor;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.item.ItemBuilder;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.feature.layout.data.LayoutData;
import dev.revere.alley.library.menu.Button;
import lombok.AllArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 13/07/2026
 */

@AllArgsConstructor
public class LayoutRenameButton extends Button {
    private static final Map<UUID, LayoutData> PENDING_RENAME = new ConcurrentHashMap<>();

    private final LayoutData layout;

    static {
        AlleyPlugin.getInstance().getServer().getPluginManager().registerEvents(new RenameChatListener(), AlleyPlugin.getInstance());
    }

    @Override
    public ItemStack getButtonItem(Player player) {
        return new ItemBuilder(Material.NAME_TAG)
                .name("&6&lRename Layout")
                .lore(CC.MENU_BAR,
                        "&7Change the display",
                        "&7name of the layout.",
                        "",
                        "&aClick to rename",
                        CC.MENU_BAR)
                .hideMeta().build();
    }

    /** Trigger rename from outside (e.g. LayoutSelectionMenu). */
    public static void triggerRename(Player player, LayoutData layout) {
        PENDING_RENAME.put(player.getUniqueId(), layout);
        player.sendMessage(CC.translate("&6&lLayout Rename"));
        player.sendMessage(CC.translate("&7Current name: &f" + layout.getDisplayName()));
        player.sendMessage(CC.translate("&7Type the new name in chat, or &c'cancel' &7to abort."));
    }

    @Override
    public void clicked(Player player, ClickType clickType) {
        if (clickType != ClickType.LEFT) return;
        triggerRename(player, layout);
        player.closeInventory();
    }

    private static class RenameChatListener implements Listener {
        @EventHandler(priority = EventPriority.LOWEST)
        public void onChat(AsyncPlayerChatEvent event) {
            Player player = event.getPlayer();
            LayoutData layout = PENDING_RENAME.remove(player.getUniqueId());
            if (layout == null) return;

            event.setCancelled(true);
            String msg = event.getMessage().trim();
            if (msg.equalsIgnoreCase("cancel")) {
                player.sendMessage(CC.translate("&cRename cancelled."));
                return;
            }
            if (msg.length() > 32) msg = msg.substring(0, 32);
            layout.setDisplayName(msg);
            player.sendMessage(CC.translate("&aLayout renamed to &e" + msg + "&a!"));
        }
    }
}
