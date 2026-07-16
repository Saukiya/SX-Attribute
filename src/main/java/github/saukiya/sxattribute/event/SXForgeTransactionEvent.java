package github.saukiya.sxattribute.event;

import github.saukiya.sxattribute.feature.equipment.core.FeatureResult;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

/** 一次锻造事务完成后的审计事件。 */
@AllArgsConstructor
@Getter
public final class SXForgeTransactionEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final String featureId;
    private final ItemStack item;
    private final FeatureResult result;

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
