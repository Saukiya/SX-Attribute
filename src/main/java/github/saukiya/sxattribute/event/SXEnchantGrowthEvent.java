package github.saukiya.sxattribute.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

/** 附魔经验或等级变化通知事件。 */
@AllArgsConstructor
@Getter
public final class SXEnchantGrowthEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final ItemStack item;
    private final String enchantId;
    private final int oldLevel;
    private final int newLevel;
    private final double gainedExperience;

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
