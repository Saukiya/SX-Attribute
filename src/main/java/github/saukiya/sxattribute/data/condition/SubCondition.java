package github.saukiya.sxattribute.data.condition;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.util.Config;
import github.saukiya.tools.nms.ItemUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

/**
 * 条件抽象类
 *
 * @author Saukiya
 */
public abstract class SubCondition implements Comparable<SubCondition> {

    @Getter
    private final static List<SubCondition> conditions = new ArrayList<>();

    @Getter
    private final String name;

    @Getter
    private final JavaPlugin plugin;

    @Getter
    private final EquipmentType[] types;

    @Getter
    private int priority = -1;

    /**
     * 实现一个条件类
     *
     * @param plugin JavaPlugin 注册插件
     * @param types  SXConditionType[] 条件类型
     */
    public SubCondition(JavaPlugin plugin, EquipmentType... types) {
        this(null, plugin, types);
    }

    public SubCondition(String name, JavaPlugin plugin, EquipmentType... types) {
        this.name = name == null ? getClass().getSimpleName() : name;
        this.plugin = plugin;
        this.types = types.length == 0 ? new EquipmentType[]{EquipmentType.ALL} : types;

        List<String> priorityList = Config.getConfig().getStringList(Config.CONDITION_PRIORITY);
        for (int i = 0; i < priorityList.size(); i++) {
            String[] split = priorityList.get(i).split("#");
            if (split[0].equals(getName())) {
                this.priority = split.length > 1 && !split[1].equals(getPlugin().getName()) ? -1 : i;
                break;
            }
        }
    }

    /**
     * 设置优先级
     *
     * @param priority int
     */
    SubCondition setPriority(int priority) {
        this.priority = priority;
        return this;
    }

    /**
     * 判断物品是否符合条件
     *
     * @param entity LivingEntity
     * @param item   ItemStack
     * @param lore   String
     * @return SXConditionReturnType
     */
    public abstract boolean determine(LivingEntity entity, ItemStack item, String lore);

    /**
     * 判断条件类型
     *
     * @param type 目标类型
     * @return boolean
     */
    public final boolean containsType(EquipmentType type) {
        return type != null && Arrays.stream(this.types).anyMatch(updateType -> updateType.equals(EquipmentType.ALL) || updateType.equals(type));
    }

    /**
     * 注册条件方法
     * 优先级需在SX-Attribute/Config.yml 设定
     */
    public void registerCondition() {
        if (getPlugin() == null) {
            SXAttribute.getInst().getLogger().warning("Condition >> [NULL|" + getName() + "] Null Plugin!");
        } else if (this.getPriority() < 0) {
            SXAttribute.getInst().getLogger().warning("Condition >> Disable [" + getPlugin().getName() + "|" + getName() + "] !");
        } else if (Bukkit.getPluginManager().getPlugin("SX-Attribute").isEnabled()) {
            SXAttribute.getInst().getLogger().warning("Condition >> SXAttribute is Enabled , Unable to register [" + getPlugin().getName() + "|" + getName() + "] !");
        } else {
            int index = IntStream.range(0, conditions.size()).filter(i -> conditions.get(i).priority == this.getPriority()).findFirst().orElse(-1);
            if (index < 0) {
                conditions.add(this);
                SXAttribute.getInst().getLogger().info("Condition >> Register [" + getPlugin().getName() + "|" + getName() + "] To Priority " + getPriority() + " !");
            } else {
                SubCondition sub = conditions.set(index, this);
                SXAttribute.getInst().getLogger().info("Condition >> The [" + getPlugin().getName() + "|" + getName() + "] Cover To [" + sub.getPlugin().getName() + "|" + sub.getName() + "] !");
            }
        }
    }

    /**
     * 条件注册成功后启动时执行的方法
     */
    public void onEnable() {

    }

    /**
     * 条件注册成功后关闭时执行的方法
     */
    public void onDisable() {

    }

    @Override
    public int compareTo(SubCondition sub) {
        return Integer.compare(priority, sub.priority);
    }

    /**
     * 获取物品名称
     *
     * @param item ItemStack
     * @return String
     */
    public static String getItemName(ItemStack item) {
        return item == null ? "N/A" : item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : item.getType().name();
    }

    /**
     * 获取物品等级
     *
     * @param item ItemStack
     * @return int 没有则为-1
     */
    public static int getItemLevel(ItemStack item) {
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasLore()) {
            return item.getItemMeta().getLore().stream().filter(lore -> lore.contains(Config.getConfig().getString(Config.NAME_LIMIT_LEVEL))).findFirst().map(lore -> Integer.valueOf(getNumber(lore).replace(".", ""))).orElse(-1);
        }
        return -1;
    }

    /**
     * 获取属性值
     *
     * @param lore String
     * @return String
     */
    public static String getNumber(String lore) {
        String str = lore.replaceAll("§\\d|[^-.\\d]", "");
        return str.length() == 0 ? "0" : str;
    }


    /**
     * 获取当前耐久值
     *
     * @param lore String
     * @return int
     */
    public static int getDurability(String lore) {
        return lore.split("/").length > 1 ? Integer.valueOf(lore.replaceAll("§\\d", "").split("/")[0].replaceAll("[^\\d]", "")) : 0;
    }

    /**
     * 获取最大耐久值
     *
     * @param lore String
     * @return int
     */
    public static int getMaxDurability(String lore) {
        return lore.contains("/") && lore.split("/").length > 1 ? Integer.valueOf(lore.replaceAll("§\\d", "").split("/")[1].replaceAll("[^\\d]", "")) : 0;
    }

    /**
     * 获取物品是否为无限耐久
     *
     * @param meta ItemMeta
     * @return boolean
     */
    public static boolean isUnbreakable(ItemMeta meta) {
        return ItemUtil.getInst().isUnbreakable(meta);
    }
}