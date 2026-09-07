package github.saukiya.sxattribute.util;

import github.saukiya.sxattribute.SXAttribute;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 属性聚合配置加载器。
 * <p>
 * 统管全部属性的开关/默认值/上下限/公式/识别名/战力/面板显示。主清单位于
 * {@code Feature/Attribute/Attributes.yml}，按 Files 顺序合并 definitions 下的分片；运行时仍向
 * 旧属性类提供一个聚合后的 {@link YamlConfiguration}，所以迁移配置路径不会破坏现有读取协议。
 * <p>
 * 取代碎片化的
 * {@code Attribute/SX-Attribute/<Name>.yml}: 属于本插件的属性在 {@link github.saukiya.sxattribute.data.attribute.SubAttribute#loadConfig()}
 * 中把 {@code config()} 数据源重定向到本文件对应节点, 并把各属性 {@code defaultConfig()} 的缺省键
 * 自动播种(仅补缺, 用户已填的键不覆盖)进节点, 首次加载后落盘, 使所有属性的可调值集中可见可改。
 * <p>
 * 数值型原版包装属性 (含 {@code RegistryKey} 的节点) 由此文件完全数据驱动:
 * {@link SXAttribute} 在 onLoad 按 {@code Version} 与实际 Bukkit API 门控注册, 无需一属性一 Java 类。
 *
 * @author Ray_Hughes
 */
public class AttributeConfig {

    private static final String ATTRIBUTES = "Attributes";

    @Getter
    private static YamlConfiguration config;

    private static File file;

    private static File attributeDirectory;

    private static boolean dirty;

    /** 当前聚合是否发现会破坏注册表一致性的错误。 */
    private static boolean loadFailed;

    @Getter
    private static boolean formulaEngine;

    @Getter
    private static boolean autoPanel;

    /** Feature/Attribute 总开关。 */
    @Getter
    private static boolean enabled;

    /**
     * 加载属性主清单与全部分片。
     * <p>
     * 旧版数据目录根部的 Attributes.yml 会被复制为 legacy.yml 并保留 .migrated.bak，
     * 迁移过程不删除用户文件；重复 ID 默认拒绝后出现的定义，避免静默覆盖战斗规则。
     */
    public static void load() {
        YamlConfiguration previous = config;
        attributeDirectory = new File(SXAttribute.getInst().getDataFolder(), "Feature" + File.separator + "Attribute");
        file = new File(attributeDirectory, "Attributes.yml");
        migrateLegacyConfig();
        saveResourceIfMissing("Feature/Attribute/Attributes.yml", file);
        YamlConfiguration manifest = YamlConfiguration.loadConfiguration(file);
        config = new YamlConfiguration();
        loadFailed = false;
        copyValues(manifest, config, null, true);
        String duplicatePolicy = manifest.getString("DuplicatePolicy", "ERROR").toUpperCase();
        for (File definition : resolveDefinitionFiles(manifest.getStringList("Files"))) {
            mergeDefinition(definition, duplicatePolicy);
        }
        if (loadFailed && previous != null) {
            config = previous;
            SXAttribute.getInst().getLogger().severe("Attribute aggregation failed; previous registry configuration retained.");
        }
        dirty = false;
        enabled = config.getBoolean("Enable", true);
        formulaEngine = config.getBoolean("Settings.FormulaEngine", true);
        autoPanel = config.getBoolean("Settings.AutoPanel", true);
    }

    private static void migrateLegacyConfig() {
        File legacy = new File(SXAttribute.getInst().getDataFolder(), "Attributes.yml");
        if (!legacy.exists() || file.exists()) return;
        File definitions = new File(attributeDirectory, "definitions");
        File migrated = new File(definitions, "legacy.yml");
        File backup = new File(SXAttribute.getInst().getDataFolder(), "Attributes.yml.migrated.bak");
        definitions.mkdirs();
        try {
            Files.copy(legacy.toPath(), migrated.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(legacy.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
            saveResourceIfMissing("Feature/Attribute/Attributes.yml", file);
            YamlConfiguration manifest = YamlConfiguration.loadConfiguration(file);
            List<String> files = new ArrayList<>(manifest.getStringList("Files"));
            files.remove("definitions/builtin.yml");
            if (!files.contains("definitions/legacy.yml")) files.add(0, "definitions/legacy.yml");
            manifest.set("Files", files);
            manifest.save(file);
            SXAttribute.getInst().getLogger().info("Migrated Attributes.yml to Feature/Attribute/definitions/legacy.yml");
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to migrate Attributes.yml", exception);
        }
    }

    private static void saveResourceIfMissing(String resourcePath, File target) {
        if (target.exists()) return;
        File parent = target.getParentFile();
        if (parent != null) parent.mkdirs();
        SXAttribute.getInst().saveResource(resourcePath, false);
    }

    private static List<File> resolveDefinitionFiles(List<String> entries) {
        List<File> files = new ArrayList<>();
        for (String entry : entries) {
            if (entry.endsWith("/*.yml")) {
                File directory = new File(attributeDirectory, entry.substring(0, entry.length() - "/*.yml".length()));
                File[] children = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".yml"));
                if (children != null) {
                    List<File> sorted = new ArrayList<>();
                    Collections.addAll(sorted, children);
                    sorted.sort(Comparator.comparing(File::getName));
                    files.addAll(sorted);
                }
                continue;
            }
            File definition = new File(attributeDirectory, entry.replace('/', File.separatorChar));
            if (!definition.exists()) {
                saveResourceIfMissing("Feature/Attribute/" + entry.replace('\\', '/'), definition);
            }
            files.add(definition);
        }
        return files;
    }

    private static void mergeDefinition(File definition, String duplicatePolicy) {
        if (!definition.exists()) {
            SXAttribute.getInst().getLogger().warning("Attribute definition file not found: " + definition.getPath());
            return;
        }
        YamlConfiguration shard = YamlConfiguration.loadConfiguration(definition);
        ConfigurationSection attributes = shard.getConfigurationSection(ATTRIBUTES);
        if (attributes == null) return;
        for (String id : attributes.getKeys(false)) {
            String targetPath = ATTRIBUTES + "." + id;
            if (config.contains(targetPath) && "ERROR".equals(duplicatePolicy)) {
                SXAttribute.getInst().getLogger().severe("Duplicate attribute id rejected: " + id + " in " + definition.getName());
                loadFailed = true;
                continue;
            }
            config.set(targetPath, null);
            ConfigurationSection source = attributes.getConfigurationSection(id);
            if (source != null) {
                ConfigurationSection target = config.createSection(targetPath);
                copyValues(source, target, null, true);
            }
        }
    }

    private static void copyValues(ConfigurationSection source, ConfigurationSection target, String prefix, boolean overwrite) {
        for (Map.Entry<String, Object> entry : source.getValues(false).entrySet()) {
            String path = prefix == null ? entry.getKey() : prefix + "." + entry.getKey();
            if (entry.getValue() instanceof ConfigurationSection) {
                ConfigurationSection child = target.getConfigurationSection(path);
                if (child == null) child = target.createSection(path);
                copyValues((ConfigurationSection) entry.getValue(), child, null, overwrite);
            } else if (overwrite || !target.contains(path)) {
                target.set(path, entry.getValue());
            }
        }
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
        return enabled && (sec == null || sec.getBoolean("Enable", true));
    }

    /**
     * 判断原版包装属性在当前服务端是否存在，供注册与面板共用同一兼容性边界。
     * <p>
     * Version 是最低版本约束，不能代替实际 API 检测；即使服主调低 Version，旧服也不会因此
     * 获得新的原版能力。普通属性及外部扩展不受此门控影响，开关仍由 isEnabled 单独判断。
     */
    public static boolean isSupported(String name) {
        ConfigurationSection sec = getSection(name);
        if (sec == null || !sec.contains("RegistryKey")) return true;
        // 1.9 以前没有 Bukkit Attribute API，必须在触碰 AttributeUtil 前返回。
        if (!SXAttribute.isHigherVersion() || !SXAttribute.isVersionAtLeast(sec.getString("Version", "1.0"))) {
            return false;
        }
        return AttributeUtil.get(sec.getString("RegistryKey", name.toLowerCase(Locale.ROOT)),
                sec.getString("LegacyName", name.toUpperCase(Locale.ROOT))) != null;
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
