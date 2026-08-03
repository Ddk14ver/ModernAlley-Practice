package dev.revere.alley.library.assemble.events;

import dev.revere.alley.library.assemble.AssembleBoard;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@Setter
public class AssembleBoardCreatedEvent extends Event {

    @Getter
    public static HandlerList handlerList = new HandlerList();

    private boolean cancelled;
    private final AssembleBoard board;

    /**
     * Assemble Board Created Event.
     * Assemble 记分板已创建事件。
     *
     * @param board of player.
     *        玩家的记分板。
     */
    public AssembleBoardCreatedEvent(AssembleBoard board) {
        this.cancelled = false;
        this.board = board;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
