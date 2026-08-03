package dev.revere.alley.feature.queue.menu.button;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.PlayerUtil;
import dev.revere.alley.common.item.ItemBuilder;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.locale.LocaleService;
import dev.revere.alley.core.locale.internal.impl.message.GlobalMessagesLocaleImpl;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.leaderboard.LeaderboardService;
import dev.revere.alley.feature.leaderboard.LeaderboardType;
import dev.revere.alley.feature.leaderboard.data.LeaderboardPlayerData;
import dev.revere.alley.feature.queue.Queue;
import dev.revere.alley.feature.server.ServerService;
import dev.revere.alley.library.menu.Button;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Emmy
 * @project Alley
 * @since 13/03/2025
 */
@AllArgsConstructor
public class UnrankedButton extends Button {
    protected final AlleyPlugin plugin = AlleyPlugin.getInstance();
    private final Queue queue;

    @Override
    public ItemStack getButtonItem(Player player) {
        Kit kit = this.queue.getKit();

        return new ItemBuilder(kit.getIconItemOrDefault())
                .name(kit.getMenuTitle())
                .durability(kit.getDurability())
                .hideMeta()
                .lore(this.getLore(kit, player))
                .hideMeta().build();
    }

    /**
     * Get the lore for the kit.
     * 获取装备包的描述文本。
     *
     * @param kit the kit to get the lore for
     *            要获取描述的装备包
     * @param player the player viewing the button
     *            查看按钮的玩家
     * @return the lore for the kit
     *         装备包的描述文本
     */
    private @NotNull List<String> getLore(Kit kit, Player player) {
        List<String> lore = new ArrayList<>();
        lore.add(CC.MENU_BAR);

        if (!kit.getDescription().isEmpty()) {
            Collections.addAll(lore,
                    "&7" + kit.getDescription(),
                    ""
            );
        }

        Collections.addAll(lore,
                "&6│ &rPlaying: &6" + this.queue.getQueueFightCount(),
                "&6│ &rQueueing: &6" + this.queue.getTotalPlayerCount()
        );

        // Show player's personal win streak
        Profile profile = AlleyPlugin.getInstance().getService(ProfileService.class).getProfile(player.getUniqueId());
        int personalStreak = profile.getProfileData().getUnrankedKitData().getOrDefault(kit.getName(), new dev.revere.alley.core.profile.data.types.ProfileUnrankedKitData()).getWinstreak();
        lore.add("");
        lore.add("&f&lYour Streak: &6" + personalStreak);

        // Show top 3 win streak leaderboard for this kit
        LeaderboardService leaderboardService = AlleyPlugin.getInstance().getService(LeaderboardService.class);
        if (leaderboardService != null) {
            List<LeaderboardPlayerData> entries = leaderboardService.getLeaderboardEntries(kit, LeaderboardType.WIN_STREAK);
            if (!entries.isEmpty()) {
                int count = 0;
                for (LeaderboardPlayerData entry : entries) {
                    if (count >= 3) break;
                    if (entry.getValue() > 0) {
                        lore.add(" &f" + (count + 1) + ". &6" + entry.getName() + " &f- &6" + entry.getValue());
                        count++;
                    }
                }
                if (count == 0) {
                    lore.add(" &fNo streaks yet!");
                }
            }
        }

        lore.add("");
        lore.add("&aClick to play.");
        lore.add(CC.MENU_BAR);

        return lore;
    }

    @Override
    public void clicked(Player player, int slot, ClickType clickType, int hotbarSlot) {
        if (clickType != ClickType.LEFT) return;

        ServerService serverService = AlleyPlugin.getInstance().getService(ServerService.class);
        if (!serverService.isQueueingAllowed()) {
            player.sendMessage(this.plugin.getService(LocaleService.class).getString(GlobalMessagesLocaleImpl.QUEUE_TEMPORARILY_DISABLED));
            player.closeInventory();
            return;
        }

        ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);
        Profile profile = profileService.getProfile(player.getUniqueId());
        this.queue.addPlayer(player, this.queue.isRanked() ? profile.getProfileData().getRankedKitData().get(this.queue.getKit().getName()).getElo() : 0);
        this.playNeutral(player);

        PlayerUtil.reset(player, false, true);
        player.closeInventory();
    }
}
