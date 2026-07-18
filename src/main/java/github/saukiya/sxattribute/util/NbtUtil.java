package github.saukiya.sxattribute.util;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.tools.nms.ItemUtil;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static github.saukiya.tools.nms.NbtUtil.getInst;

/**
 * @author Saukiya
 */

@Getter
public class NbtUtil {

    private static boolean failureLogged;

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
        if (!isUsable(item)) return item;
        try {
            getInst().getItemTagWrapper(item).builder().set(key, value).save();
        } catch (Exception e) {
            reportFailure("setNBT", e);
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
        if (!isUsable(item)) return item;
        try {
            getInst().getItemTagWrapper(item).builder().set(key, list).save();
        } catch (Exception e) {
            reportFailure("setNBTList", e);
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
        if (!isUsable(item)) return null;
        try {
            Object result = getInst().getItemTagWrapper(item).get(key);
            if (result != null) {
                return String.valueOf(result);
            }
        } catch (Exception e) {
            reportFailure("getNBT", e);
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
        if (!isUsable(item)) return list;
        try {
            Object result = getInst().getItemTagWrapper(item).get(key);
            if (result instanceof List) {
                for (Object o : (List) result) {
                    list.add(o.toString());
                }
                return list;
            }
        } catch (Exception e) {
            reportFailure("getNBTList", e);
        }
        return list;
    }

    /**
     * 读取配置节点下可交给属性解析器处理的全部文本。
     * <p>
     * 节点可以是单个标量、列表或嵌套 Map。Map 的叶子会被转换为
     * {@code 键: 值}，使“攻击力: 10”这类结构化 NBT 与 Lore 使用同一套识别规则；
     * 列表元素则保持原文本，避免改变已有的完整属性行。
     *
     * @param item 物品
     * @param nodes 需要读取的 NBT 节点路径
     * @return 按配置顺序展开的属性文本，节点不存在或适配失败时返回空列表
     */
    public List<String> getAttributeValues(ItemStack item, List<String> nodes) {
        List<String> values = new ArrayList<>();
        if (!isUsable(item) || nodes == null || nodes.isEmpty()) return values;
        for (String node : nodes) {
            try {
                Object value = getInst().getItemTagWrapper(item).get(node);
                appendAttributeValues(values, value, null);
            } catch (Exception exception) {
                // 单个第三方节点格式异常不能阻断其余已配置节点，错误摘要仍由统一限流处理。
                reportFailure("getAttributeValues(" + node + ")", exception);
            }
        }
        return values;
    }

    /**
     * 判断物品是否含有任一已配置 NBT 属性节点。
     * 此方法用于手持物品切换的快速判定，不能仅依赖 Lore，否则纯 NBT 物品不会触发属性刷新。
     */
    public boolean hasAttributeValues(ItemStack item, List<String> nodes) {
        if (!isUsable(item) || nodes == null || nodes.isEmpty()) return false;
        for (String node : nodes) {
            try {
                if (getInst().getItemTagWrapper(item).get(node) != null) return true;
            } catch (Exception exception) {
                // 与实际读取保持相同的节点隔离语义，确保后续有效节点仍能触发刷新。
                reportFailure("hasAttributeValues(" + node + ")", exception);
            }
        }
        return false;
    }

    /**
     * 递归展开适配器返回的 Java 集合结构；只在 Map 叶子处拼接键名，
     * 因为普通列表通常已经保存了完整的 Lore 风格属性行。
     */
    private void appendAttributeValues(List<String> output, Object value, String fieldName) {
        if (value == null) return;
        if (value instanceof Map) {
            for (Object entryObject : ((Map) value).entrySet()) {
                Map.Entry entry = (Map.Entry) entryObject;
                appendAttributeValues(output, entry.getValue(), String.valueOf(entry.getKey()));
            }
            return;
        }
        if (value instanceof Iterable) {
            for (Object element : (Iterable) value) {
                appendAttributeValues(output, element, null);
            }
            return;
        }
        if (value.getClass().isArray()) {
            for (int index = 0; index < Array.getLength(value); index++) {
                appendAttributeValues(output, Array.get(value, index), null);
            }
            return;
        }
        String text = String.valueOf(value);
        if (!text.isEmpty()) {
            output.add(fieldName == null ? text : fieldName + ": " + text);
        }
    }

    /**
     * 判断是否有物品NBT数据
     *
     * @param item ItemStack
     * @param key  String
     * @return Boolean
     */
    public boolean hasNBT(ItemStack item, String key) {
        if (!isUsable(item)) return false;
        try {
            return getInst().getItemTagWrapper(item).get(key) != null;
        } catch (Exception e) {
            reportFailure("hasNBT", e);
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
        if (!isUsable(item)) return false;
        try {
            Object result = getInst().getItemTagWrapper(item).set(key, null);
            return result != null;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isUsable(ItemStack item) {
        return item != null && item.getType() != Material.AIR;
    }

    /** NMS NBT 适配失败时只记录一次摘要，避免装备刷新和战斗事件持续输出完整堆栈。 */
    private void reportFailure(String operation, Exception exception) {
        if (failureLogged) return;
        failureLogged = true;
        SXAttribute.getInst().getLogger().severe("NBT adapter failed during " + operation
                + "; NBT-dependent features will use safe defaults: " + exception.getClass().getSimpleName()
                + ": " + exception.getMessage());
    }
}
