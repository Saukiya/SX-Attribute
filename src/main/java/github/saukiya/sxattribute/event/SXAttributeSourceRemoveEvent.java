package github.saukiya.sxattribute.event;

import github.saukiya.sxattribute.data.attribute.SXAttributeData;
import lombok.Getter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * 移除属性源事件 (仅通知, 不可取消)
 * <p>
 * 为实体移除一个命名属性源(非静态源)时触发, 携带被移除的 {@link #data} 供监听/调试。
 *
 * @author Ray_Hughes
 */
public class SXAttributeSourceRemoveEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    @Getter
    private final LivingEntity entity;

    @Getter
    private final String source;

    @Getter
    private final SXAttributeData data;

    public SXAttributeSourceRemoveEvent(LivingEntity entity, String source, SXAttributeData data) {
        this.entity = entity;
        this.source = source;
        this.data = data;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
