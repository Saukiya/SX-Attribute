package github.saukiya.sxattribute.listener;

import github.saukiya.sxattribute.util.Message;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * @author Saukiya
 */
public class OnInventoryCloseListener implements Listener {

    @EventHandler
    void onInventoryCloseSellEvent(InventoryCloseEvent event) {
        if (event.getInventory().getName().equals(Message.getMsg(Message.INVENTORY__SELL__NAME))) {
            Player player = (Player) event.getView().getPlayer();
            Inventory inv = event.getInventory();
            for (int i = 0; i < 18; i++) {
                ItemStack item = inv.getItem(i);
                if (item != null && !item.getType().equals(Material.AIR)) {
                    player.getInventory().addItem(item);
                }
            }
        }
    }

    @EventHandler
    void onInventoryCloseRepairEvent(InventoryCloseEvent event) {
        if (event.getInventory().getName().equals(Message.getMsg(Message.INVENTORY__REPAIR__NAME))) {
            Player player = (Player) event.getView().getPlayer();
            Inventory inv = event.getInventory();
            ItemStack item = inv.getItem(20);
            if (item != null && !item.getType().equals(Material.AIR)) {
                player.getInventory().addItem(item);
            }
        }

    }

}
