package github.saukiya.sxattribute.listener;

import github.saukiya.sxattribute.util.Config;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;

public class ListenerItemSpawn implements Listener {

    @EventHandler
    void onItemSpawnEvent(ItemSpawnEvent event) {
        if (event.isCancelled() || !Config.isItemDisplayName() || event.getEntity().isCustomNameVisible()) return;
        Item item = event.getEntity();
        ItemStack itemStack = item.getItemStack();
        if (!event.isCancelled() && itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName() && !item.hasMetadata("SX|NoVisible")) {
            item.setCustomNameVisible(true);
            if (itemStack.getAmount() > 1) {
                item.setCustomName(itemStack.getItemMeta().getDisplayName() + " §b*" + itemStack.getAmount());
            } else {
                item.setCustomName(itemStack.getItemMeta().getDisplayName());
            }
        }
    }

    @EventHandler
    void onItemMergeEvent(ItemMergeEvent event) {
        if (!Config.isItemDisplayName() || event.isCancelled()) return;
        Item item = event.getTarget();
        Item oldItem = event.getEntity();
        if (item.isCustomNameVisible() && !item.hasMetadata("SX|NoVisible")) {
            item.setCustomName(item.getItemStack().getItemMeta().getDisplayName() + " §b*" + (item.getItemStack().getAmount() + oldItem.getItemStack().getAmount()));
        }
    }

}
