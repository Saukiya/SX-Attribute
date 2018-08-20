package github.saukiya.sxattribute.inventory;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.util.Message;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * @author Saukiya
 */
public class DisplaySlotInventory {

    private final SXAttribute plugin;

    public DisplaySlotInventory(SXAttribute plugin) {
        this.plugin = plugin;
    }

    public void openDisplaySlotInventory(Player player) {
        Inventory inv = Bukkit.createInventory(null, 45, Message.getMsg(Message.INVENTORY__DISPLAY_SLOTS_NAME));
        ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15);
        ItemMeta glassMeta = glass.getItemMeta();
        glassMeta.setDisplayName("§r");
        glass.setItemMeta(glassMeta);
        for (int i = 9; i < 18; i++) {
            inv.setItem(i, glass);
        }
        plugin.getRegisterSlotManager().getRegisterSlotMap().forEach((slot, registerSlot) -> inv.setItem(slot > 8 ? slot + 9 : slot, registerSlot.getItem()));
        player.openInventory(inv);
    }


}
