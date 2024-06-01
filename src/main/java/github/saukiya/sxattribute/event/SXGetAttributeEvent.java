package github.saukiya.sxattribute.event;

import github.saukiya.sxattribute.data.attribute.SXAttributeData;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * 获取实体数据事件
 *
 * @author Saukiya
 */
@Getter
public class SXGetAttributeEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final LivingEntity entity;

    @Setter
    private SXAttributeData data;

    public SXGetAttributeEvent(LivingEntity entity, SXAttributeData data) {
        this.entity = entity;
        this.data = data;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

}
