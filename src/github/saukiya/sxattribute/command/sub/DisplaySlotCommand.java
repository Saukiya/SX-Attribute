package github.saukiya.sxattribute.command.sub;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.command.SenderType;
import github.saukiya.sxattribute.command.SubCommand;
import github.saukiya.sxattribute.util.Message;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * 槽位展示指令
 *
 * @author Saukiya
 */
public class DisplaySlotCommand extends SubCommand implements Listener {

    public DisplaySlotCommand() {
        super("displaySlot", SenderType.PLAYER);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        Bukkit.getPluginManager().registerEvents(this, getPlugin());
    }

    @Override
    public void onCommand(SXAttribute plugin, CommandSender sender, String[] args) {
        if (plugin.getRegisterSlotManager().getRegisterSlotMap().size() > 0) {
            openDisplaySlotInventory((Player) sender);
        } else {
            sender.sendMessage(Message.getMsg(Message.PLAYER__NO_REGISTER_SLOTS));
        }
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
        SXAttribute.getApi().getRegisterSlotMapEntrySet().forEach((entry) -> inv.setItem(entry.getKey() > 8 ? entry.getKey() + 9 : entry.getKey(), entry.getValue().getItem()));
        player.openInventory(inv);
    }

    @EventHandler
    void onInventoryClickStatsEvent(InventoryClickEvent event) {
        if (event.getInventory().getName().equalsIgnoreCase(Message.getMsg(Message.INVENTORY__DISPLAY_SLOTS_NAME))) {
            event.setCancelled(true);
        }
    }

    @Override
    public List<String> onTabComplete(SXAttribute plugin, CommandSender sender, String[] args) {
        return null;
    }

}
