package dev.revere.alley.feature.leaderboard.hologram;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.common.text.CC;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.kit.KitService;
import dev.revere.alley.feature.leaderboard.LeaderboardService;
import dev.revere.alley.feature.leaderboard.LeaderboardType;
import dev.revere.alley.feature.leaderboard.data.LeaderboardPlayerData;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

import java.util.*;

/**
 * @author Alley
 * @project Alley
 * @since 02/07/2025
 *
 * Leaderboard-specific hologram that displays top players for a kit + leaderboard type.
 * 显示特定套件+排行榜类型的顶级玩家的排行榜专用全息图。
 */
@Getter
@Setter
public class LeaderboardHologram extends Hologram {
    private String kitName;               // Kit name (null = global across all kits)
    private LeaderboardType leaderboardType;
    private boolean rotatingKits;         // Whether to rotate through all kits

    /**
     * Creates a leaderboard hologram at a specific location.
     */
    public LeaderboardHologram(String name, Location baseLocation, String kitName, LeaderboardType type) {
        super(name, baseLocation);
        this.kitName = kitName;
        this.leaderboardType = type;
        this.rotatingKits = false;
        this.showStat = 10;
    }

    /**
     * Creates a rotating-kits hologram.
     */
    public LeaderboardHologram(String name, Location baseLocation, LeaderboardType type) {
        super(name, baseLocation);
        this.kitName = null;
        this.leaderboardType = type;
        this.rotatingKits = true;
        this.showStat = 10;
    }

    /**
     * Creates a hologram from config (loaded later).
     */
    public LeaderboardHologram(String name) {
        super(name);
        this.leaderboardType = LeaderboardType.RANKED;
    }

    // ========================
    // Content
    // ========================

    @Override
    public List<String> getTextLines() {
        List<String> lines = new ArrayList<>();

        KitService kitService = AlleyPlugin.getInstance().getService(KitService.class);
        LeaderboardService lbService = AlleyPlugin.getInstance().getService(LeaderboardService.class);

        Kit kit = this.kitName != null ? kitService.getKit(this.kitName) : null;

        // Header
        String typeName = getTypeDisplayName();
        lines.add(CC.translate("&6&l" + typeName + " Leaderboard"));

        if (kit != null) {
            lines.add(CC.translate("&7" + kit.getDisplayName()));
        } else if (!this.rotatingKits) {
            lines.add(CC.translate("&7Global Rankings"));
        }
        lines.add(CC.translate("&8&m------------------"));

        // Get data — aggregate across all kits when kitName is null
        List<LeaderboardPlayerData> entries;
        if (kit != null) {
            entries = lbService.getLeaderboardEntries(kit, this.leaderboardType);
        } else {
            entries = buildAggregatedLeaderboard(lbService, kitService);
        }

        int count = Math.min(entries.size(), this.showStat);
        for (int i = 0; i < count; i++) {
            LeaderboardPlayerData entry = entries.get(i);
            String prefix = getPlacementPrefix(i + 1);
            String label = getStatLabel();
            lines.add(CC.translate(prefix + " &f" + entry.getName() + " &7- " + label + ": &e" + entry.getValue()));
        }

        // Fill empty slots
        for (int i = count; i < this.showStat; i++) {
            String prefix = getPlacementPrefix(i + 1);
            lines.add(CC.translate(prefix + " &7---"));
        }

        lines.add(CC.translate("&8&m------------------"));
        return lines;
    }

    /**
     * Aggregates leaderboard entries across all enabled kits.
     * Sums values per player UUID and returns sorted top-N.
     */
    private List<LeaderboardPlayerData> buildAggregatedLeaderboard(LeaderboardService lbService, KitService kitService) {
        Map<UUID, LeaderboardPlayerData> aggregated = new LinkedHashMap<>();

        for (Kit k : kitService.getKits()) {
            if (!k.isEnabled()) continue;

            List<LeaderboardPlayerData> kitEntries = lbService.getLeaderboardEntries(k, this.leaderboardType);
            for (LeaderboardPlayerData entry : kitEntries) {
                UUID uuid = entry.getUuid();
                if (aggregated.containsKey(uuid)) {
                    aggregated.get(uuid).setValue(aggregated.get(uuid).getValue() + entry.getValue());
                } else {
                    aggregated.put(uuid, new LeaderboardPlayerData(entry.getName(), uuid, null, entry.getValue()));
                }
            }
        }

        List<LeaderboardPlayerData> sorted = new ArrayList<>(aggregated.values());
        sorted.sort(Comparator.comparingInt(LeaderboardPlayerData::getValue).reversed());

        return sorted;
    }

    @Override
    public void updateContent() {
        if (!this.enabled || this.baseLocation == null) return;

        List<String> textLines = getTextLines();
        updateSmartly(textLines);
    }

    // ========================
    // Helpers
    // ========================

    private String getPlacementPrefix(int placement) {
        return switch (placement) {
            case 1 -> "&6&l#1";
            case 2 -> "&7&l#2";
            case 3 -> "&c&l#3";
            default -> "&e#" + placement;
        };
    }

    private String getStatLabel() {
        return switch (this.leaderboardType) {
            case RANKED -> "Elo";
            case UNRANKED, UNRANKED_MONTHLY -> "Wins";
            case WIN_STREAK -> "Streak";
            case FFA -> "Kills";
            case TOURNAMENT -> "Wins";
        };
    }

    private String getTypeDisplayName() {
        return this.leaderboardType.getName();
    }
}
