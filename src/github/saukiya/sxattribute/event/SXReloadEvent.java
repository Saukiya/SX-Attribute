package github.saukiya.sxattribute.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * 插件重载事件
 *
 * @author Saukiya
 */
@AllArgsConstructor
@Getter
public class SXReloadEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private CommandSender sender;

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

}
