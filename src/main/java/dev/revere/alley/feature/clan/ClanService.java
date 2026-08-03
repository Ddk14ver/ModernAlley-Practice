package dev.revere.alley.feature.clan;

import dev.revere.alley.bootstrap.lifecycle.Service;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 /**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 26/06/2026
 */

public interface ClanService extends Service {
    List<Clan> getClans();
    Clan getClanByName(String name);
    Clan getClanByPlayer(Player player);
    Clan getClanByPlayer(UUID uuid);
    void createClan(String name, Player leader);
    void disbandClan(Clan clan, Player disbander);
    void renameClan(Clan clan, String newName);
    void addMember(Clan clan, Player player);
    void removeMember(Clan clan, Player player);
    void promoteToOfficer(Clan clan, Player player);
    void demoteFromOfficer(Clan clan, Player player);
    void banPlayer(Clan clan, Player target);
    void unbanPlayer(Clan clan, Player target);
    void saveClan(Clan clan);
    void deleteClan(Clan clan);
    void saveAllClans();
    ClanInvite getInvite(Player player, String clanName);
    void addInvite(Clan clan, Player sender, Player target);
    void removeInvite(Player player, String clanName);
    Map<String, ClanInvite> getPendingInvites(Player player);
    String getChatFormat();
    int getMaxMembers();
}
