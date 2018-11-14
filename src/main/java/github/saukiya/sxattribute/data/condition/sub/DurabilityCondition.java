package github.saukiya.sxattribute.data.condition.sub;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.ItemDataManager;
import github.saukiya.sxattribute.data.condition.SXConditionReturnType;
import github.saukiya.sxattribute.data.condition.SubCondition;
import github.saukiya.sxattribute.util.Config;
import github.saukiya.sxattribute.util.Message;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

/**
 * 耐久标签
 *
 * @author Saukiya
 */
public class DurabilityCondition extends SubCondition implements Listener {

    public DurabilityCondition() {
        super("Durability");
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, getPlugin());
    }

    /**
     * 低版本清理物品的方式
     *
     * @param player Player
     * @param item   ItemStack
     */
    private void clearItem(Player player, ItemStack item) {
        EntityEquipment eq = player.getEquipment();
        if (item.equals(eq.getBoots())) {
            eq.setBoots(new ItemStack(Material.AIR));
        } else if (item.equals(eq.getChestplate())) {
            eq.setChestplate(new ItemStack(Material.AIR));
        } else if (item.equals(eq.getHelmet())) {
            eq.setHelmet(new ItemStack(Material.AIR));
        } else if (item.equals(eq.getLeggings())) {
            eq.setLeggings(new ItemStack(Material.AIR));
        } else if (item.equals(eq.getItemInMainHand())) {
            eq.setItemInMainHand(new ItemStack(Material.AIR));
        } else if (item.equals(eq.getItemInOffHand())) {
            eq.setItemInOffHand(new ItemStack(Material.AIR));
        }
    }

    /**
     * 扣取物品耐久度
     *
     * @param player         玩家
     * @param item           物品
     * @param takeDurability 扣取值
     * @return boolean 成功则true 否则false
     */
    private boolean takeDurability(Player player, ItemStack item, int takeDurability) {
        if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
            ItemMeta meta = item.getItemMeta();
            List<String> loreList = meta.getLore();
            takeDurability = item.getType().toString().contains("_") && "SPADE|PICKAXE|AXE|HDE".contains(item.getType().toString().split("_")[1]) ? 1 : takeDurability;
            for (int i = 0; i < loreList.size(); i++) {
                String lore = loreList.get(i);
                if (lore.contains(Config.getConfig().getString(Config.NAME_DURABILITY))) {
                    // 扣取耐久值 设定耐久条耐久
                    int durability = getDurability(lore) - takeDurability;
                    if (durability < 0) durability = 0;
                    int maxDurability = getMaxDurability(lore);
                    if (durability > maxDurability) {
                        durability = maxDurability;
                    }
                    // 扣取耐久时免疫颜色代码
                    lore = ItemDataManager.replaceColor(ItemDataManager.clearColor(lore).replaceFirst(String.valueOf(getDurability(lore)), String.valueOf(durability)));
                    loreList.set(i, lore);
                    meta.setLore(loreList);
                    item.setItemMeta(meta);
                    if (meta instanceof Repairable) {
                        // 禁止修复
                        Repairable repairable = (Repairable) meta;
                        if (repairable.getRepairCost() != 999) {
                            repairable.setRepairCost(999);
                            item.setItemMeta((ItemMeta) repairable);
                        }
                    }
                    // 物品是否消失
                    if (durability <= 0) {
                        if (Config.isClearItemDurability()) {
                            // 当耐久为0时物品消失 并取消属性
                            if (SXAttribute.getVersionSplit()[1] > 10) {
                                item.setAmount(0);
                                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_BREAK, 1f, 1f);
                            } else {
                                clearItem(player, item);
                                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_BREAK, 1f, 1f);
                            }
                            updateAllDataAndStats(player);
                            return true;
                        }
                        updateAllDataAndStats(player);
                    }
                    // 设定耐久条
                    if (item.getType().getMaxDurability() != 0 && !getUnbreakable(meta)) {
                        int maxDefaultDurability = item.getType().getMaxDurability();
                        int defaultDurability = (int) (((double) durability / maxDurability) * maxDefaultDurability);
                        item.setDurability((short) (maxDefaultDurability - defaultDurability));
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 在物品损坏时，更新玩家所有数据
     *
     * @param player Player
     */
    private void updateAllDataAndStats(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                SXAttribute.getApi().updateEquipmentData(player);
                SXAttribute.getApi().updateRPGInventoryData(player);
                SXAttribute.getApi().updateHandData(player);
                SXAttribute.getApi().updateStats(player);
            }
        }.runTaskAsynchronously(getPlugin());
    }

    @EventHandler
    void onItemDurabilityEvent(PlayerItemDamageEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        // 如果是 取消原版事件
        if (takeDurability(player, item, event.getDamage())) {
            event.setCancelled(true);
        }
    }

    @Override
    public SXConditionReturnType determine(LivingEntity entity, ItemStack item, String lore) {
        if (lore.contains(Config.getConfig().getString(Config.NAME_DURABILITY))) {
            if (getDurability(lore) <= 0) {
                if (entity instanceof Player && item != null) {
                    Message.send((Player) entity, Message.getMsg(Message.PLAYER__NO_DURABILITY, getItemName(item)));
                }
                return SXConditionReturnType.ITEM;
            }
        }
        return SXConditionReturnType.NULL;
    }
}
