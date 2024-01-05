package github.saukiya.sxattribute.event;

import github.saukiya.sxattribute.data.attribute.sub.attack.AttackElement;
import github.saukiya.sxattribute.data.eventdata.sub.DamageData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@AllArgsConstructor
@Getter
public class SXElementDamageEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private DamageData data;

    private AttackElement.ElementData elementData;

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

}
