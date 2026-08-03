package dev.revere.alley.core.profile.data.command.ranked.impl;

import dev.revere.alley.common.PlayerUtil;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.common.time.TimeUtil;
import dev.revere.alley.core.locale.internal.impl.message.GlobalMessagesLocaleImpl;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Emmy
 * @project Alley
 * @since 13/03/2025
 */
public class RankedBanCommand extends BaseCommand {
    @CommandData(
            name = "ranked.ban",
            isAdminOnly = true,
            usage = "ranked ban <player> <duration> [reason...]",
            description = "Ban a player from ranked matches."
    )
    @Override
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        String[] args = command.getArgs();

        if (args.length < 2) {
            player.sendMessage(CC.translate("&cUsage: /ranked ban <player> <duration> [reason...]"));
            player.sendMessage(CC.translate("&cDuration examples: 7d, 24h, 30m, 1w"));
            return;
        }

        String targetName = args[0];
        OfflinePlayer target = PlayerUtil.getOfflinePlayerByName(targetName);
        if (target == null) {
            player.sendMessage(this.getString(GlobalMessagesLocaleImpl.ERROR_INVALID_PLAYER));
            return;
        }

        Profile profile = this.plugin.getService(ProfileService.class).getProfile(target.getUniqueId());
        if (profile == null) {
            player.sendMessage(this.getString(GlobalMessagesLocaleImpl.ERROR_INVALID_PLAYER));
            return;
        }

        if (profile.getProfileData().isRankedBanned()) {
            player.sendMessage(this.getString(GlobalMessagesLocaleImpl.RANKED_PLAYER_ALREADY_BANNED)
                    .replace("{name-color}", String.valueOf(profile.getNameColor()))
                    .replace("{player}", target.getName())
            );
            return;
        }

        // Parse duration
        String durationArg = args[1];
        long durationMs = TimeUtil.parseTime(durationArg);
        if (durationMs <= 0) {
            player.sendMessage(CC.translate("&cInvalid duration: &f" + durationArg));
            player.sendMessage(CC.translate("&cExamples: 7d, 24h, 30m, 1w"));
            return;
        }

        // Parse optional reason
        String reason = "N/A";
        if (args.length >= 3) {
            reason = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
        }

        // Generate ban ID
        String banId = "#" + Integer.toHexString(ThreadLocalRandom.current().nextInt(0x1000, 0xFFFF)).toUpperCase();

        // Apply ban
        profile.getProfileData().setRankedBanned(true);
        profile.getProfileData().setRankedBanExpiry(System.currentTimeMillis() + durationMs);
        profile.getProfileData().setRankedBanReason(reason);
        profile.getProfileData().setRankedBanId(banId);

        String durationFormatted = TimeUtil.millisToRoundedTime(durationMs);

        // Broadcast
        if (this.getBoolean(GlobalMessagesLocaleImpl.RANKED_PLAYER_BAN_BROADCAST_BOOLEAN)) {
            List<String> message = this.getStringList(GlobalMessagesLocaleImpl.RANKED_PLAYER_BAN_BROADCAST);
            for (String line : message) {
                this.plugin.getServer().broadcastMessage(CC.translate(line
                        .replace("{name-color}", String.valueOf(profile.getNameColor()))
                        .replace("{player}", target.getName())
                        .replace("{reason}", reason)
                        .replace("{ban-id}", banId)
                        .replace("{duration}", durationFormatted)
                ));
            }
        }

        // Notify target
        if (this.getBoolean(GlobalMessagesLocaleImpl.RANKED_BAN_MESSAGE_NOTICE_BOOLEAN)) {
            if (target.isOnline()) {
                Player targetPlayer = (Player) target;
                List<String> message = this.getStringList(GlobalMessagesLocaleImpl.RANKED_BAN_MESSAGE_NOTICE);
                for (String line : message) {
                    targetPlayer.sendMessage(CC.translate(line
                            .replace("{name-color}", String.valueOf(profile.getNameColor()))
                            .replace("{player}", target.getName())
                            .replace("{reason}", reason)
                            .replace("{ban-id}", banId)
                            .replace("{duration}", durationFormatted)
                    ));
                }
            }
        }
    }
}
