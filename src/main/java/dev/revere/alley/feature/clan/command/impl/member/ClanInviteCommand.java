package dev.revere.alley.feature.clan.command.impl.member;

import dev.revere.alley.common.text.CC;
import dev.revere.alley.feature.clan.Clan;
import dev.revere.alley.feature.clan.ClanService;
import dev.revere.alley.library.command.BaseCommand;
import dev.revere.alley.library.command.CommandArgs;
import dev.revere.alley.library.command.annotation.CommandData;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 26/06/2026
 */
public class ClanInviteCommand extends BaseCommand {
    @CommandData(name = "clan.invite", usage = "clan invite <player>", description = "Invite a player to your clan.")
    @Override
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        String[] args = command.getArgs();
        if (args.length < 1) { command.sendUsage(); return; }

        ClanService clanService = this.plugin.getService(ClanService.class);
        Clan clan = clanService.getClanByPlayer(player);
        if (clan == null) { player.sendMessage(CC.translate("&cYou are not in a clan.")); return; }
        if (!clan.isLeaderOrOfficer(player)) { player.sendMessage(CC.translate("&cOnly leaders and officers can invite.")); return; }
        if (clan.getMemberCount() >= clanService.getMaxMembers()) {
            player.sendMessage(CC.translate("&cYour clan is full (" + clanService.getMaxMembers() + " members max)."));
            return;
        }

        Player target = this.plugin.getServer().getPlayer(args[0]);
        if (target == null) { player.sendMessage(CC.translate("&cPlayer not found or offline.")); return; }
        if (target.getUniqueId().equals(player.getUniqueId())) { player.sendMessage(CC.translate("&cYou cannot invite yourself.")); return; }
        if (clanService.getClanByPlayer(target) != null) { player.sendMessage(CC.translate("&cThat player is already in a clan.")); return; }
        if (clan.isBanned(target)) { player.sendMessage(CC.translate("&cThat player is banned from your clan.")); return; }

        clanService.addInvite(clan, player, target);

        // Send invite to target
        target.sendMessage(CC.translate("&6&lClan Invite &8| &7You have been invited to join &6" + clan.getColoredName() + "&7!"));
        target.sendMessage(CC.translate("&7Use &e/clan accept " + clan.getName() + " &7to join!"));

        // Clickable invite message
        TextComponent clickable = new TextComponent(CC.translate("&a[Click to Accept]"));
        clickable.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/clan accept " + clan.getName()));
        clickable.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(CC.translate("&7Click to join &6" + clan.getName())).create()));
        target.spigot().sendMessage(clickable);

        player.sendMessage(CC.translate("&aInvited &6" + target.getName() + " &ato join your clan."));
    }
}
