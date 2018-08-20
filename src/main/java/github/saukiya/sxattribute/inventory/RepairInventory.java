package github.saukiya.sxattribute.inventory;

import github.saukiya.sxattribute.util.Config;
import github.saukiya.sxattribute.util.Message;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * @author Saukiya
 * @since 2018年6月16日
 */

public class RepairInventory {

    public static void openRepairInventory(Player player) {
        Inventory inv = Bukkit.createInventory(null, 45, Message.getMsg(Message.INVENTORY__REPAIR__NAME));
        ItemStack glassItem = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15);
        ItemMeta glassMeta = glassItem.getItemMeta();
        glassMeta.setDisplayName("§r");
        glassItem.setItemMeta(glassMeta);
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, glassItem);
            inv.setItem(36 + i, glassItem);
        }
        glassItem.setDurability((short) 9);
        for (int i = 0; i < 5; i++) {
            if (i == 2) continue;
            inv.setItem(9 + i, glassItem);
            inv.setItem(18 + i, glassItem);
            inv.setItem(27 + i, glassItem);
        }
        glassItem.setDurability((short) 7);
        for (int i = 6; i < 9; i++) {
            inv.setItem(9 + i, glassItem);
            inv.setItem(18 + i, glassItem);
            inv.setItem(27 + i, glassItem);
        }
        glassItem.setDurability((short) 0);
        glassItem.setType(Material.IRON_FENCE);
        for (int i = 1; i < 4; i++) {
            inv.setItem(5 + (i * 9), glassItem);
        }
        glassItem.setType(Material.THIN_GLASS);
        glassMeta.setDisplayName(Message.getMsg(Message.INVENTORY__REPAIR__GUIDE));
        glassItem.setItemMeta(glassMeta);
        inv.setItem(11, glassItem);
        inv.setItem(19, glassItem);
        inv.setItem(21, glassItem);
        inv.setItem(29, glassItem);
        ItemStack enterItem = new ItemStack(Material.ANVIL);
        ItemMeta enterMeta = enterItem.getItemMeta();
        enterMeta.setDisplayName(Message.getMsg(Message.INVENTORY__REPAIR__ENTER));
        enterMeta.setLore(Message.getStringList(Message.INVENTORY__REPAIR__LORE__ENTER, Config.getConfig().getDouble(Config.REPAIR_ITEM_VALUE)));
        enterMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        enterItem.setItemMeta(enterMeta);
        inv.setItem(25, enterItem);

        player.openInventory(inv);
    }

}
