package dev.revere.alley.feature.clan.listener;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.core.profile.enums.ChatChannel;
import dev.revere.alley.feature.clan.Clan;
import dev.revere.alley.feature.clan.ClanService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 /**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 26/06/2026
 *
 * Listener for clan-related events: chat routing, quit handling.
 * 公会相关事件监听器：聊天路由、退出处理。
 */
public class ClanListener implements Listener {

    /**
     * Routes chat to clan channel when player has CLAN chat mode enabled.
     * 当玩家启用了 CLAN 聊天模式时，将聊天路由到公会频道。
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);
        Profile profile = profileService.getProfile(player.getUniqueId());
        if (profile == null) return;

        // Check if chat channel is set to CLAN
        String channel = profile.getProfileData().getSettingData().getChatChannel();
        if (channel == null) return;
        if (!channel.equalsIgnoreCase(ChatChannel.CLAN.toString())) return;

        ClanService clanService = AlleyPlugin.getInstance().getService(ClanService.class);
        Clan clan = clanService.getClanByPlayer(player);

        if (clan == null) {
            // Not in a clan, reset to global
            player.sendMessage(CC.translate("&cYou are not in a clan. Chat channel reset to Global."));
            profile.getProfileData().getSettingData().setChatChannel(ChatChannel.GLOBAL.toString());
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);

        if (clan.isChatMuted() && !clan.isLeaderOrOfficer(player)) {
            player.sendMessage(CC.translate("&cClan chat is currently muted."));
            return;
        }

        String formatted = clanService.getChatFormat()
                .replace("{player}", player.getName())
                .replace("{message}", event.getMessage());
        clan.broadcast(CC.translate(formatted));
    }

    /**
     * Handles player quit - cleans up invites and notifies clan members.
     * 处理玩家退出 - 清理邀请并通知公会成员。
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        ClanService clanService = AlleyPlugin.getInstance().getService(ClanService.class);

        // Clean up pending invites sent to this player
        clanService.getPendingInvites(player).clear();

        // Notify clan members (clan persists even when leader quits)
        Clan clan = clanService.getClanByPlayer(player);
        if (clan != null) {
            clan.broadcast(CC.translate("&7" + player.getName() + " &7went offline."));
        }
    }
}
