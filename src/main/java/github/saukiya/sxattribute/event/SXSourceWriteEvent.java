package github.saukiya.sxattribute.event;

import github.saukiya.sxattribute.feature.source.SourceWriteResult;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/** 来源写入、拒绝或删除结果通知事件。 */
@AllArgsConstructor
@Getter
public final class SXSourceWriteEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final LivingEntity entity;
    private final String source;
    private final SourceWriteResult result;

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
