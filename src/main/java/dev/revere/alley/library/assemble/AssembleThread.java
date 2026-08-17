package dev.revere.alley.library.assemble;

import dev.revere.alley.common.logger.Logger;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.Collections;
import java.util.List;

public class AssembleThread extends Thread {
    private final AssembleServiceImpl assembleServiceImpl;

    /**
     * Assemble Thread.
     * Assemble 线程。
     *
     * @param assembleServiceImpl instance.
     *        AssembleServiceImpl 实例。
     */
    AssembleThread(AssembleServiceImpl assembleServiceImpl) {
        this.assembleServiceImpl = assembleServiceImpl;
        this.start();
    }

    @Override
    @SuppressWarnings("all")
    public void run() {
        while (!this.isInterrupted()) {
            try {
                tick();
                sleep(this.assembleServiceImpl.getTicks() * 50);
            } catch (InterruptedException ignored) {
                this.interrupt();
                return;
            } catch (Exception exception) {
                if (this.isInterrupted() || !this.assembleServiceImpl.getPlugin().isEnabled()) {
                    return;
                }
                Logger.error("There was an error in the Assemble Thread.");
                exception.printStackTrace();
            }
        }
    }

    private void tick() {
        for (Player player : this.assembleServiceImpl.getPlugin().getServer().getOnlinePlayers()) {
            try {
                AssembleBoard board = this.assembleServiceImpl.getBoards().get(player.getUniqueId());
                if (board == null) continue;

                Scoreboard scoreboard = board.getScoreboard();
                Objective objective = board.getObjective();
                if (scoreboard == null || objective == null) continue;

                if (this.assembleServiceImpl.getAdapter() == null) continue;
                String rawTitle = this.assembleServiceImpl.getAdapter().getTitle(player);
                if (rawTitle == null) rawTitle = "";
                String title = ChatColor.translateAlternateColorCodes('&', rawTitle);

                List<String> newLines = this.assembleServiceImpl.getAdapter().getLines(player);
                if (title.isEmpty() && (newLines == null || newLines.isEmpty())) {
                    this.assembleServiceImpl.removeBoard(player);
                    continue;
                }
                if (this.assembleServiceImpl.getBoards().get(player.getUniqueId()) != board) continue;

                if (!objective.getDisplayName().equals(title)) {
                    objective.setDisplayName(title);
                }

                List<AssembleBoardEntry> entries = board.getEntries();
                if (newLines == null || newLines.isEmpty()) {
                    if (entries != null) { entries.forEach(AssembleBoardEntry::remove); entries.clear(); }
                } else {
                    if (newLines.size() > 15) {
                        newLines = newLines.subList(0, 15);
                    }

                    if (!this.assembleServiceImpl.getAssembleStyle().isDescending()) {
                        Collections.reverse(newLines);
                    }

                    if (entries != null && entries.size() > newLines.size()) {
                        for (int i = newLines.size(); i < board.getEntries().size(); i++) {
                            AssembleBoardEntry entry = board.getEntryAtPosition(i);

                            if (entry != null) {
                                entry.remove();
                            }
                        }
                    }

                    int cache = this.assembleServiceImpl.getAssembleStyle().getStartNumber();
                    for (int i = 0; i < newLines.size(); i++) {
                        AssembleBoardEntry entry = board.getEntryAtPosition(i);

                        String rawLine = newLines.get(i);
                        if (rawLine == null) continue;
                        String line = ChatColor.translateAlternateColorCodes('&', rawLine);
                        if (entry == null) {
                            entry = new AssembleBoardEntry(board, line, i);
                        }

                        entry.setText(line);
                        entry.setup();
                        entry.send(
                                this.assembleServiceImpl.getAssembleStyle().isDescending() ? cache-- : cache++
                        );
                    }
                }

                if (this.assembleServiceImpl.getBoards().get(player.getUniqueId()) != board) continue;
                if (player.getScoreboard() != scoreboard && !assembleServiceImpl.isHook()) {
                    player.setScoreboard(scoreboard);
                }
            } catch (Exception exception) {
                exception.printStackTrace();
                throw new AssembleException("There was an error updating " + player.getName() + "'s scoreboard.");
            }
        }
    }
}
