package dev.revere.alley.feature.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;

@Getter
@RequiredArgsConstructor
public enum EventType {
    SUMO("Sumo", "Knock opponents off the platform.", Material.LEAD, null),
    BRACKETS("Brackets", "Advance through a series of one-on-one fights.", Material.IRON_SWORD, EventMode.BRACKETS),
    LMS("LMS", "Fight until only one player remains.", Material.TNT, EventMode.LAST_MAN_STANDING),
    SKYWARS("SkyWars", "Use the selected kit and arena to be the last player alive.", Material.GRASS_BLOCK, EventMode.LAST_MAN_STANDING);

    private final String displayName;
    private final String description;
    private final Material icon;
    private final EventMode defaultMode;
}
