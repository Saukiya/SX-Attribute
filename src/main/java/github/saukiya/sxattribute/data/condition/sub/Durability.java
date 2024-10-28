package github.saukiya.sxattribute.data.condition.sub;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.condition.SubCondition;
import github.saukiya.sxattribute.util.Config;
import github.saukiya.sxattribute.util.Message;
import github.saukiya.sxitem.event.SXItemSpawnEvent;
import github.saukiya.sxitem.event.SXItemUpdateEvent;
import github.saukiya.tools.nms.NMS;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;

import java.util.Arrays;
import java.util.List;

/**
 * 耐久标签
 *
 * @author Saukiya
 */
public class Durability extends SubCondition implements Listener {

    private final List<String> COLOR_LIST = Arrays.asList("§0", "§1", "§2", "§3", "§4", "§5", "§6", "§7", "§8", "§9");

    private final List<String> COLOR_REPLACE_LIST = Arrays.asList("%零%", "%一%", "%二%", "%三%", "%四%", "%五%", "%六%", "%七%", "%八%", "%九%");

    public Durability() {
        super(SXAttribute.getInst());
    }

    /**
     * 低版本清理物品的方式
     *
     * @param player Player
     * @param item   ItemStack
     */
    private static void clearItem(Player player, ItemStack item) {
        EntityEquipment eq = player.getEquipment();
        ItemStack itemAir = new ItemStack(Material.AIR);
        if (item.equals(eq.getBoots())) {
            eq.setBoots(itemAir);
        } else if (item.equals(eq.getChestplate())) {
            eq.setChestplate(itemAir);
        } else if (item.equals(eq.getHelmet())) {
            eq.setHelmet(itemAir);
        } else if (item.equals(eq.getLeggings())) {
            eq.setLeggings(itemAir);
        } else if (item.equals(eq.getItemInMainHand())) {
            eq.setItemInMainHand(itemAir);
        } else if (item.equals(eq.getItemInOffHand())) {
            eq.setItemInOffHand(itemAir);
        }
    }

    /**
     * 修改物品耐久度
     *
     * @param player         玩家
     * @param item           物品
     * @param takeDurability 扣取值 - 支持反向操作
     * @return boolean 成功则true 否则false
     */
    private boolean editDurability(Player player, ItemStack item, int takeDurability) {
        if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
            ItemMeta meta = item.getItemMeta();
            List<String> loreList = meta.getLore();
            takeDurability = item.getType().toString().contains("_") && "SPADE|PICKAXE|AXE|HDE".contains(item.getType().toString().split("_")[1]) ? 1 : takeDurability;
            for (int i = 0; i < loreList.size(); i++) {
                String lore = loreList.get(i);
                if (lore.contains(Config.getConfig().getString(Config.NAME_DURABILITY))) {
                    // 扣取耐久值 设定耐久条耐久
                    int thisDurability = getDurability(lore);
                    int maxDurability = getMaxDurability(lore);
                    int durability = Math.min(Math.max(thisDurability - takeDurability, 0), maxDurability);

                    // 扣取耐久时免疫颜色代码
                    loreList.set(i, replaceNumberColor(clearNumberColor(lore).replaceFirst(String.valueOf(thisDurability), String.valueOf(durability))));
                    meta.setLore(loreList);
                    if (meta instanceof Repairable) {
                        ((Repairable) meta).setRepairCost(999);
                    }
                    item.setItemMeta(meta);
                    // 物品是否消失
                    if (durability <= 0) {
                        if (Config.isClearItemDurability()) {
                            Bukkit.getPluginManager().callEvent(new PlayerItemBreakEvent(player, item));
                            // 当耐久为0时物品消失 并取消属性
                            if (NMS.compareTo(1,10,0) >= 0) {
                                item.setAmount(0);
                            } else {
                                clearItem(player, item);
                            }
                            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_BREAK, 1f, 1f);
                            SXAttribute.getApi().updateData(player);
                            SXAttribute.getApi().attributeUpdate(player);
                            return true;
                        }
                        SXAttribute.getApi().updateData(player);
                        SXAttribute.getApi().attributeUpdate(player);
                    }
                    // 设定耐久条
                    if (item.getType().getMaxDurability() != 0 && !isUnbreakable(meta)) {
                        double defaultDurability = ((double) durability / maxDurability) * item.getType().getMaxDurability();
                        item.setDurability((short) (item.getType().getMaxDurability() - defaultDurability));
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    void onItemDurabilityEvent(PlayerItemDamageEvent event) {
        event.setCancelled(editDurability(event.getPlayer(), event.getItem(), event.getDamage()));
    }

    @EventHandler(priority = EventPriority.LOW)
    void onSXItemSpawnEvent(SXItemSpawnEvent event) {
        if (event instanceof SXItemUpdateEvent) return;
        editDurability(event.getPlayer(), event.getItem(), 0);
    }

    @EventHandler
    void onSXItemUpdateEvent(SXItemUpdateEvent event) {
        if (event.isCancelled()) return;
        ItemMeta itemMeta = event.getItem().getItemMeta();
        ItemMeta oldItemMeta = event.getOldItem().getItemMeta();
        if (itemMeta.hasLore() && oldItemMeta.hasLore()) {
            List<String> itemList = itemMeta.getLore();
            List<String> oldItemList = oldItemMeta.getLore();
            String detect = Config.getConfig().getString(Config.NAME_DURABILITY);
            for (int i = itemList.size() - 1; i >= 0; i--) {
                if (itemList.get(i).contains(detect)) {
                    for (int i1 = oldItemList.size() - 1; i1 >= 0; i1--) {
                        String oldLore = oldItemList.get(i1);
                        if (oldLore.contains(detect)) {
                            double oldMaxDurability = SubCondition.getMaxDurability(oldLore);
                            double oldDurability = SubCondition.getDurability(oldLore);
                            String lore = itemList.get(i);
                            double maxDurability = SubCondition.getMaxDurability(lore);
                            itemList.set(i, replaceNumberColor(clearNumberColor(lore).replaceFirst(String.valueOf(SubCondition.getDurability(lore)), String.valueOf((int) (oldDurability / oldMaxDurability * maxDurability)))));
                            itemMeta.setLore(itemList);
                            event.getItem().setItemMeta(itemMeta);
                            return;
                        }
                    }
                }
            }
        }
        editDurability(event.getPlayer(), event.getItem(), 0);
    }

    @Override
    public boolean determine(LivingEntity entity, ItemStack item, String lore) {
        if (lore.contains(Config.getConfig().getString(Config.NAME_DURABILITY))) {
            if (getDurability(lore) <= 0 && item != null) {
                Message.send(entity, Message.PLAYER__NO_DURABILITY, getItemName(item));
                return false;
            }
        }
        return true;
    }

    /**
     * 清除物品颜色
     *
     * @param lore String
     * @return String
     */
    public String clearNumberColor(String lore) {
        for (int i = 0; i < 10; i++) {
            lore = lore.replace(COLOR_LIST.get(i), COLOR_REPLACE_LIST.get(i));
        }
        return lore;
    }

    /**
     * 恢复物品数字颜色
     *
     * @param lore String
     * @return String
     */
    public String replaceNumberColor(String lore) {
        for (int i = 0; i < 10; i++) {
            lore = lore.replace(COLOR_REPLACE_LIST.get(i), COLOR_LIST.get(i));
        }
        return lore;
    }
}
