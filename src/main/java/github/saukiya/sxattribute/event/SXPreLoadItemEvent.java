package github.saukiya.sxattribute.event;

import github.saukiya.sxattribute.data.PreLoadItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;

/**
 * 预加载物品事件
 *
 * @author Saukiya
 */
@AllArgsConstructor
@Getter
@ToString
public class SXPreLoadItemEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private LivingEntity entity;

    private List<PreLoadItem> itemList;

    public SXPreLoadItemEvent(LivingEntity entity, List<PreLoadItem> itemList, boolean isAsync) {
        super(isAsync);
        this.entity = entity;
        this.itemList = itemList;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
