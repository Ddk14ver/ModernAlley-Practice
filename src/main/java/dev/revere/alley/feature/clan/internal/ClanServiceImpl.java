package dev.revere.alley.feature.clan.internal;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.bootstrap.annotation.Service;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.core.database.MongoService;
import dev.revere.alley.core.profile.Profile;
import dev.revere.alley.core.profile.ProfileService;
import dev.revere.alley.feature.clan.Clan;
import dev.revere.alley.feature.clan.ClanInvite;
import dev.revere.alley.feature.clan.ClanService;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Ddk1 ClaudeCode
 * @project Alley
 * @since 26/06/2026
 *
 * Implementation of ClanService with MongoDB persistence.
 * 使用MongoDB持久化的ClanService实现。
 */
@Service(provides = ClanService.class, priority = 240)
public class ClanServiceImpl implements ClanService {
    private final Map<String, Clan> clans = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, ClanInvite>> pendingInvites = new ConcurrentHashMap<>();
    private MongoCollection<Document> collection;

    private static final int MAX_MEMBERS = 15;
    private static final String CHAT_FORMAT = "&8[&6Clan&8] &7{player}&8: &f{message}";

    @Override
    public void setup(dev.revere.alley.bootstrap.AlleyContext context) {
        // No setup needed
    }

    @Override
    public void initialize(dev.revere.alley.bootstrap.AlleyContext context) {
        MongoService mongoService = AlleyPlugin.getInstance().getService(MongoService.class);
        MongoDatabase database = mongoService.getMongoDatabase();
        this.collection = database.getCollection("clans");
        this.loadAllClans();

        // Register listener
        AlleyPlugin.getInstance().getServer().getPluginManager()
                .registerEvents(new dev.revere.alley.feature.clan.listener.ClanListener(), AlleyPlugin.getInstance());
    }

    @Override
    public void shutdown(dev.revere.alley.bootstrap.AlleyContext context) {
        this.saveAllClans();
        this.clans.clear();
        this.pendingInvites.clear();
    }

    // ========================
    // CRUD Operations
    // ========================

    @Override
    public List<Clan> getClans() {
        return new ArrayList<>(this.clans.values());
    }

