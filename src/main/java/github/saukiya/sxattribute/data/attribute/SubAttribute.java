package github.saukiya.sxattribute.data.attribute;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.eventdata.EventData;
import github.saukiya.sxattribute.util.Config;
import github.saukiya.sxattribute.util.Message;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 属性抽象类
 *
 * @author Saukiya
 */
public abstract class SubAttribute {

    static final AttributeMap attributeMap = new AttributeMap();

    @Setter
    @Getter
    private static String firstPerson;

    @Getter
    private final String name;

    private final SXAttributeType[] attributeTypes;

    private final double[] doubles;

    private JavaPlugin plugin = null;

    @Getter
    private DecimalFormat df = SXAttribute.getDf();


    /**
     * 实现一个属性类
     *
     * @param name           String 属性名
     * @param doublesLength  int 数组长度
     * @param attributeTypes SXAttributeType 属性类型
     */
    public SubAttribute(String name, int doublesLength, SXAttributeType... attributeTypes) {
        this.name = name;
        this.attributeTypes = attributeTypes;
        this.doubles = new double[doublesLength];
    }

    /**
     * 判断几率
     *
     * @param d double
     * @return boolean
     */
    public static boolean probability(double d) {
        return d > 0 && d / 100D > SXAttribute.getRandom().nextDouble();
    }


    /**
     * 获取属性值
     *
     * @param lore String
     * @return String
     */
    public static String getNumber(String lore) {
        String str = lore.replaceAll("§+[a-z0-9]", "").replaceAll("[^-0-9.]", "");
        return str.length() == 0 || str.replaceAll("[^.]", "").length() > 1 ? "0" : str;
    }

    /**
     * 获取类型
     *
     * @return SXAttributeType[]
     */
    public final SXAttributeType[] getType() {
        return attributeTypes.clone();
    }

    /**
     * 判断属性类型 以及是否有效
     *
     * @param attributeType SXAttributeType
     * @return boolean
     */
    public final boolean containsType(SXAttributeType attributeType) {
        return Arrays.stream(attributeTypes).anyMatch(type -> type.equals(attributeType));
    }

    /**
     * 注册属性方法
     * 优先级需在SX-Attribute/Config.yml 设定
     *
     * @param plugin JavaPlugin
     */
    public final void registerAttribute(JavaPlugin plugin) {
        if (plugin == null) {
            Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§cAttribute >> §4" + this.getName() + " §cNull Plugin!");
        } else if (this.getPriority() < 0) {
            Bukkit.getConsoleSender().sendMessage("[" + plugin.getName() + "] §8Attribute >> §4" + this.getName() + " §8Disable!");
        } else if (attributeTypes.length == 0) {
            Bukkit.getConsoleSender().sendMessage("[" + plugin.getName() + "] §cAttribute >> §4" + this.getName() + " §cNo SXAttributeType!");
        } else if (SXAttribute.isPluginEnabled()) {
            Bukkit.getConsoleSender().sendMessage("[" + plugin.getName() + "] §cAttribute >> §cSXAttribute is Enabled §4" + this.getName() + "§r !");
        } else {
            this.plugin = plugin;
            SubAttribute attribute = attributeMap.put(this.getPriority(), this);
            if (attribute == null) {
                Bukkit.getConsoleSender().sendMessage("[" + plugin.getName() + "] Attribute >> Register §c" + this.getName() + " §rTo Priority §c" + this.getPriority() + " §r!");
            } else {
                Bukkit.getConsoleSender().sendMessage("[" + plugin.getName() + "] Attribute >> The §c" + this.getName() + " §rCover To §c" + attribute.getName() + " §7[§c" + attribute.getPlugin() + "§7]§r !");
            }
            attributeMap.put(this.getPriority(), this);
        }

    }

    /**
     * 属性注册启动时执行的方法
     */
    public void onEnable() {

    }

    /**
     * 属性关闭时执行的方法
     */
    public void onDisable() {

    }

