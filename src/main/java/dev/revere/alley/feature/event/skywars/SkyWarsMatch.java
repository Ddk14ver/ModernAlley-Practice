package dev.revere.alley.feature.event.skywars;

import dev.revere.alley.common.text.CC;
import dev.revere.alley.feature.arena.Arena;
import dev.revere.alley.feature.arena.internal.types.StandAloneArena;
import dev.revere.alley.feature.kit.Kit;
import dev.revere.alley.feature.match.internal.types.FFAMatch;
import dev.revere.alley.feature.match.model.GameParticipant;
import dev.revere.alley.feature.match.model.internal.MatchGamePlayer;
import dev.revere.alley.feature.queue.Queue;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** A last-player-standing FFA match with dedicated SkyWars spawning, loot and opening protection. */
@Getter
public class SkyWarsMatch extends FFAMatch {
    public static final int PROTECTION_SECONDS = 10;

    private final Kit resourceKit;
    private final Map<UUID, Location> assignedSpawns = new HashMap<>();
    private boolean openingProtectionPending = true;
    private long protectionEndsAt;

    public SkyWarsMatch(Queue queue, Kit kit, Arena arena,
                         List<GameParticipant<MatchGamePlayer>> participants, Kit resourceKit) {
        super(queue, kit, arena, participants);
        this.resourceKit = resourceKit;
    }

    @Override
    public void startMatch() {
        this.assignSpawns();
        SkyWarsLoot.populateAllChests(this.getArena(), this.resourceKit);
        super.startMatch();
    }

    @Override
    public void setupPlayer(Player player) {
        super.setupPlayer(player);
        Location spawn = this.assignedSpawns.get(player.getUniqueId());
        if (spawn != null) {
            player.teleportAsync(spawn);
        }
    }

    @Override
    public void handleRoundStart() {
        super.handleRoundStart();
        this.openingProtectionPending = false;
        this.protectionEndsAt = System.currentTimeMillis() + (PROTECTION_SECONDS * 1_000L);
    }

    @Override
    public void handleDeath(Player player, EntityDamageEvent.DamageCause cause) {
        if (this.isProtectionActive()) {
            player.setHealth(player.getMaxHealth());
            Location spawn = this.assignedSpawns.get(player.getUniqueId());
            if (spawn != null) player.teleport(spawn);
            return;
        }
        super.handleDeath(player, cause);
    }

    @Override
    public void sendPlayerVersusPlayerMessage() {
        this.sendMessage(CC.translate("&7[&bSkyWars&7] &fThe battle begins with &b"
                + this.getParticipants().size() + " &fplayers."));
    }

    public boolean isProtectionActive() {
        // MatchTaskManager switches the state to RUNNING one scheduler tick before
        // it invokes handleRoundStart. Keep players protected during that hand-off.
        return this.openingProtectionPending || this.protectionEndsAt > System.currentTimeMillis();
    }

    public int getProtectionSecondsRemaining() {
        if (this.openingProtectionPending) {
            return PROTECTION_SECONDS;
        }
        long remainingMillis = Math.max(0L, this.protectionEndsAt - System.currentTimeMillis());
        return (int) Math.ceil(remainingMillis / 1_000.0D);
    }

    private void assignSpawns() {
        if (!(this.getArena() instanceof StandAloneArena arena)) {
            throw new IllegalStateException("SkyWars matches require a dedicated standalone arena.");
        }
        List<Location> spawns = new ArrayList<>(arena.getSkyWarsSpawns());
        Collections.shuffle(spawns);

        int participantIndex = 0;
        for (GameParticipant<MatchGamePlayer> participant : this.getParticipants()) {
            for (MatchGamePlayer gamePlayer : participant.getAllPlayers()) {
                this.assignedSpawns.put(gamePlayer.getUuid(),
                        spawns.get(participantIndex % spawns.size()).clone());
                participantIndex++;
            }
        }
    }
}
