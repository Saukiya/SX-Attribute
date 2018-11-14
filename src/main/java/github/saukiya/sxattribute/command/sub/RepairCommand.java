package github.saukiya.sxattribute.command.sub;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.command.SenderType;
import github.saukiya.sxattribute.command.SubCommand;
import github.saukiya.sxattribute.data.ItemDataManager;
import github.saukiya.sxattribute.data.condition.SubCondition;
import github.saukiya.sxattribute.util.Config;
import github.saukiya.sxattribute.util.Message;
import github.saukiya.sxattribute.util.MoneyUtil;
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
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * 修复物品指令
 *
 * @author Saukiya
 */
public class RepairCommand extends SubCommand implements Listener {

    public RepairCommand() {
        super("repair", SenderType.PLAYER);
    }

    /**
     * 打开修复界面
     *
     * @param player Player
     */
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

    @Override
    public void onEnable() {
        super.onEnable();
        Bukkit.getPluginManager().registerEvents(this, getPlugin());
    }

    @Override
    public void onCommand(SXAttribute plugin, CommandSender sender, String[] args) {
        if (SXAttribute.isVault()) {
            openRepairInventory((Player) sender);
        } else {
            sender.sendMessage(Message.getMsg(Message.PLAYER__NO_VAULT));
        }
    }

    @Override
    public boolean hide() {
        return !SXAttribute.isVault();
    }

    @Override
    public List<String> onTabComplete(SXAttribute plugin, CommandSender sender, String[] args) {
        return null;
    }

    @EventHandler
    void onInventoryClickRepairEvent(InventoryClickEvent event) {
        if (event.getInventory().getName().equals(Message.getMsg(Message.INVENTORY__REPAIR__NAME))) {
            if (event.getRawSlot() < 0) {
                event.getView().getPlayer().closeInventory();
                return;
            }
            Inventory inv = event.getInventory();
            ItemStack enterItem = inv.getItem(25);
            ItemMeta enterMeta = enterItem.getItemMeta();
            Player player = (Player) event.getView().getPlayer();
            double value = Config.getConfig().getDouble(Config.REPAIR_ITEM_VALUE);
            if (event.getRawSlot() != 20 && event.getRawSlot() < 45) {
                event.setCancelled(true);
                if (event.getRawSlot() == 25) {
                    ItemStack item = inv.getItem(20);
                    if (item != null && !item.getType().equals(Material.AIR) && item.getItemMeta().hasLore() && item.getAmount() == 1) {
                        ItemMeta meta = item.getItemMeta();
                        List<String> loreList = meta.getLore();
                        for (int i = 0; i < loreList.size(); i++) {
                            String lore = loreList.get(i);
                            if (lore.contains(Config.getConfig().getString(Config.NAME_DURABILITY))) {
                                int durability = SubCondition.getDurability(lore);
                                int maxDurability = SubCondition.getMaxDurability(lore);
                                if (maxDurability != 0 && durability < maxDurability) {
                                    double money = (maxDurability - durability) * value;
                                    if (!enterMeta.hasEnchants()) {
                                        if (MoneyUtil.has(player, money)) {
                                            enterMeta.addEnchant(Enchantment.DURABILITY, 1, true);
                                            enterMeta.setDisplayName(Message.getMsg(Message.INVENTORY__REPAIR__MONEY));
                                        } else {
                                            enterMeta.setDisplayName(Message.getMsg(Message.INVENTORY__REPAIR__NO_MONEY));
                                        }
                                        enterMeta.setLore(Message.getStringList(Message.INVENTORY__REPAIR__LORE__MONEY, maxDurability - durability, money, value));
                                    } else {
                                        enterMeta.removeEnchant(Enchantment.DURABILITY);
                                        if (MoneyUtil.has(player, money)) {
                                            MoneyUtil.take(player, money);
                                            enterMeta.setDisplayName(Message.getMsg(Message.INVENTORY__REPAIR__REPAIR, money));
                                            enterMeta.setLore(Message.getStringList(Message.INVENTORY__REPAIR__LORE__ENTER, value));
                                            //TODO 修理步骤
                                            lore = ItemDataManager.replaceColor(ItemDataManager.clearColor(lore).replaceFirst(String.valueOf(durability), String.valueOf(maxDurability)));
                                            loreList.set(i, lore);
                                            meta.setLore(loreList);
                                            if (!SubCondition.getUnbreakable(meta) && item.getType().getMaxDurability() != 0) {
                                                item.setDurability((short) 0);
                                            }
                                            item.setItemMeta(meta);
                                        } else {
                                            enterMeta.setDisplayName(Message.getMsg(Message.INVENTORY__REPAIR__NO_MONEY));
                                        }
                                    }
                                    enterItem.setItemMeta(enterMeta);
                                    return;
                                }
                                break;
                            }
                        }
                    }
                    enterMeta.removeEnchant(Enchantment.DURABILITY);
                    enterMeta.setDisplayName(Message.getMsg(Message.INVENTORY__REPAIR__UNSUITED));
                    enterMeta.setLore(Message.getStringList(Message.INVENTORY__REPAIR__LORE__ENTER, value));
                    enterItem.setItemMeta(enterMeta);
                    return;
                }

            }
            enterMeta.removeEnchant(Enchantment.DURABILITY);
            enterMeta.setDisplayName(Message.getMsg(Message.INVENTORY__REPAIR__ENTER));
            enterMeta.setLore(Message.getStringList(Message.INVENTORY__REPAIR__LORE__ENTER, value));
            enterItem.setItemMeta(enterMeta);
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
            inv.clear();
        }
    }
}