    @Override
    public Clan getClanByName(String name) {
        return this.clans.values().stream()
                .filter(c -> c.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Clan getClanByPlayer(Player player) {
        return this.getClanByPlayer(player.getUniqueId());
    }

    @Override
    public Clan getClanByPlayer(UUID uuid) {
        for (Clan clan : this.clans.values()) {
            if (clan.getMembers().contains(uuid)) {
                return clan;
            }
        }
        return null;
    }

    @Override
    public void createClan(String name, Player leader) {
        Clan clan = new Clan(name, leader);
        this.clans.put(name.toLowerCase(), clan);

        // Set Profile reference
        Profile profile = AlleyPlugin.getInstance().getService(ProfileService.class)
                .getProfile(leader.getUniqueId());
        if (profile != null) {
            profile.setClan(clan);
        }

        this.saveClan(clan);
    }

    @Override
    public void disbandClan(Clan clan, Player disbander) {
        // Remove clan reference from all member profiles
        ProfileService profileService = AlleyPlugin.getInstance().getService(ProfileService.class);
        for (UUID memberUuid : new ArrayList<>(clan.getMembers())) {
            Profile profile = profileService.getProfile(memberUuid);
            if (profile != null) {
                profile.setClan(null);
            }
        }

        clan.getMembers().clear();
        clan.getOfficers().clear();

        this.deleteClan(clan);
        this.clans.remove(clan.getName().toLowerCase());
    }

    @Override
    public void renameClan(Clan clan, String newName) {
        this.clans.remove(clan.getName().toLowerCase());
        this.deleteClan(clan);
        clan.setName(newName);
        clan.setDisplayName(newName);
        this.clans.put(newName.toLowerCase(), clan);
        this.saveClan(clan);
    }

    @Override
    public void addMember(Clan clan, Player player) {
        clan.addMember(player);

        Profile profile = AlleyPlugin.getInstance().getService(ProfileService.class)
                .getProfile(player.getUniqueId());
        if (profile != null) {
            profile.setClan(clan);
        }

        // Remove any pending invites
        this.removeInvite(player, clan.getName());

        this.saveClan(clan);
    }

    @Override
    public void removeMember(Clan clan, Player player) {
        clan.removeMember(player.getUniqueId());

        Profile profile = AlleyPlugin.getInstance().getService(ProfileService.class)
                .getProfile(player.getUniqueId());
        if (profile != null) {
            profile.setClan(null);
        }

        this.saveClan(clan);
    }

    @Override
    public void promoteToOfficer(Clan clan, Player player) {
        clan.promoteToOfficer(player);
        this.saveClan(clan);
    }

    @Override
    public void demoteFromOfficer(Clan clan, Player player) {
        clan.demoteFromOfficer(player);
        this.saveClan(clan);
    }

    @Override
    public void banPlayer(Clan clan, Player target) {
        if (!clan.getBannedPlayers().contains(target.getUniqueId())) {
            clan.getBannedPlayers().add(target.getUniqueId());
        }
        // Also remove from members if they were one
        if (clan.getMembers().contains(target.getUniqueId())) {
            this.removeMember(clan, target);
        }
        this.saveClan(clan);
    }

    @Override
    public void unbanPlayer(Clan clan, Player target) {
        clan.getBannedPlayers().remove(target.getUniqueId());
        this.saveClan(clan);
    }

    // ========================
    // Invite System
    // ========================

    @Override
    public ClanInvite getInvite(Player player, String clanName) {
        Map<String, ClanInvite> invites = this.pendingInvites.get(player.getUniqueId());
        if (invites == null) return null;
        ClanInvite invite = invites.get(clanName.toLowerCase());
        if (invite != null && invite.isExpired()) {
            invites.remove(clanName.toLowerCase());
            return null;
        }
        return invite;
    }

    @Override
    public void addInvite(Clan clan, Player sender, Player target) {
        Map<String, ClanInvite> invites = this.pendingInvites.computeIfAbsent(
                target.getUniqueId(), k -> new ConcurrentHashMap<>());
        invites.put(clan.getName().toLowerCase(),
                new ClanInvite(clan.getName(), sender.getUniqueId(), target.getUniqueId()));
    }

    @Override
    public void removeInvite(Player player, String clanName) {
        Map<String, ClanInvite> invites = this.pendingInvites.get(player.getUniqueId());
        if (invites != null) {
            invites.remove(clanName.toLowerCase());
        }
    }

    @Override
    public Map<String, ClanInvite> getPendingInvites(Player player) {
        return this.pendingInvites.getOrDefault(player.getUniqueId(), Collections.emptyMap());
    }

    // ========================
    // Settings
    // ========================

    @Override
    public String getChatFormat() {
        return CHAT_FORMAT;
    }

    @Override
    public int getMaxMembers() {
        return MAX_MEMBERS;
    }

    // ========================
    // MongoDB Persistence
    // ========================

    @Override
    public void saveClan(Clan clan) {
        Document doc = clanToDocument(clan);
        this.collection.replaceOne(
                Filters.eq("name", clan.getName().toLowerCase()),
                doc,
                new ReplaceOptions().upsert(true)
        );
    }

    @Override
    public void deleteClan(Clan clan) {
        this.collection.deleteOne(Filters.eq("name", clan.getName().toLowerCase()));
    }

    @Override
    public void saveAllClans() {
        for (Clan clan : this.clans.values()) {
            this.saveClan(clan);
        }
    }

    private void loadAllClans() {
        for (Document doc : this.collection.find()) {
            try {
                Clan clan = documentToClan(doc);
                if (clan != null) {
                    this.clans.put(clan.getName().toLowerCase(), clan);
                }
            } catch (Exception e) {
                AlleyPlugin.getInstance().getLogger()
                        .warning("Failed to load clan from document: " + e.getMessage());
            }
        }
    }

    // ========================
    // Serialization
    // ========================

    private Document clanToDocument(Clan clan) {
        Document doc = new Document();
        doc.put("name", clan.getName().toLowerCase());
        doc.put("displayName", clan.getDisplayName());
        doc.put("description", clan.getDescription());
        doc.put("leader", clan.getLeader().toString());
        doc.put("color", clan.getColor().name());
        doc.put("points", clan.getPoints());
        doc.put("inviteOnly", clan.isInviteOnly());
        doc.put("chatMuted", clan.isChatMuted());
        doc.put("createdAt", clan.getCreatedAt());

        // Members
        List<String> memberStrs = new ArrayList<>();
        for (UUID uuid : clan.getMembers()) {
            memberStrs.add(uuid.toString());
        }
        doc.put("members", memberStrs);

        // Officers
        List<String> officerStrs = new ArrayList<>();
        for (UUID uuid : clan.getOfficers()) {
            officerStrs.add(uuid.toString());
        }
        doc.put("officers", officerStrs);

        // Banned players
        List<String> bannedStrs = new ArrayList<>();
        for (UUID uuid : clan.getBannedPlayers()) {
            bannedStrs.add(uuid.toString());
        }
        doc.put("bannedPlayers", bannedStrs);

        // Home location
        if (clan.getHome() != null) {
            Document homeDoc = new Document();
            homeDoc.put("world", clan.getHome().getWorld().getName());
            homeDoc.put("x", clan.getHome().getX());
            homeDoc.put("y", clan.getHome().getY());
            homeDoc.put("z", clan.getHome().getZ());
            homeDoc.put("yaw", (double) clan.getHome().getYaw());
            homeDoc.put("pitch", (double) clan.getHome().getPitch());
            doc.put("home", homeDoc);
        }

        return doc;
    }

    private Clan documentToClan(Document doc) {
        String name = doc.getString("name");
        String displayName = doc.getString("displayName");
        String description = doc.getString("description");
        String leaderStr = doc.getString("leader");
        UUID leaderUuid = UUID.fromString(leaderStr);

        // Reconstruct clan without needing online player
        Clan clan = new Clan(name, leaderUuid);

        clan.setDisplayName(displayName != null ? displayName : name);

        if (description != null) clan.setDescription(description);

        try {
            String colorStr = doc.getString("color");
            if (colorStr != null) clan.setColor(ChatColor.valueOf(colorStr));
        } catch (IllegalArgumentException ignored) {}

        clan.setPoints(doc.getInteger("points", 0));
        if (doc.containsKey("inviteOnly")) clan.setInviteOnly(doc.getBoolean("inviteOnly", true));
        if (doc.containsKey("chatMuted")) clan.setChatMuted(doc.getBoolean("chatMuted", false));

        // Members
        List<String> memberStrs = doc.getList("members", String.class);
        if (memberStrs != null) {
            clan.getMembers().clear();
            for (String uuidStr : memberStrs) {
                try {
                    clan.getMembers().add(UUID.fromString(uuidStr));
                } catch (IllegalArgumentException ignored) {}
            }
        }

        // Officers
        List<String> officerStrs = doc.getList("officers", String.class);
        if (officerStrs != null) {
            clan.getOfficers().clear();
            for (String uuidStr : officerStrs) {
                try {
                    clan.getOfficers().add(UUID.fromString(uuidStr));
                } catch (IllegalArgumentException ignored) {}
            }
        }

        // Banned players
        List<String> bannedStrs = doc.getList("bannedPlayers", String.class);
        if (bannedStrs != null) {
            clan.getBannedPlayers().clear();
            for (String uuidStr : bannedStrs) {
                try {
                    clan.getBannedPlayers().add(UUID.fromString(uuidStr));
                } catch (IllegalArgumentException ignored) {}
            }
        }

        // Home
        Document homeDoc = doc.get("home", Document.class);
        if (homeDoc != null) {
            try {
                String worldName = homeDoc.getString("world");
                double x = homeDoc.getDouble("x");
                double y = homeDoc.getDouble("y");
                double z = homeDoc.getDouble("z");
                float yaw = homeDoc.getDouble("yaw").floatValue();
                float pitch = homeDoc.getDouble("pitch").floatValue();
                org.bukkit.World world = Bukkit.getWorld(worldName);
                if (world != null) {
                    clan.setHome(new Location(world, x, y, z, yaw, pitch));
                }
            } catch (Exception ignored) {}
        }

        return clan;
    }
}
