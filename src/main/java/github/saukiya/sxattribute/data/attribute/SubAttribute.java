package github.saukiya.sxattribute.data.attribute;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.eventdata.EventData;
import github.saukiya.sxattribute.util.Config;
import github.saukiya.sxattribute.util.Message;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

/**
 * 属性抽象类
 *
 * @author Saukiya
 */
public abstract class SubAttribute implements Comparable<SubAttribute>, Message.Tool {

    @Getter
    private final static List<SubAttribute> attributes = new ArrayList<>();

    // TODO 预计去除
    @Setter
    @Getter
    private static String firstPerson;

    @Getter
    private final String name;

    @Getter
    private final JavaPlugin plugin;

    @Getter
    private final AttributeType[] types;

    @Getter
    @Setter
    private int length;

    @Getter
    private int priority = -1;

    @Getter
    private final File configFile;

    @Getter
    private YamlConfiguration config;

    /**
     * 实现一个属性类
     *
     * @param length int 数组长度
     * @param types  SXAttributeType 属性类型
     */
    public SubAttribute(JavaPlugin plugin, int length, AttributeType... types) {
        this(null, plugin, length, types);
    }

    public SubAttribute(String name, JavaPlugin plugin, int length, AttributeType... types) {
        this.name = name == null ? getClass().getSimpleName() : name;
        this.plugin = plugin;
        this.length = length;
        this.types = types.length == 0 ? new AttributeType[]{AttributeType.OTHER} : types;
        this.configFile = new File(SXAttribute.getInst().getDataFolder(), "Attribute" + File.separator + getPlugin().getName() + File.separator + getName() + ".yml");

        List<String> priorityList = Config.getConfig().getStringList(Config.ATTRIBUTE_PRIORITY);
        for (int i = 0; i < priorityList.size(); i++) {
            String[] split = priorityList.get(i).split("#");
            if (split[0].equals(getName())) {
                this.priority = split.length > 1 && !split[1].equals(getPlugin().getName()) ? -1 : i;
                break;
            }
        }
    }

    /**
     * 属性正常启动后
     * 加载配置文件
     *
     * @return this
     */
    public final SubAttribute loadConfig() {
        if (!getConfigFile().exists()) {
            YamlConfiguration yaml = defaultConfig(new YamlConfiguration());
            if (yaml != null) {
                config = yaml;
                saveConfig();
            }
        } else {
            config = YamlConfiguration.loadConfiguration(getConfigFile());
        }
        return this;
    }

    /**
     * 编写默认配置文件
     * 返回null则不需要配置文件
     *
     * @param config new YamlConfiguration
     * @return Yaml
     */
    protected YamlConfiguration defaultConfig(YamlConfiguration config) {
        return null;
    }

    /**
     * 保存配置信息
     */
    public void saveConfig() {
        try {
            config.save(getConfigFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置优先级
     *
     * @param priority int
     */
    SubAttribute setPriority(int priority) {
        this.priority = priority;
        return this;
    }

    /**
     * 判断属性类型
     *
     * @param attributeType SXAttributeType
     * @return boolean
     */
    public final boolean containsType(AttributeType attributeType) {
        return Arrays.asList(types).contains(attributeType);
    }

    /**
     * 注册属性方法-实例化就注册【脏话】
     * <p>
     * 优先级需在SX-Attribute/Config.yml 设定
     */
    public void registerAttribute() {
        if (getPlugin() == null) {
            SXAttribute.getInst().getLogger().warning("Attribute >>  [NULL|" + getName() + "] Null Plugin!");
        } else if (getPriority() < 0) {
            SXAttribute.getInst().getLogger().warning("Attribute >> Disable [" + getPlugin().getName() + "|" + getName() + "] !");
        } else if (Bukkit.getPluginManager().getPlugin("SX-Attribute").isEnabled()) {
            SXAttribute.getInst().getLogger().warning("Attribute >> SXAttribute is Enabled , Unable to register [" + getPlugin().getName() + "|" + getName() + "] !");
        } else {
            int index = IntStream.range(0, attributes.size()).filter(i -> attributes.get(i).priority == this.getPriority()).findFirst().orElse(-1);
            if (index < 0) {
                attributes.add(this);
                SXAttribute.getInst().getLogger().config("Attribute >> Register [" + getPlugin().getName() + "|" + getName() + "] To Priority " + this.getPriority() + " !");
            } else {
                SubAttribute sub = attributes.set(index, this);
                SXAttribute.getInst().getLogger().info("Attribute >> The [" + getPlugin().getName() + "|" + getName() + "] Cover To [" + sub.getPlugin().getName() + "|" + sub.getName() + "] !");
            }
        }
    }

    /**
     * 启动时执行的方法
     */
    public void onEnable() {
    }

    /**
     * 重载时执行的方法
     */
    public void onReLoad() {
    }

    /**
     * 关闭时执行的方法
     */
    public void onDisable() {
    }

    /**
     * 根据属性枚举执行相应方法
     * 伤害事件
     *
     * @param values    属性数据
     * @param eventData 事件数据
     */
    public abstract void eventMethod(double[] values, EventData eventData);

    /**
     * 获取placeholder变量
     *
     * @param values 属性数据
     * @param string String
     * @param player Player
     * @return Object / null
     */
    public abstract Object getPlaceholder(double[] values, LivingEntity player, String string);

    public Double getValue(double[] values, LivingEntity player, String string) {
        String placeholder = getPlaceholder(values, player, string).toString();
        if (placeholder.contains(" - ")) {
            String[] split = placeholder.split(" - ");
            return SXAttribute.getRandom().nextDouble() * (Double.parseDouble(split[1]) - Double.parseDouble(split[0])) + Double.parseDouble(split[0]);
        }
        return Double.parseDouble(placeholder);
    }

    /**
     * 获取placeholder变量列表
     *
     * @return List
     */
    public abstract List<String> getPlaceholders();

    /**
     * 读取属性方法
     *
     * @param values 属性数据
     * @param lore   物品lore
     */
    public abstract void loadAttribute(double[] values, String lore);

    /**
     * 纠正属性值
     *
     * @param values 属性数据
     */
    public void correct(double[] values) {
        int bound = values.length;
        for (int i = 0; i < bound; i++) {
            values[i] = Math.max(values[i], 0D);
        }
    }

    /**
     * 计算战力值
     *
     * @param values 属性数据
     * @return 战力值
     */
    public double calculationCombatPower(double[] values) {
        return 0D;
    }

    @Override
    public int compareTo(SubAttribute sub) {
        return Integer.compare(priority, sub.priority);
    }


    public static DecimalFormat getDf() {
        return SXAttribute.getDf();
    }

    /**
     * 判断几率
     *
     * @param value double
     * @return boolean
     */
    public static boolean probability(double value) {
        boolean b = value > 0 && value / 100D > SXAttribute.getRandom().nextDouble();
        System.out.println("开始随机计算：" + value + " " +b);
        return b;
    }

    /**
     * 获取属性值
     *
     * @param lore String
     * @return String
     */
    public static double getNumber(String lore) {
        String str = lore.replaceAll("§+[a-z0-9]", "").replaceAll("[^-0-9.]", "");
        return str.isEmpty() || str.replaceAll("[^.]", "").length() > 1 ? 0D : Double.parseDouble(str);
    }

    /**
     * 获取属性
     *
     * @param attributeName String
     * @return SubAttribute / null
     */
    public static SubAttribute getSubAttribute(String attributeName) {
        for (SubAttribute attribute : SubAttribute.getAttributes()) {
            if (attribute.getName().equals(attributeName)) {
                return attribute;
            }
        }
        return null;
    }
}
