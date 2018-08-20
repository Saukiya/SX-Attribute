package github.saukiya.sxattribute.listener;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.ItemDataManager;
import github.saukiya.sxattribute.util.Config;
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

import java.util.List;

public class OnItemDurabilityListener implements Listener {

    private final SXAttribute plugin;

    public OnItemDurabilityListener(SXAttribute plugin) {
        this.plugin = plugin;
    }

    // 获取当前耐久值
    public static int getDurability(String lore) {
        int durability = 0;
        if (lore.contains("/") && lore.split("/").length > 1) {
            durability = Integer.valueOf(lore.replaceAll("§+[0-9]", "").split("/")[0].replaceAll("[^0-9]", ""));
        }
        return durability;
    }

    // 获取最大耐久值
    public static int getMaxDurability(String lore) {
        int maxDurability = 0;
        if (lore.contains("/") && lore.split("/").length > 1) {
            maxDurability = Integer.valueOf(lore.replaceAll("§+[0-9]", "").split("/")[1].replaceAll("[^0-9]", ""));
        }
        return maxDurability;
    }

    public static Boolean getUnbreakable(ItemMeta meta) {
        if (SXAttribute.getVersionSplit()[1] >= 11) {
            //1.11.2 方法
            return meta.isUnbreakable();
        } else {
            //1.9.0方法
            return meta.spigot().isUnbreakable();
        }
    }

    public static void clearItem(LivingEntity entity, ItemStack item) {
        EntityEquipment eq = entity.getEquipment();
        if (eq.getBoots() != null && eq.getBoots().equals(item)) {
            eq.setBoots(new ItemStack(Material.AIR));
        } else if (eq.getChestplate() != null && eq.getChestplate().equals(item)) {
            eq.setChestplate(new ItemStack(Material.AIR));
        } else if (eq.getHelmet() != null && eq.getHelmet().equals(item)) {
            eq.setHelmet(new ItemStack(Material.AIR));
        } else if (eq.getLeggings() != null && eq.getLeggings().equals(item)) {
            eq.setLeggings(new ItemStack(Material.AIR));
        } else if (eq.getItemInMainHand() != null && eq.getItemInMainHand().equals(item)) {
            eq.setItemInMainHand(new ItemStack(Material.AIR));
        } else if (eq.getItemInOffHand() != null && eq.getItemInOffHand().equals(item)) {
            eq.setItemInOffHand(new ItemStack(Material.AIR));
        }
    }

    public Boolean takeDurability(LivingEntity entity, ItemStack item, int takeDurability, Boolean strip) {
        if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
            ItemMeta meta = item.getItemMeta();
            List<String> loreList = meta.getLore();
            takeDurability = item.getType().toString().contains("_") && "SPADE|PICKAXE|AXE|HDE".contains(item.getType().toString().split("_")[1]) ? 1 : takeDurability;
            for (int i = 0; i < loreList.size(); i++) {
                String lore = loreList.get(i);
                if (lore.contains(Config.getConfig().getString(Config.NAME_DURABILITY))) {
                    // 扣取耐久值 设定耐久条耐久
                    int durability = getDurability(lore) - takeDurability;
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
                        // 当耐久为0时物品消失 并取消属性
                        if (SXAttribute.getVersionSplit()[1] > 10) {
                            item.setAmount(0);
                            // 重新加载装备属性
                            if (entity instanceof Player) {
                                plugin.getOnUpdateStatsListener().updateEquipmentData((Player) entity);
                                ((Player) entity).playSound(entity.getLocation(), Sound.BLOCK_ANVIL_BREAK, 1f, 1f);
                            }
                        } else {
                            clearItem(entity, item);
                            if (entity instanceof Player) {
                                plugin.getOnUpdateStatsListener().updateEquipmentData((Player) entity);
                                ((Player) entity).playSound(entity.getLocation(), Sound.BLOCK_ANVIL_BREAK, 1f, 1f);
                            }
                        }
                        return true;
                    }
                    // 设定耐久条
                    if (strip && !getUnbreakable(meta)) {
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

    @EventHandler
    void onItemDurabilityEvent(PlayerItemDamageEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        // 如果是 取消原版事件
        if (takeDurability(player, item, event.getDamage(), true)) {
            event.setCancelled(true);
        }
    }

}
