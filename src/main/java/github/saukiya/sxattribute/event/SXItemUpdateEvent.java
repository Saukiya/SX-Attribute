package github.saukiya.sxattribute.event;

import github.saukiya.sxattribute.data.itemdata.IGenerator;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

/**
 * 物品更新事件
 *
 * @author Saukiya
 */
@Getter
public class SXItemUpdateEvent extends SXItemSpawnEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private ItemStack oldItem;

    @Setter
    private boolean cancelled = false;

    public SXItemUpdateEvent(Player player, IGenerator ig, ItemStack item, ItemStack oldItem) {
        super(player, ig, item);
        this.oldItem = oldItem;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

}
