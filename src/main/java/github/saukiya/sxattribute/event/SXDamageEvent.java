package github.saukiya.sxattribute.event;

import github.saukiya.sxattribute.data.eventdata.sub.DamageEventData;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * 这个类用来加载玩家防具物品的数据事件
 * 可以对里面的Items、AttributeData进行修改操作
 *
 * @author Saukiya
 */

public class SXDamageEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    @Getter
    private DamageEventData data;

    public SXDamageEvent(DamageEventData data) {
        this.data = data;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

}
