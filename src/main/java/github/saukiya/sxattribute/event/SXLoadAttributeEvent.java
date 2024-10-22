package github.saukiya.sxattribute.event;

import github.saukiya.sxattribute.data.PreLoadItem;
import github.saukiya.sxattribute.data.attribute.SXAttributeData;
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
@Getter
@ToString
public class SXLoadAttributeEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private LivingEntity entity;

    private List<PreLoadItem> itemList;

    private SXAttributeData attributeData;

    public SXLoadAttributeEvent(LivingEntity entity, List<PreLoadItem> itemList, SXAttributeData attributeData) {
        this(entity, itemList, attributeData, false);
    }

    public SXLoadAttributeEvent(LivingEntity entity, List<PreLoadItem> itemList, SXAttributeData attributeData, boolean isAsync) {
        super(isAsync);
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
