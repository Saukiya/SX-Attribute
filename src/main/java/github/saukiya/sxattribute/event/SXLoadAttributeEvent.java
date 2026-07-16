package github.saukiya.sxattribute.event;

import github.saukiya.sxattribute.data.PreLoadItem;
import github.saukiya.sxattribute.data.attribute.SXAttributeData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;

/**
 * 加载实体属性事件
 *
 * @author Saukiya
 */
@AllArgsConstructor
@Getter
@ToString
public class SXLoadAttributeEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private LivingEntity entity;

    private List<PreLoadItem> itemList;

    private SXAttributeData attributeData;

    /**
     * 创建属性加载事件；旧异步参数仅保留调用兼容性，实际标记由当前 Bukkit 线程决定。
     */
    public SXLoadAttributeEvent(LivingEntity entity, List<PreLoadItem> itemList,
                                SXAttributeData attributeData, boolean isAsync) {
        super(EventThreadContext.isAsynchronous());
        this.entity = entity;
        this.itemList = itemList;
        this.attributeData = attributeData;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
