package github.saukiya.sxattribute.event;

import github.saukiya.sxattribute.feature.attribute.AttributeExecutionContext;
import lombok.Getter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Map;

/** 配置化属性动作执行前后事件；PRE 阶段可取消动作。 */
@Getter
public final class SXAttributeActionEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final String attributeId;
    private final Phase phase;
    private final Map<String, Object> action;
    private final AttributeExecutionContext context;
    private boolean cancelled;

    public SXAttributeActionEvent(String attributeId, Phase phase, Map<String, Object> action, AttributeExecutionContext context) {
        this.attributeId = attributeId;
        this.phase = phase;
        this.action = action;
        this.context = context;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public enum Phase {PRE, POST}
}
