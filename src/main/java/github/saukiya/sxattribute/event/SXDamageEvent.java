package github.saukiya.sxattribute.event;

import github.saukiya.sxattribute.data.eventdata.sub.DamageData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * 伤害事件
 *
 * @author Saukiya
 */
@AllArgsConstructor
@Getter
public class SXDamageEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private DamageData data;

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

}
