package dev.revere.alley.library.assemble.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@Setter
public class AssembleBoardCreateEvent extends Event implements Cancellable {

    @Getter
    public static HandlerList handlerList = new HandlerList();

    private Player player;
    private boolean cancelled;

    /**
     * Assemble Board Create Event.
     * Assemble 记分板创建事件。
     *
     * @param player that the board is being created for.
     *        正在为其创建记分板的玩家。
     */
    public AssembleBoardCreateEvent(Player player) {
        this.player = player;
        this.cancelled = false;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
