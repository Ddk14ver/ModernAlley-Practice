package dev.revere.alley.feature.clan.menu;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.item.ItemBuilder;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.feature.clan.Clan;
import dev.revere.alley.feature.clan.ClanService;
import dev.revere.alley.library.menu.Button;
import dev.revere.alley.library.menu.pagination.PaginatedMenu;
import lombok.AllArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 26/06/2026
 *
 * Admin clan management GUI - lists all clans with pagination.
 * 管理员公会管理 GUI - 通过分页列出所有公会。
 */
public class ClanManagementMenu extends PaginatedMenu {

    @Override
    public String getPrePaginatedTitle(Player player) {
        return "&6&lClan Manager";
    }

    @Override
    public int getMaxItemsPerPage() {
        return 28;
    }

    @Override
    public int getSize() {
        return 9 * 6;
    }

    @Override
    public Map<Integer, Button> getGlobalButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        this.addGlassHeader(buttons, Material.BLACK_STAINED_GLASS_PANE);
        buttons.put(49, new CloseMenuButton());
        return buttons;
    }

    @Override
    public Map<Integer, Button> getAllPagesButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        List<Clan> clans = this.plugin.getService(ClanService.class).getClans();
        if (clans.isEmpty()) {
            buttons.put(22, new EmptyInfoButton());
            return buttons;
        }

        int index = 0;
        for (Clan clan : clans) {
            buttons.put(index++, new ClanDisplayButton(clan));
        }

        return buttons;
    }

    @AllArgsConstructor
    private static class ClanDisplayButton extends Button {
        private final Clan clan;

        @Override
        public ItemStack getButtonItem(Player player) {
            List<String> lore = new ArrayList<>();
            lore.add(CC.MENU_BAR);
            lore.add("&7Name: " + this.clan.getColoredName());
            lore.add("&7Leader: &f" + getLeaderName());
            lore.add("&7Members: &f" + this.clan.getOnlineCount() + "&7/&f" + this.clan.getMemberCount() + " &7online");
            lore.add("&7Officers: &f" + this.clan.getOfficers().size());
            lore.add("&7Points: &f" + this.clan.getPoints());
            lore.add("&7Home: " + (this.clan.getHome() != null ? "&aSet" : "&cNot Set"));
            lore.add("&7Invite Only: " + (this.clan.isInviteOnly() ? "&aYes" : "&cNo"));
            lore.add("");
            lore.add("&7Description: &f" + this.clan.getDescription());
            lore.add("");
            lore.add("&eLeft-Click &7to teleport to clan home.");
            lore.add("&eRight-Click &7to disband this clan.");
            lore.add(CC.MENU_BAR);

            return new ItemBuilder(Material.DIAMOND_HELMET)
                    .name(this.clan.getColoredName())
                    .lore(lore)
                    .glow(this.clan.getOnlineCount() > 0)
                    .hideMeta()
                    .build();
        }

        private String getLeaderName() {
            return AlleyPlugin.getInstance().getServer()
                    .getOfflinePlayer(this.clan.getLeader()).getName();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType == ClickType.LEFT) {
                if (this.clan.getHome() != null) {
                    player.teleport(this.clan.getHome());
                    player.sendMessage(CC.translate("&aTeleported to clan &6" + this.clan.getName() + " &ahome."));
                    this.playSuccess(player);
                } else {
                    player.sendMessage(CC.translate("&cThis clan has no home set."));
                    this.playFail(player);
                }
            } else if (clickType == ClickType.RIGHT) {
                ClanService clanService = AlleyPlugin.getInstance().getService(ClanService.class);
                player.closeInventory();
                player.sendMessage(CC.translate("&cUse &e/clan disband " + this.clan.getName() + " &cto disband this clan."));
                this.playNeutral(player);
            }
        }
    }

    private static class EmptyInfoButton extends Button {
        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.BARRIER)
                    .name("&c&lNo Clans Found")
                    .lore(CC.MENU_BAR, "&7No clans have been created yet.", CC.MENU_BAR)
                    .hideMeta()
                    .build();
        }
    }

    private static class CloseMenuButton extends Button {
        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.BARRIER)
                    .name("&c&lClose Menu")
                    .lore(CC.MENU_BAR, "&7Click to close.", CC.MENU_BAR)
                    .hideMeta()
                    .build();
        }

        @Override
        public void clicked(Player player, ClickType clickType) {
            if (clickType == ClickType.LEFT) player.closeInventory();
        }
    }
}
