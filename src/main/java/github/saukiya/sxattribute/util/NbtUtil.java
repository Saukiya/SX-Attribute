package github.saukiya.sxattribute.util;

import github.saukiya.util.nms.ItemUtil;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static github.saukiya.util.nms.NbtUtil.getInst;

/**
 * @author Saukiya
 */

@Getter
public class NbtUtil {

    public boolean isEquipment(ItemStack item) {
        switch (item.getType().name()) {
            case "DIAMOND_HELMET":
            case "DIAMOND_CHESTPLATE":
            case "DIAMOND_LEGGINGS":
            case "DIAMOND_BOOTS":
            case "GOLD_HELMET":
            case "GOLD_CHESTPLATE":
            case "GOLD_LEGGINGS":
            case "GOLD_BOOTS":
            case "IRON_HELMET":
            case "IRON_CHESTPLATE":
            case "IRON_LEGGINGS":
            case "IRON_BOOTS":
            case "LEATHER_HELMET":
            case "LEATHER_CHESTPLATE":
            case "LEATHER_LEGGINGS":
            case "LEATHER_BOOTS":
            case "CHAINMAIL_HELMET":
            case "CHAINMAIL_CHESTPLATE":
            case "CHAINMAIL_LEGGINGS":
            case "CHAINMAIL_BOOTS":
            case "DIAMOND_AXE":
            case "DIAMOND_HOE":
            case "DIAMOND_SWORD":
            case "DIAMOND_SPADE":
            case "DIAMOND_PICKAXE":
            case "GOLD_AXE":
            case "GOLD_HOE":
            case "GOLD_SWORD":
            case "GOLD_SPADE":
            case "GOLD_PICKAXE":
            case "IRON_AXE":
            case "IRON_HOE":
            case "IRON_SWORD":
            case "IRON_SPADE":
            case "IRON_PICKAXE":
            case "STONE_AXE":
            case "STONE_HOE":
            case "STONE_SWORD":
            case "STONE_SPADE":
            case "STONE_PICKAXE":
            case "WOOD_AXE":
            case "WOOD_HOE":
            case "WOOD_SWORD":
            case "WOOD_SPADE":
            case "WOOD_PICKAXE":
                return true;
            default:
                return false;
        }
    }

    /**
     * 清除物品默认属性标签
     *
     * @param item ItemStack
     */
    public ItemStack clearAttribute(ItemStack item) {
        ItemUtil.getInst().clearAttribute(item);
        return item;
    }

    /**
     * 获取全部NBT数据
     *
     * @param item ItemStack
     * @return String
     */
    public String getAllNBT(ItemStack item) {
        try {
            Object nmsItem = getInst().getNMSItem(item);
            if (nmsItem != null) {
                return "§c[" + item.getType().name() + ":" + item.getDurability() + "-" + item.hashCode() + "]§7 " + getInst().getNMSItemNBT(nmsItem).toString().replace("§", "&");
            }
            return "§c[" + item.getType().name() + ":" + item.getDurability() + "-" + item.hashCode() + "]§7 §cNULL";
        } catch (Exception e) {
            return null;
        }

    }

    /**
     * 设置物品NBT数据
     *
     * @param item  ItemStack
     * @param key   String
     * @param value String
     * @return ItemStack
     */
    public ItemStack setNBT(ItemStack item, String key, Object value) {
        try {
            getInst().getItemTagWrapper(item).builder().set(key, value).save();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return item;
    }

    /**
     * 设置物品NBT数据 List 会被设置ItemMeta
     *
     * @param item ItemStack
     * @param key  String
     * @param list List
     * @return ItemStack
     */
    public ItemStack setNBTList(ItemStack item, String key, List<String> list) {
        try {
            getInst().getItemTagWrapper(item).builder().set(key, list).save();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return item;
    }

    /**
     * 获取物品NBT数据
     *
     * @param item ItemStack
     * @param key  String
     * @return String
     */
    public String getNBT(ItemStack item, String key) {
        try {
            Object result = getInst().getItemTagWrapper(item).get(key);
            if (result != null) {
                return String.valueOf(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 设置物品NBT数据 List
     *
     * @param item ItemStack
     * @param key  String
     * @return List
     */
    public List<String> getNBTList(ItemStack item, String key) {
        List<String> list = new ArrayList<>();
        try {
            Object result = getInst().getItemTagWrapper(item).get(key);
            if (result instanceof List) {
                for (Object o : (List) result) {
                    list.add(o.toString());
                }
                return list;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 判断是否有物品NBT数据
     *
     * @param item ItemStack
     * @param key  String
     * @return Boolean
     */
    public boolean hasNBT(ItemStack item, String key) {
        try {
            return getInst().getItemTagWrapper(item).get(key) != null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * 清除指定nbt
     *
     * @param item ItemStack
     * @param key  String
     * @return boolean
     */
    public boolean removeNBT(ItemStack item, String key) {
        try {
            Object result = getInst().getItemTagWrapper(item).set(key, null);
            return result != null;
        } catch (Exception e) {
            return false;
        }
    }
}
