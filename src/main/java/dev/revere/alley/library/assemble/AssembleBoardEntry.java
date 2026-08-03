package dev.revere.alley.library.assemble;

import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

@Setter
public class AssembleBoardEntry {
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();

    private final AssembleBoard board;
    private final String identifier;
    private String text;
    private Team team;

    /**
     * Assemble Board Entry
     * Assemble 记分板条目。
     *
     * @param board    that entry belongs to.
     *        条目所属的记分板。
     * @param text     of entry.
     *        条目的文本。
     * @param position of entry.
     *        条目的位置。
     */
    public AssembleBoardEntry(AssembleBoard board, String text, int position) {
        this.board = board;
        this.identifier = this.board.getUniqueIdentifier(position);
        this.text = text;

        this.setup();
    }

    /**
     * Setup Board Entry.
     * 设置记分板条目。
     */
    public void setup() {
        final Scoreboard scoreboard = this.board.getScoreboard();
        if (scoreboard == null) return;

        String teamName = this.identifier;
        if (teamName.length() > 16) {
            teamName = teamName.substring(0, 16);
        }

        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
        }

        if (!team.getEntries().contains(this.identifier)) {
            team.addEntry(this.identifier);
        }

        if (!this.board.getEntries().contains(this)) {
            this.board.getEntries().add(this);
        }

        this.team = team;
    }

    /**
     * Send the board entry update to the player.
     * 将记分板条目更新发送给玩家。
     *
     * @param position of entry.
     *        条目的位置。
     */
    public void send(int position) {
        this.team.prefix(LEGACY_SERIALIZER.deserialize(this.text));
        this.team.suffix(Component.empty());

        this.board.getObjective().getScore(this.identifier).setScore(position);
        // Hide the red score numbers (Paper 1.20.3+ API)
        try {
            this.board.getObjective().numberFormat(
                    io.papermc.paper.scoreboard.numbers.NumberFormat.blank());
        } catch (Exception ignored) {}
    }

    public void remove() {
        this.board.getIdentifiers().remove(this.identifier);
        this.board.getScoreboard().resetScores(this.identifier);
    }
}
