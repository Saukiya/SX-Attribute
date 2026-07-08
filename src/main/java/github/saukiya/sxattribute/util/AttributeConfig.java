package github.saukiya.sxattribute.util;

import github.saukiya.sxattribute.SXAttribute;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

/**
 * 属性中央配置 Attributes.yml 加载器 (单一事实来源)
 * <p>
 * 统管全部属性的开关/默认值/上下限/公式/识别名/战力/面板显示。取代碎片化的
 * {@code Attribute/SX-Attribute/<Name>.yml}: 属于本插件的属性在 {@link github.saukiya.sxattribute.data.attribute.SubAttribute#loadConfig()}
 * 中把 {@code config()} 数据源重定向到本文件对应节点, 并把各属性 {@code defaultConfig()} 的缺省键
 * 自动播种(仅补缺, 用户已填的键不覆盖)进节点, 首次加载后落盘, 使所有属性的可调值集中可见可改。
 * <p>
 * 数值型原版包装属性 (含 {@code RegistryKey} 的节点) 由此文件完全数据驱动:
 * {@link SXAttribute} 在 onLoad 遍历本文件按 {@code Version} 门控实例化注册, 无需一属性一 Java 类。
 *
 * @author Ray_Hughes
 */
public class AttributeConfig {

    private static final String ATTRIBUTES = "Attributes";

    @Getter
    private static YamlConfiguration config;

    private static File file;

    private static boolean dirty;

    @Getter
    private static boolean formulaEngine;

    @Getter
    private static boolean autoPanel;

    /**
     * 加载 Attributes.yml (无文件则从 jar 释放默认种子)
     */
    public static void load() {
        file = new File(SXAttribute.getInst().getDataFolder(), "Attributes.yml");
        if (!file.exists()) {
            SXAttribute.getInst().getLogger().info("Create Attributes.yml");
            SXAttribute.getInst().saveResource("Attributes.yml", true);
        }
        config = YamlConfiguration.loadConfiguration(file);
        dirty = false;
        formulaEngine = config.getBoolean("Settings.FormulaEngine", true);
        autoPanel = config.getBoolean("Settings.AutoPanel", true);
    }

    /**
     * 获取某属性的配置节点 (整段透传, 含任意嵌套子 key)
     *
     * @param name 属性名 (节点键)
     * @return ConfigurationSection, 不存在返回 null
     */
    public static ConfigurationSection getSection(String name) {
        if (config == null) return null;
        return config.getConfigurationSection(ATTRIBUTES + "." + name);
    }

    /**
     * 获取或创建某属性的配置节点 (供播种缺省键)
     *
     * @param name 属性名
     * @return ConfigurationSection, config 未加载返回 null
     */
    public static ConfigurationSection getOrCreateSection(String name) {
        if (config == null) return null;
        ConfigurationSection sec = getSection(name);
        return sec != null ? sec : config.createSection(ATTRIBUTES + "." + name);
    }

    /**
     * @param name 属性名
     * @return 该属性是否已配置节点
     */
    public static boolean has(String name) {
        return getSection(name) != null;
    }

    /**
     * 属性是否启用 (缺省 true; 无节点时也视为启用, 不阻断尚未纳入的属性)
     *
     * @param name 属性名
     * @return 启用返回 true
     */
    public static boolean isEnabled(String name) {
        ConfigurationSection sec = getSection(name);
        return sec == null || sec.getBoolean("Enable", true);
    }

    /**
     * @return Attributes 段下的全部属性节点名
     */
    public static Set<String> attributeNames() {
        if (config == null) return Collections.emptySet();
        ConfigurationSection sec = config.getConfigurationSection(ATTRIBUTES);
        return sec == null ? Collections.emptySet() : sec.getKeys(false);
    }

    /**
     * 标记配置已被播种修改, 待落盘
     */
    public static void markDirty() {
        dirty = true;
    }

    /**
     * 播种后的持久化钩子 —— 有意不落盘。
     * <p>
     * 战斗类的 DiscernName/CombatPower/UpperLimit/Message/List 等缺省值由各属性 defaultConfig()
     * 在 loadConfig() 时<b>仅在内存</b>合并进节点即可生效; 之所以不写回磁盘, 是因为 Bukkit 的
     * {@code YamlConfiguration.save} 会<b>抹除 Attributes.yml 里的注释</b>。为保留文件内详尽的
     * 公式变量/结果说明注释, 这里不做保存。用户如需覆盖某缺省值, 直接在对应节点补写该键即可 (内存合并只补缺不覆盖)。
     */
    public static void saveIfDirty() {
        // no-op: 保留 Attributes.yml 注释, 播种仅存内存
        dirty = false;
    }
}
