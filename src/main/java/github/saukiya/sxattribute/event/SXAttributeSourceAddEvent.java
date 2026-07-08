package github.saukiya.sxattribute.event;

import github.saukiya.sxattribute.data.attribute.SXAttributeData;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * 添加属性源事件 (可取消)
 * <p>
 * 为实体添加一个命名属性源(非静态源)时触发。其他插件可取消以拦截注入, 或修改 {@link #data} 调整注入值。
 * 静态源({@code createStaticAttributeSource}、内部物品源、抛射物)不触发本事件。
 *
 * @author Ray_Hughes
 */
public class SXAttributeSourceAddEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    @Getter
    private final LivingEntity entity;

    @Getter
    private final String source;

    @Getter
    @Setter
    private SXAttributeData data;

    @Getter
    @Setter
    private boolean cancelled;

    public SXAttributeSourceAddEvent(LivingEntity entity, String source, SXAttributeData data) {
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
