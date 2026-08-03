package dev.revere.alley.library.assemble;

import dev.revere.alley.library.assemble.events.AssembleBoardCreatedEvent;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class AssembleBoard {
    private final AssembleServiceImpl assembleServiceImpl;

    private final List<AssembleBoardEntry> entries;
    private final List<String> identifiers;

    private final UUID uuid;

    /**
     * Assemble Board.
     * Assemble 记分板。
     *
     * @param player   that the board belongs to.
     *        记分板所属的玩家。
     * @param assembleServiceImpl instance.
     *        AssembleServiceImpl 实例。
     */
    public AssembleBoard(Player player, AssembleServiceImpl assembleServiceImpl) {
        this.assembleServiceImpl = assembleServiceImpl;
        this.entries = new ArrayList<>();
        this.identifiers = new ArrayList<>();
        this.uuid = player.getUniqueId();
        this.setup(player);
    }

    /**
     * Get's a player's bukkit scoreboard.
     * 获取玩家的 Bukkit 记分板。
     *
     * @return either existing scoreboard or new scoreboard.
     *         现有的记分板或新的记分板。
     */
    public Scoreboard getScoreboard() {
        Player player = Bukkit.getPlayer(getUuid());
        if (this.assembleServiceImpl.isHook() || player.getScoreboard() != Bukkit.getScoreboardManager().getMainScoreboard()) {
            return player.getScoreboard();
        }

        return Bukkit.getScoreboardManager().getNewScoreboard();
    }

    /**
     * Get's the player's scoreboard objective.
     * 获取玩家的记分板目标(objective)。
     *
     * @return either existing objecting or new objective.
     *         现有的目标或新的目标。
     */
    public Objective getObjective() {
        Scoreboard scoreboard = this.getScoreboard();
        if (scoreboard.getObjective("Assemble") == null) {
            Objective objective = scoreboard.registerNewObjective("Assemble", "dummy");
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
            objective.setDisplayName(getAssembleServiceImpl().getAdapter().getTitle(Bukkit.getPlayer(getUuid())));
            return objective;
        }

        return scoreboard.getObjective("Assemble");
    }

    /**
     * Setup the board for a player.
     * 为玩家设置记分板。
     *
     * @param player who's board to setup.
     *        要设置记分板的玩家。
     */
    private void setup(Player player) {
        Scoreboard scoreboard = getScoreboard();
        player.setScoreboard(scoreboard);
        this.getObjective();

        if (this.assembleServiceImpl.isCallEvents()) {
            AssembleBoardCreatedEvent createdEvent = new AssembleBoardCreatedEvent(this);
            Bukkit.getPluginManager().callEvent(createdEvent);
        }
    }

    /**
     * Get the board entry at a specific position.
     * 获取指定位置上的记分板条目。
     *
     * @param pos to find entry.
     *        要查找条目的位置。
     * @return entry if it isn't out of range.
     *         如果未超出范围则返回条目。
     */
    public AssembleBoardEntry getEntryAtPosition(int pos) {
        return pos >= this.entries.size() ? null : this.entries.get(pos);
    }

    /**
     * Get the unique identifier for position in scoreboard.
     * 获取记分板中位置的唯一标识符。
     *
     * @param position for identifier.
     *        标识符对应的位置。
     * @return unique identifier.
     *         唯一标识符。
     */
    public String getUniqueIdentifier(int position) {
        String identifier = this.getRandomChatColor(position) + ChatColor.WHITE;

        while (this.identifiers.contains(identifier)) {
            identifier = identifier + this.getRandomChatColor(position) + ChatColor.WHITE;
        }

        if (identifier.length() > 16) {
            return this.getUniqueIdentifier(position);
        }

        this.identifiers.add(identifier);

        return identifier;
    }

    /**
     * Gets a ChatColor based off the position in the collection.
     * 根据集合中的位置获取对应的 ChatColor。
     *
     * @param position of entry.
     *        条目的位置。
     * @return ChatColor adjacent to position.
     *         与位置对应的 ChatColor。
     */
    private String getRandomChatColor(int position) {
        return this.assembleServiceImpl.getChatColorCache()[position].toString();
    }
}
