package github.saukiya.sxattribute.data;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.condition.SubCondition;
import github.saukiya.sxattribute.util.Config;
import github.saukiya.sxattribute.util.Message;
import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;

import java.util.*;

/**
 * @author Saukiya
 */
@Getter
class ItemData {

    private String name;

    private ItemStack item;

    private boolean importItem;

    private List<String> ids;

    private List<String> enchantList;

    private int hashCode;


    ItemData(String name,ItemStack item, boolean importItem, List<String> ids, List<String> enchantList, int hashCode) {
        this.name = name;
        this.item = item;
        this.importItem = importItem;
        this.ids = ids;
        this.enchantList = enchantList;
        this.hashCode = hashCode;
    }

    public ItemStack getItem(SXAttribute plugin, Player player) {
        if (importItem){
            return getItem().clone();
        }
        ItemStack item = this.getItem().clone();
        Map<String, String> lockRandomMap = new HashMap<>();
        String id = plugin.getRandomStringManager().processRandomString(name, this.getIds().get(SXAttribute.getRandom().nextInt(this.getIds().size())), lockRandomMap);
        int itemMaterial = 260, itemDurability = 0;
        if (id != null) {
            if (id.contains(":")) {
                String[] idSplit = id.split(":");
                itemMaterial = Integer.valueOf(idSplit[0]);
                itemDurability = Integer.valueOf(idSplit[1]);
            } else {
                itemMaterial = Integer.valueOf(id);
            }
        }
        item.setTypeId(itemMaterial);
        item.setDurability((short) itemDurability);
        if (item.getType().name().equals(Material.AIR.name())) {
            Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§cItem §4" + name + "§c Error ID! Please Check ID: §4" + id + "§c!");
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
            if (meta.hasDisplayName()) {
                String itemName = plugin.getRandomStringManager().processRandomString(name, meta.getDisplayName(), lockRandomMap);
                meta.setDisplayName(itemName.replace("&", "§").replace("%DeleteLore%", ""));
            }
            List<String> loreList = meta.getLore();
            for (int i = loreList.size() - 1; i >= 0; i--) {
                String lore = plugin.getRandomStringManager().processRandomString(name, loreList.get(i), lockRandomMap);
                // 计算耐久值
                if (lore.contains(Config.getConfig().getString(Config.NAME_DURABILITY))) {
                    // 识别物品是否为工具
                    if (item.getType().getMaxDurability() > 0) {
                        if (!SubCondition.getUnbreakable(meta)) {
                            Repairable repairable = (Repairable) meta;
                            repairable.setRepairCost(999);
                            meta = (ItemMeta) repairable;
                            int durability = SubCondition.getDurability(lore);
                            int maxDurability = SubCondition.getMaxDurability(lore);
                            int maxDefaultDurability = item.getType().getMaxDurability();
                            int defaultDurability = (int) (((double) durability / maxDurability) * maxDefaultDurability);
                            item.setDurability((short) (maxDefaultDurability - defaultDurability));
                        }
                    }
                }
                if (lore.contains("%DeleteLore%")) {
                    loreList.remove(i);
                } else {
                    lore = lore.replace("&", "§");
                    if (lore.contains("\n") || lore.contains("/n")) {
                        loreList.remove(i);
                        loreList.addAll(i, Arrays.asList(lore.replace("/n", "\n").split("\n")));
                    } else {
                        loreList.set(i, lore);
                    }
                }
            }
            if (SXAttribute.isPlaceholder() && player != null) {
                loreList = PlaceholderAPI.setPlaceholders(player, loreList);
            }
            meta.setLore(loreList);
            item.setItemMeta(meta);
        }
        if (this.getEnchantList() != null && this.getEnchantList().size() > 0) {
            List<String> enchantList = new ArrayList<>();
            for (String enchantName : this.getEnchantList()) {
                enchantName = plugin.getRandomStringManager().processRandomString(name, enchantName, lockRandomMap);
                if (enchantName.contains("\n") || enchantName.contains("/n")) {
                    enchantList.addAll(Arrays.asList(enchantName.replace("/n", "\n").split("\n")));
                } else {
                    enchantList.add(enchantName);
                }
            }
            for (String enchantName : enchantList) {
                if (enchantName.contains(":") && enchantName.split(":").length > 1) {
                    Enchantment enchant = Enchantment.getByName(enchantName.split(":")[0]);
                    int level = Integer.valueOf(enchantName.split(":")[1].replaceAll("[^0-9]", ""));
                    if (enchant != null) {
                        if (level > 0) {
                            meta.addEnchant(enchant, level, true);
                        }
                    } else {
                        Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§c物品: §4" + name + " §c的附魔: §4" + enchantName.split(":")[0] + "§c 不是正常的附魔名称！");
                    }
                }
            }
        }
        item.setItemMeta(meta);
        // 存储lockRandomMap
        if (lockRandomMap.size() > 0) {
            List<String> list = new ArrayList<>();
            lockRandomMap.forEach((key, value) -> list.add(key + "§e§l§k|§e§r" + value));
            plugin.getItemUtil().setNBTList(item, "LockRandomMap", list);
        }
        if (Config.isDamageGauges()) {
            return plugin.getItemUtil().setAttackSpeed(item);
        } else {
            return item;
        }
    }
}
