package github.saukiya.sxattribute.command.sub;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.command.SenderType;
import github.saukiya.sxattribute.command.SubCommand;
import github.saukiya.sxattribute.data.itemdata.ItemDataManager;
import github.saukiya.sxattribute.util.Message;
import github.saukiya.sxattribute.util.MoneyUtil;
import github.saukiya.sxattribute.verision.MaterialControl;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * 出售物品指令
 *
 * @author Saukiya
 */
public class SellCommand extends SubCommand implements Listener {

    private static final InventoryHolder holder = () -> null;

    public SellCommand() {
        super("sell");
        setType(SenderType.PLAYER);
    }

    /**
     * 打开出售界面
     *
     * @param player Player
     */
    public static void openSellInventory(Player player) {
        Inventory inv = Bukkit.createInventory(holder, 27, Message.getMsg(Message.INVENTORY__SELL__NAME));
        ItemStack stainedGlass = MaterialControl.BLACK_STAINED_GLASS_PANE.parseItem();
        ItemMeta glassMeta = stainedGlass.getItemMeta();
        glassMeta.setDisplayName("§r");
        stainedGlass.setItemMeta(glassMeta);
        for (int i = 18; i < 27; i++) {
            inv.setItem(i, stainedGlass);
        }

        ItemStack enterItem = MaterialControl.YELLOW_STAINED_GLASS_PANE.parseItem();
        ItemMeta enterMeta = enterItem.getItemMeta();
        enterMeta.setDisplayName(Message.getMsg(Message.INVENTORY__SELL__SELL));
        enterMeta.setLore(Message.getStringList(Message.INVENTORY__SELL__LORE__DEFAULT));
        enterMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        enterItem.setItemMeta(enterMeta);
        inv.setItem(22, enterItem);

        player.openInventory(inv);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        if (SXAttribute.isVault()) {
            openSellInventory((Player) sender);
        } else {
            sender.sendMessage(Message.getMsg(Message.PLAYER__NO_VAULT));
        }
    }

    @Override
    public boolean isUse(CommandSender sender, SenderType type) {
        return super.isUse(sender, type) && SXAttribute.isVault();
    }

    @EventHandler
    void onInventoryClickSellEvent(InventoryClickEvent event) {
        if (!event.isCancelled() && holder.equals(event.getInventory().getHolder())) {
            if (event.getRawSlot() < 0) {
                event.getView().getPlayer().closeInventory();
                return;
            }
            Inventory inv = event.getInventory();
            ItemStack enterItem = inv.getItem(22);
            ItemMeta enterMeta = enterItem.getItemMeta();
            Player player = (Player) event.getView().getPlayer();
            if (event.getRawSlot() >= 18 && event.getRawSlot() < 27) {
                event.setCancelled(true);
                if (event.getRawSlot() == 22) {
                    double sell = 0;
                    int size = 0;
                    List<String> loreList = new ArrayList<>();
                    for (int i = 0; i < 18; i++) {
                        ItemStack item = inv.getItem(i);
                        if (item != null && !item.getType().equals(MaterialControl.AIR.parse()) && item.getItemMeta().hasLore()) {
                            double value = ItemDataManager.getSellValue(item) * item.getAmount();
                            sell += value;
                            if (!enterMeta.hasEnchants()) {
                                String itemName = item.getItemMeta().getDisplayName();
                                if (itemName == null) {
                                    itemName = item.getType().name();
                                }
                                if (value > 0) {
                                    loreList.add(Message.getMsg(Message.INVENTORY__SELL__LORE__FORMAT, i + 1, itemName, value));
                                } else {
                                    loreList.add(Message.getMsg(Message.INVENTORY__SELL__LORE__NO_SELL, i + 1));
                                }
                            } else {
                                if (value > 0) {
                                    size++;
                                    inv.setItem(i, null);
                                }
                            }
                        }
                    }
                    if (!enterMeta.hasEnchants()) {
                        loreList.add(Message.getMsg(Message.INVENTORY__SELL__LORE__ALL_SELL, sell));
                        enterMeta.setLore(loreList);
                        if (sell == 0) {
                            enterMeta.setDisplayName(Message.getMsg(Message.INVENTORY__SELL__NO_SELL));
                        } else {
                            enterMeta.setDisplayName(Message.getMsg(Message.INVENTORY__SELL__ENTER));
                            enterMeta.addEnchant(Enchantment.DURABILITY, 1, true);
                        }
                    } else {
                        enterMeta.setDisplayName(Message.getMsg(Message.INVENTORY__SELL__OUT, sell));
                        enterMeta.setLore(new ArrayList<>());
                        enterMeta.removeEnchant(Enchantment.DURABILITY);
                        MoneyUtil.give(player, sell);
                        Message.send(player, Message.PLAYER__SELL, size, sell);
                    }
                    enterItem.setItemMeta(enterMeta);
                    return;
                }
            }
            enterMeta.removeEnchant(Enchantment.DURABILITY);
            enterMeta.setDisplayName(Message.getMsg(Message.INVENTORY__SELL__SELL));
            enterMeta.setLore(Message.getStringList(Message.INVENTORY__SELL__LORE__DEFAULT));
            enterItem.setItemMeta(enterMeta);
        }
    }

    @EventHandler
    void onInventoryCloseSellEvent(InventoryCloseEvent event) {
        if (holder.equals(event.getInventory().getHolder())) {
            Player player = (Player) event.getView().getPlayer();
            Inventory inv = event.getInventory();
            for (int i = 0; i < 18; i++) {
                ItemStack item = inv.getItem(i);
                if (item != null && !item.getType().equals(MaterialControl.AIR.parse())) {
                    player.getInventory().addItem(item);
                }
            }
            inv.clear();
        }
    }
}
