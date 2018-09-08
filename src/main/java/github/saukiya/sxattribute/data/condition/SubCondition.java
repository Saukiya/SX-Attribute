package github.saukiya.sxattribute.data.condition;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.util.Config;
import github.saukiya.sxattribute.util.Message;
import github.saukiya.sxlevel.SXLevel;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 条件抽象类
 *
 * @author Saukiya
 */
public abstract class SubCondition {

    static final ConditionMap conditionMap = new ConditionMap();

    private final String name;

    private JavaPlugin plugin = null;

    private SXConditionType[] updateTypes = new SXConditionType[]{SXConditionType.ALL};

    /**
     * 实现一个条件类
     *
     * @param name String 条件名
     * @param type SXConditionType[] 条件类型
     */
    public SubCondition(String name, SXConditionType... type) {
        this.name = name;
        this.updateTypes = type;
    }

    /**
     * 实现一个条件类
     *
     * @param name String 条件名
     */
    public SubCondition(String name) {
        this.name = name;
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
     * 获取生物等级
     *
     * @param entity LivingEntity
     * @return int 非玩家则为10000
     */
    public static int getLevel(LivingEntity entity) {
        return entity instanceof Player ? SXAttribute.isSxLevel() ? SXLevel.getApi().getPlayerData((Player) entity).getLevel() : ((Player) entity).getLevel() : 10000;
    }

    /**
     * 获取属性值
     *
     * @param lore String
     * @return String
     */
    public static String getNumber(String lore) {
        String str = lore.replaceAll("§+[a-z0-9]", "").replaceAll("[^-0-9.]", "");
        return str.length() == 0 ? "0" : str;
    }


    /**
     * 获取当前耐久值
     *
     * @param lore String
     * @return int
     */
    public static int getDurability(String lore) {
        return lore.contains("/") && lore.split("/").length > 1 ? Integer.valueOf(lore.replaceAll("§+[0-9]", "").split("/")[0].replaceAll("[^0-9]", "")) : 0;
    }

    /**
     * 获取最大耐久值
     *
     * @param lore String
     * @return int
     */
    public static int getMaxDurability(String lore) {
        return lore.contains("/") && lore.split("/").length > 1 ? Integer.valueOf(lore.replaceAll("§+[0-9]", "").split("/")[1].replaceAll("[^0-9]", "")) : 0;
    }

    /**
     * 获取物品是否为无限耐久
     *
     * @param meta ItemMeta
     * @return boolean
     */
    public static boolean getUnbreakable(ItemMeta meta) {
        return SXAttribute.getVersionSplit()[1] >= 11 ? meta.isUnbreakable() : meta.spigot().isUnbreakable();
    }

    /**
     * 获取条件名
     *
     * @return String
     */
    public final String getName() {
        return name;
    }

    /**
     * 获取类型
     *
     * @return SXConditionType[]
     */
    public final SXConditionType[] getType() {
        return updateTypes.clone();
    }

    /**
     * 判断物品是否符合条件
     *
     * @param entity LivingEntity
     * @param item   ItemStack
     * @param lore   String
     * @return SXConditionReturnType
     */
    public abstract SXConditionReturnType determine(LivingEntity entity, ItemStack item, String lore);

    /**
     * 判断条件类型
     *
     * @param type        目标类型
     * @param strContains 是否判断字符串
     * @return boolean
     */
    public final boolean containsType(SXConditionType type, boolean strContains) {
        return type != null && Arrays.stream(this.updateTypes).anyMatch(updateType -> updateType.equals(SXConditionType.ALL) || (strContains ? type.getName().contains(updateType.getName()) : updateType.equals(type)));
    }

    /**
     * 注册条件方法
     * 优先级需在SX-Attribute/Config.yml 设定
     *
     * @param plugin JavaPlugin
     */
    public final void registerCondition(JavaPlugin plugin) {
        if (plugin == null) {
            Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§cCondition >> §4" + this.getName() + " §cNull Plugin!");
        } else if (this.getPriority() < 0) {
            Bukkit.getConsoleSender().sendMessage("[" + plugin.getName() + "] §8Condition >> Disable §4" + this.getName() + " §8!");
        } else if (SXAttribute.isPluginEnabled()) {
            Bukkit.getConsoleSender().sendMessage("[" + plugin.getName() + "] §cCondition >> §cSXAttribute is Enabled §4" + this.getName() + "§r !");
        } else {
            this.plugin = plugin;
            SubCondition condition = conditionMap.put(this.getPriority(), this);
            if (condition == null) {
                Bukkit.getConsoleSender().sendMessage("[" + plugin.getName() + "] Condition >> Register §c" + this.getName() + " §rTo Priority §c" + this.getPriority() + " §r!");
            } else {
                Bukkit.getConsoleSender().sendMessage("[" + plugin.getName() + "] Condition >> §cThe §4" + this.getName() + " §cCover To §4" + condition.getName() + " §7[§c"+condition.getPlugin().getName()+"§7]§r !");
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

    /**
     * 为条件添加介绍说明
     *
     * @return List
     */
    public List<String> introduction() {
        return new ArrayList<>();
    }

    /**
     * 获取优先级
     *
     * @return int 优先值 -1为关闭
     */
    public final int getPriority() {
        return Config.getConfig().getInt("ConditionPriority." + getName(), -1);
    }

    /**
     * 获取注册该条件的插件
     *
     * @return JavaPlugin
     */
    public final JavaPlugin getPlugin() {
        return plugin;
    }
}
