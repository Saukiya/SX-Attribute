package github.saukiya.sxattribute.listener;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.ItemDataManager;
import github.saukiya.sxattribute.data.condition.SubCondition;
import github.saukiya.sxattribute.inventory.StatsInventory;
import github.saukiya.sxattribute.util.Config;
import github.saukiya.sxattribute.util.Message;
import github.saukiya.sxattribute.util.MoneyUtil;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Saukiya
 * @since 2018年3月16日
 */

public class OnInventoryClickListener implements Listener {

    private final SXAttribute plugin;

    public OnInventoryClickListener(SXAttribute plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    void onInventoryClickStatsEvent(InventoryClickEvent event) {
        if (event.getInventory().getName().equals(Message.getMsg(Message.INVENTORY__STATS__NAME))) {
            if (event.getRawSlot() < 0) {
                event.getView().getPlayer().closeInventory();
                return;
            }
            event.setCancelled(true);
            if (event.getRawSlot() == 4) {
                Player player = (Player) event.getView().getPlayer();
                if (StatsInventory.getHideList().contains(player.getUniqueId())) {
                    StatsInventory.getHideList().remove(player.getUniqueId());
                } else {
                    StatsInventory.getHideList().add(player.getUniqueId());
                }
                plugin.getStatsInventory().openStatsInventory(player);
            }
        }
        if (event.getInventory().getName().equalsIgnoreCase(Message.getMsg(Message.INVENTORY__DISPLAY_SLOTS_NAME))) {
            event.setCancelled(true);
        }
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
    void onInventoryClickSellEvent(InventoryClickEvent event) {
        if (event.getInventory().getName().equals(Message.getMsg(Message.INVENTORY__SELL__NAME))) {
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
                        Message.send(player, Message.getMsg(Message.PLAYER__SELL, size, sell));
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
}
