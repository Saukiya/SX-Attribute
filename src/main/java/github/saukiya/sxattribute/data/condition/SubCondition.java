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
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

/**
 * 条件抽象类
 *
 * @author Saukiya
 */
public abstract class SubCondition {

    static final ConditionMap conditionMap = new ConditionMap();

    @Getter
    private final String name;

    @Getter
    private JavaPlugin plugin = null;

    private SXConditionType[] updateTypes = new SXConditionType[]{SXConditionType.ALL};

    /**
     * 实现一个条件类
     *
     * @param name 名称
     * @param type 更新类型
     */
    public SubCondition(String name, SXConditionType... type) {
        this.name = name;
        this.updateTypes = type;
    }

    /**
     * 实现一个条件类
     *
     * @param name 名称
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
        if (item == null) {
            return "N/A";
        }
        if (item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        } else {
            return item.getType().name();
        }
    }

    /**
     * 获取lore内的中文 (职业)
     *
     * @param lore String
     * @return String
     */
    public static String getText(String lore) {
        String str = lore.replaceAll("[^\u0391-\uFFE5]", "");
        if (lore.contains(":") || lore.contains("：")) {
            str = lore.replace("：", ":");
            str = str.replace(str.split(":")[0] + ":", "").replaceAll("[^\u0391-\uFFE5]", "");
        }
        return str;
    }

    /**
     * 获取物品等级
     *
     * @param item 物品
     * @return 没有则为-1
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
     * @return int
     */
    public static int getLevel(LivingEntity entity) {
        return entity instanceof Player ? SXAttribute.isSxLevel() ? SXLevel.getApi().getPlayerData((Player) entity).getLevel() : ((Player) entity).getLevel() : 10000;
    }

    /**
     * 获取lore内的时间
     *
     * @param lore String
     * @return String
     */
    protected static String getTime(String lore) {
        String str = lore.replace(Config.getConfig().getString(Config.FORMAT_EXPIRY_TIME), "").replaceAll("§+[a-z0-9]", "");
        if (str.contains(":") || str.contains("：")) {
            str = str.replace("：", ":");
            str = str.replace(str.split(":")[0] + ":", "");
            return str;
        }
        return str;
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
     * 获取类型
     *
     * @return SXConditionType[]
     */
    public SXConditionType[] getType() {
        return updateTypes.clone();
    }

    /**
     * 判断物品是否符合条件
     *
     * @param entity LivingEntity
     * @param item   ItemStack
     * @param lore   String
     * @return boolean
     */
    public abstract boolean determine(LivingEntity entity, ItemStack item, String lore);

    /**
     * 判断类型
     *
     * @param type        目标类型
     * @param strContains 是否判断字符串
     * @return boolean
     */
    public boolean containsType(SXConditionType type, boolean strContains) {
        return type != null && Arrays.stream(this.updateTypes).anyMatch(updateType -> updateType.equals(SXConditionType.ALL) || (strContains ? type.getName().contains(updateType.getName()) : updateType.equals(type)));
    }

    /**
     * 注册属性方法
     */
    public final void registerCondition(JavaPlugin plugin) {
        if (plugin == null) {
            Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§cSubCondition >> §4" + this.getName() + " §cNull Plugin!");
            return;
        } else if (this.getPriority() < 0) {
            Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§8SubCondition >> Disable §4" + this.getName() + " §8!");
            return;
        }
        this.plugin = plugin;
        if (!conditionMap.containsKey(this.getPriority())) {
            Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "SubCondition >> Register §c" + this.getName() + " §rTo Priority §c" + this.getPriority() + " §r!");
        } else {
            Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "SubCondition >> §cThe §4" + this.getName() + " §cCover To §4" + conditionMap.get(this.getPriority()).getName() + "§c !");
        }
        conditionMap.put(this.getPriority(), this);
    }

    /**
     * 获取优先级
     *
     * @return int 优先值 -1为关闭
     */
    public int getPriority() {
        return Config.getConfig().getInt("ConditionPriority." + getName(), -1);
    }
}
