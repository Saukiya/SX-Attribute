package github.saukiya.sxattribute.command.sub;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.command.SXAttributeCommand;
import github.saukiya.sxattribute.util.Message;
import github.saukiya.sxattribute.util.MoneyUtil;
import github.saukiya.sxitem.command.SenderType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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
import java.util.function.Supplier;

/**
 * 出售物品指令
 *
 * @author Saukiya
 */
public class SellCommand extends SXAttributeCommand implements Listener {

    public static final InventoryHolder holder = () -> null;

    public SellCommand() {
        super("sell", 70);
        setType(SenderType.PLAYER);
    }

    /**
     * 打开出售界面
     *
     * @param player Player
     */
    public static void openSellInventory(Player player) {
        player.openInventory(InventoryCreator.getInventory());
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
                        if (item != null && !item.getType().equals(Material.AIR) && item.getItemMeta().hasLore()) {
                            //TODO double value = ItemDataManager.getSellValue(item) * item.getAmount();
                            double value = 20 * item.getAmount();
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
                if (item != null && !item.getType().equals(Material.AIR)) {
                    player.getInventory().addItem(item);
                }
            }
            inv.clear();
        }
    }

    public static class InventoryCreator {
        static ItemStack[] items;

        static {
            items = new ItemStack[27];
            ItemStack blackGlass = createItem("STAINED_GLASS_PANE", 15, () -> Material.BLACK_STAINED_GLASS_PANE);
            for (int i = 18; i < 27; i++) {
                items[i] = blackGlass;
            }
        }

        static Inventory getInventory() {
            Inventory inv = Bukkit.createInventory(holder, 27, Message.getMsg(Message.INVENTORY__SELL__NAME));
            inv.setContents(items);
            ItemStack enterItem = createItem("STAINED_GLASS_PANE", 4, () -> Material.YELLOW_STAINED_GLASS_PANE);
            ItemMeta enterMeta = enterItem.getItemMeta();
            enterMeta.setDisplayName(Message.getMsg(Message.INVENTORY__SELL__SELL));
            enterMeta.setLore(Message.getStringList(Message.INVENTORY__SELL__LORE__DEFAULT));
            enterMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            enterItem.setItemMeta(enterMeta);
            inv.setItem(22, enterItem);

            return inv;
        }

        private static ItemStack createItem(String oldName, int oldSubId, Supplier<Material> newMaterial) {
            Material material = Material.getMaterial(oldName);
            ItemStack item;
            if (material != null) {
                item = new ItemStack(material, 1, (short) oldSubId);
            } else {
                item = new ItemStack(newMaterial.get());
            }
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("§r");
            item.setItemMeta(meta);
            return item;
        }
    }
}
