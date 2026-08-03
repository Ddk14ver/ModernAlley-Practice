package dev.revere.alley.library.assemble.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@Setter
public class AssembleBoardDestroyEvent extends Event implements Cancellable {

    @Getter
    public static HandlerList handlerList = new HandlerList();

    private Player player;
    private boolean cancelled;

    /**
     * Assemble Board Destroy Event.
     * Assemble 记分板销毁事件。
     *
     * @param player who's board got destroyed.
     *        其记分板被销毁的玩家。
     */
    public AssembleBoardDestroyEvent(Player player) {
        this.player = player;
        this.cancelled = false;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
