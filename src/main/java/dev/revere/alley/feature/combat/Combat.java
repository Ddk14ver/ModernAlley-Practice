package dev.revere.alley.feature.combat;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * @author Emmy
 * @project Alley
 * @since 07/03/2025
 */
@Getter
@Setter
public class Combat {
    private UUID attackerUUID;
    private long attackTimestamp;
    private long expirationTime;

    /**
     * Constructor for the Combat class.
     *
     * @param attackerUUID    The UUID of the attacker.
     * @param attackTimestamp The timestamp of the attack.
     * @param expirationTime  The expiration time of the combat.
     */
    public Combat(UUID attackerUUID, long attackTimestamp, long expirationTime) {
        this.attackerUUID = attackerUUID;
        this.attackTimestamp = attackTimestamp;
        this.expirationTime = expirationTime;
    }
}