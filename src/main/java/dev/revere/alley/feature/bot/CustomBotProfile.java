package dev.revere.alley.feature.bot;

import lombok.Getter;
import lombok.Setter;

import java.util.regex.Pattern;

/** Player-owned Bot Duel configuration persisted inside ProfileData. */
@Getter
@Setter
public final class CustomBotProfile {
    private static final Pattern VALID_NAME = Pattern.compile("^[A-Za-z0-9_]{1,16}$");
    private String name = "CustomBot";
    private String skinName = "";
    private double cps = 10.0D;
    private double maxReach = 3.0D;
    private double swingRange = 4.2D;
    private double minReach = 1.35D;
    private double movementSpeed = 1.0D;
    private double aimSpeed = 22.0D;
    private double aimError = 0.08D;
    private int ping;
    private boolean tryhard = true;
    private boolean wTap = true;
    private boolean strafe = true;
    private boolean bow;
    private boolean rod;
    private boolean lava;
    private int lavaTicks = 12;
    private boolean antiFire = true;
    private double healHealth = 9.0D;

    public static boolean isValidName(String name) {
        return name != null && VALID_NAME.matcher(name).matches();
    }
}
