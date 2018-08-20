package github.saukiya.sxattribute.inventory;

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
 * @since 2018年6月12日
 */

public class SellInventory {

    public static void openSellInventory(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, Message.getMsg(Message.INVENTORY__SELL__NAME));
        ItemStack stainedGlass = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 15);
        ItemMeta glassMeta = stainedGlass.getItemMeta();
        glassMeta.setDisplayName("§r");
        stainedGlass.setItemMeta(glassMeta);
        for (int i = 18; i < 27; i++) {
            inv.setItem(i, stainedGlass);
        }

        ItemStack enterItem = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 4);
        ItemMeta enterMeta = enterItem.getItemMeta();
        enterMeta.setDisplayName(Message.getMsg(Message.INVENTORY__SELL__SELL));
        enterMeta.setLore(Message.getStringList(Message.INVENTORY__SELL__LORE__DEFAULT));
        enterMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        enterItem.setItemMeta(enterMeta);
        inv.setItem(22, enterItem);

        player.openInventory(inv);
    }

}
