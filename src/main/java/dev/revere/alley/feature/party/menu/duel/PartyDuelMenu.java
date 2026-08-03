package dev.revere.alley.feature.party.menu.duel;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.library.menu.Button;
import dev.revere.alley.library.menu.pagination.PaginatedMenu;
import dev.revere.alley.feature.party.PartyService;
import dev.revere.alley.feature.party.menu.duel.button.DuelOtherPartyButton;
import org.bukkit.entity.Player;
import org.bukkit.Material;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * 队伍对决菜单，显示所有可挑战的队伍列表。
 * @author Emmy
 * @project Alley
 * @date 08/10/2024 - 21:01
 */
public class PartyDuelMenu extends PaginatedMenu {
    @Override
    public String getPrePaginatedTitle(Player player) {
        return "&6&lDuel other parties";
    }

    @Override
    public Map<Integer, Button> getGlobalButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        this.addGlassHeader(buttons, Material.BLACK_STAINED_GLASS_PANE);

        return buttons;
    }

    @Override
    public Map<Integer, Button> getAllPagesButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        AlleyPlugin.getInstance().getService(PartyService.class).getParties().stream()
                .sorted(Comparator.comparing(party -> party.getLeader().getName()))
                //.filter(party -> !party.getLeader().equals(player))
                .sorted(Comparator.comparingInt(party -> party.getMembers().size()))
                .forEach(party -> buttons.put(buttons.size(), new DuelOtherPartyButton(party)))
        ;

        return buttons;
    }

    @Override
    public int getSize() {
        return 9 * 5;
    }
}