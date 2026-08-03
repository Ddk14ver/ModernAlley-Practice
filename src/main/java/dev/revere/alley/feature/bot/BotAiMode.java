package dev.revere.alley.feature.bot;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum BotAiMode {
    MELEE("Melee", Material.IRON_SWORD, "Pure close-range combat without item tactics."),
    POTPVP("PotPvP", Material.SPLASH_POTION, "Close-range combat with healing potion decisions."),
    BUILDUHC("BuildUHC", Material.LAVA_BUCKET, "Uses melee, rods, bows, lava and golden apples."),
    GOMOKU("Gomoku", Material.ENDER_PEARL, "Uses turn-based five-in-a-row board AI.");

    private final String displayName;
    private final Material icon;
    private final String description;

    public static BotAiMode fromName(String input) {
        if (input == null) return MELEE;
        return Arrays.stream(values())
                .filter(mode -> mode.name().equalsIgnoreCase(input)
                        || mode.displayName.equalsIgnoreCase(input))
                .findFirst()
                .orElse(MELEE);
    }
}
