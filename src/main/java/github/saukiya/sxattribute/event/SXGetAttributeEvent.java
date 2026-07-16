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
public class SXGetAttributeEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    @Getter
    private final LivingEntity entity;

    @Getter
    @Setter
    private SXAttributeData data;

    /**
     * 创建属性读取事件；旧异步参数仅保留调用兼容性，防止调用意图与真实线程不一致。
     */
    public SXGetAttributeEvent(LivingEntity entity, SXAttributeData data, boolean isAsync) {
        super(EventThreadContext.isAsynchronous());
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