    /**
     * 为属性添加介绍说明
     *
     * @return List
     */
    public List<String> introduction() {
        return new ArrayList<>();
    }

    /**
     * 实例化一个属性
     *
     * @return SubAttribute
     */
    SubAttribute newAttribute() {
        try {
            return this.getClass().newInstance().setPlugin(plugin);
        } catch (InstantiationException | IllegalAccessException e) {
            Bukkit.getConsoleSender().sendMessage("§r[§c" + plugin.getName() + "§r] Attribute >> §4" + this.getName() + " §cConstructors Error §r!");
            return null;
        }
    }

    /**
     * 根据属性枚举执行相应方法
     * 伤害事件
     *
     * @param eventData 事件数据
     */
    public abstract void eventMethod(EventData eventData);

    /**
     * 获取placeholder变量
     *
     * @param string String
     * @param player Player
     * @return String / null
     */
    public abstract String getPlaceholder(Player player, String string);

    /**
     * 获取placeholder变量列表
     *
     * @return List
     */
    public abstract List<String> getPlaceholders();

    /**
     * 获取优先级
     *
     * @return int 优先值 -1为关闭
     */
    public final int getPriority() {
        return Config.getConfig().getInt("AttributePriority." + getName(), -1);
    }

    /**
     * 获取准确的属性
     *
     * @return double[] 属性值组
     */
    public final double[] getAttributes() {
        return doubles;
    }

    /**
     * 设置属性
     *
     * @param doubles 属性
     */
    public final void setAttributes(Double... doubles) {
        IntStream.range(0, this.doubles.length).forEach(i -> this.doubles[i] = doubles[i]);
    }

    /**
     * 增加属性
     *
     * @param doubles 属性组
     */
    public final void addAttribute(double[] doubles) {
        IntStream.range(0, this.doubles.length).forEach(i -> this.doubles[i] += doubles[i]);
    }

    /**
     * 读取属性方法
     *
     * @param lore 物品lore
     * @return boolean
     */
    public abstract boolean loadAttribute(String lore);

    /**
     * 从字符串中读取属性
     * 默认为:
     * 属性名#数值/数值/数值
     * 属性名#数值
     *
     * @param attributeString 字符串
     */
    public final void loadFromString(String attributeString) {
        String[] args1 = attributeString.split("#");
        if (args1[0].equals(getName())) {
            List<String> list = args1[1].contains("/") ? Arrays.asList(args1[1].split("/")) : Collections.singletonList(args1[1]);
            setAttributes(list.stream().filter(s -> s.length() > 0).map(Double::valueOf).toArray(Double[]::new));
        }
    }

    /**
     * 保存为字符串
     * 默认为:
     * 属性名#数值/数值/数值
     * 属性名#数值
     *
     * @return String
     */
    public final String saveToString() {
        if (Arrays.stream(doubles, 0, doubles.length).anyMatch(aDouble -> aDouble != 0D)) {
            return getName() + "#" + IntStream.range(0, doubles.length).mapToObj(i -> String.valueOf(i == doubles.length - 1 ? doubles[i] : doubles[i] + "/")).collect(Collectors.joining());
        }
        return null;
    }

    /**
     * 纠正属性值
     */
    public void correct() {
        IntStream.range(0, doubles.length).filter(i -> doubles[i] < 0).forEach(i -> doubles[i] = 0D);
    }

    /**
     * 获取战力值
     *
     * @return double
     */
    public abstract double getValue();

    /**
     * 测试复制区域
     *
     * @return SubAttribute
     */
    @Override
    protected SubAttribute clone() {
        try {
            return (SubAttribute) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    /**
     * 获取注册该属性的插件
     *
     * @return JavaPlugin
     */
    public final JavaPlugin getPlugin() {
        return plugin;
    }

    SubAttribute setPlugin(JavaPlugin plugin) {
        this.plugin = plugin;
        return this;
    }
}
