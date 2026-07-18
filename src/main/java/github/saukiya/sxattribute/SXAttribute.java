package github.saukiya.sxattribute;

import github.saukiya.sxattribute.api.SXAPI;
import github.saukiya.sxattribute.command.MainCommand;
import github.saukiya.sxattribute.data.PersistentSourceManager;
import github.saukiya.sxattribute.data.RandomStringManager;
import github.saukiya.sxattribute.data.SlotDataManager;
import github.saukiya.sxattribute.data.attribute.AttributeType;
import github.saukiya.sxattribute.data.attribute.SXAttributeManager;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.attribute.sub.attack.*;
import github.saukiya.sxattribute.data.attribute.sub.defence.*;
import github.saukiya.sxattribute.data.attribute.sub.other.EventMessage;
import github.saukiya.sxattribute.data.attribute.sub.other.ExpAddition;
import github.saukiya.sxattribute.data.attribute.sub.other.JSAttribute;
import github.saukiya.sxattribute.data.attribute.sub.update.*;
import github.saukiya.sxattribute.data.condition.SXConditionManager;
import github.saukiya.sxattribute.data.condition.sub.*;
import github.saukiya.sxattribute.data.itemdata.ItemDataManager;
import github.saukiya.sxattribute.data.itemdata.sub.GeneratorImport;
import github.saukiya.sxattribute.data.itemdata.sub.GeneratorSX;
import github.saukiya.sxattribute.feature.attribute.AttributeEngine;
import github.saukiya.sxattribute.feature.equipment.ForgeFeatureManager;
import github.saukiya.sxattribute.feature.source.SourceService;
import github.saukiya.sxattribute.listener.*;
import github.saukiya.sxattribute.util.*;
import github.saukiya.sxattribute.util.hologram.DecentHologramsProvider;
import github.saukiya.sxattribute.util.hologram.HologramProvider;
import github.saukiya.sxattribute.util.hologram.HolographicDisplaysProvider;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.java.JavaPlugin;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * SX-Attribute
 *
 * @author Saukiya
 * <p>
 * 该插件只发布与MCBBS。
 */


public class SXAttribute extends JavaPlugin {

    @Getter
    @Setter
    private static DecimalFormat df = new DecimalFormat("#.##");

    @Getter
    private static int[] versionSplit = new int[3];

    @Getter
    private static Random random = new Random();

    @Getter
    private static SXAttribute inst;

    @Getter
    private static SXAPI api = new SXAPI();

    @Getter
    private static SXAttributeManager attributeManager;

    @Getter
    private static SXConditionManager conditionManager;

    @Getter
    private static RandomStringManager randomStringManager;

    @Getter
    private static ItemDataManager itemDataManager;

    @Getter
    private static SlotDataManager slotDataManager;

    @Getter
    private static NbtUtil nbtUtil;

    @Getter
    private static ListenerHealthChange listenerHealthChange;

    @Getter
    private static boolean placeholder, holographic, vault, rpgInventory, mythicMobs;

    /**
     * 全息显示提供者. 优先 HolographicDisplays, 其次 DecentHolograms, 均无则为 null.
     */
    @Getter
    private static HologramProvider hologramProvider;

    @Getter
    private static MainCommand mainCommand;

    @Getter
    private static PersistentSourceManager persistentSourceManager;

    /** 配置化属性注册表与白名单动作运行时。 */
    @Getter
    private static AttributeEngine attributeEngine;

    /** 统一管理临时及持久化命名源的生命周期服务。 */
    @Getter
    private static SourceService sourceService;

    /** 独立装备成长模块与统一锻造 GUI 注册器。 */
    @Getter
    private static ForgeFeatureManager forgeFeatureManager;

    /**
     * 判断服务器次版本是否 >= 指定值 (跨新旧版本号方案安全)
     * <p>
     * 旧版本号方案为 "1.X.Y", 次版本号 versionSplit[1] 即为 9/13/16...;
     * 高版本新方案(如 26.x) getBukkitVersion() 形如 "26.1.build.2",
     * 解析后 versionSplit = [26, 1, 0], 此时 versionSplit[1] 恒为个位数,
     * 单纯判断 versionSplit[1] >= minMinor 会把新版误判为低版本, 需一并判断主版本号:
     * 主版本 > 1 时(新方案)必然覆盖所有旧特性, 直接返回 true。
     *
     * @param minMinor int 旧方案下要求的最低次版本号 (如 9 表示 1.9)
     * @return boolean 满足则返回 true
     */
    public static boolean isVersionAtLeast(int minMinor) {
        return versionSplit[0] > 1 || versionSplit[1] >= minMinor;
    }

    /**
     * 判断服务器版本是否 >= 指定版本 (精确到补丁号, 跨新旧版本号方案安全)
     * <p>
     * 用于门控按补丁号引入的原版属性 (如 scale 于 1.20.5, camera_distance 于 1.21.6):
     * 逐段比较主版本 -> 次版本 -> 补丁号。高版本新方案(major > 1, 如 26.x)在与 major=1 的
     * 旧特性比较时 versionSplit[0] > major 直接返回 true, 天然覆盖所有旧特性。
     *
     * @param major int 主版本号 (旧方案恒为 1)
     * @param minor int 次版本号
     * @param patch int 补丁号
     * @return boolean 满足则返回 true
     */
    public static boolean isVersionAtLeast(int major, int minor, int patch) {
        if (versionSplit[0] != major) return versionSplit[0] > major;
        if (versionSplit[1] != minor) return versionSplit[1] > minor;
        return versionSplit[2] >= patch;
    }

    /**
     * 解析 "major.minor.patch" 版本串并判断服务器版本是否 >= 之 (缺省段补 0)
     *
     * @param version 版本串 (如 "1.20.5" / "1.21" / "26.2")
     * @return 满足则返回 true
     */
    public static boolean isVersionAtLeast(String version) {
        String[] p = version.split("[.]");
        int major = p.length > 0 ? parseIntSafe(p[0], 1) : 1;
        int minor = p.length > 1 ? parseIntSafe(p[1], 0) : 0;
        int patch = p.length > 2 ? parseIntSafe(p[2], 0) : 0;
        return isVersionAtLeast(major, minor, patch);
    }

    private static int parseIntSafe(String s, int def) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    /**
     * 判断服务器版本是否 >= 1.9 (即支持副手、攻击速度属性、双手物品等特性)
     *
     * @return boolean 版本 >= 1.9 返回 true
     */
    public static boolean isHigherVersion() {
        return isVersionAtLeast(9);
    }

    @SneakyThrows
    @Override
    public void onLoad() {
        super.onLoad();
        inst = this;
        String version = Bukkit.getBukkitVersion().split("-")[0].replace(" ", "");
        String[] strSplit = version.split("[.]");
        // 逐段解析版本号: 遇到非纯数字段(如高版本的 "build")即停止, 并防止写入越界
        // 高版本(26.x)getBukkitVersion() 可能形如 "26.1.build.2", 旧的 Integer.valueOf 全量解析会崩
        for (int i = 0; i < strSplit.length && i < versionSplit.length; i++) {
            if (!strSplit[i].matches("\\d+")) {
                break;
            }
            versionSplit[i] = Integer.parseInt(strSplit[i]);
        }
        SXAttribute.getInst().getLogger().info("ServerVersion: " + version + " -> " + Arrays.toString(versionSplit));
        Config.loadConfig();
        AttributeConfig.load();
        Message.loadMessage();
        mainCommand = new MainCommand();

        new Crit().registerAttribute();
        new Damage().registerAttribute();
        new HitRate().registerAttribute();
        new Ignition().registerAttribute();
        new LifeSteal().registerAttribute();
        new Lightning().registerAttribute();
        new AttackPotion().registerAttribute();
        new Real().registerAttribute();
        new Tearing().registerAttribute();

        new Block().registerAttribute();
        new Defense().registerAttribute();
        new Dodge().registerAttribute();
        new Reflection().registerAttribute();
        new Toughness().registerAttribute();

        new EventMessage().registerAttribute();
        new ExpAddition().registerAttribute();
        new HealthRegen().registerAttribute();

        new Health().registerAttribute();
        new WalkSpeed().registerAttribute();
        if (SXAttribute.isHigherVersion()) {
            new AttackSpeed().registerAttribute();
        }
        new Command().registerAttribute();

        // 原版属性包装 (数据驱动): 遍历 Feature/Attribute 聚合视图中含 RegistryKey 的数值型节点,
        // 按 Version 精确门控(低版本自动跳过), 实例化 VanillaUpdateAttribute 注册。
        // 属性在当前版本不存在时 AttributeUtil 返回 null 二次降级, 不报错。
        for (String attributeName : AttributeConfig.attributeNames()) {
            ConfigurationSection sec = AttributeConfig.getSection(attributeName);
            if (sec == null || !sec.contains("RegistryKey")) {
                continue;
            }
            if (isVersionAtLeast(sec.getString("Version", "1.0"))) {
                new VanillaUpdateAttribute(attributeName, sec).registerAttribute();
            }
        }

        loadJavaScriptAttributes();

        if (SXAttribute.isHigherVersion()) {
            new MainHand().registerCondition();
            new OffHand().registerCondition();
        }
        new Hand().registerCondition();
        new LimitLevel().registerCondition();
        new Role().registerCondition();
        new ExpiryTime().registerCondition();
        new Durability().registerCondition();

        ItemDataManager.registerGenerator(new GeneratorImport());
        ItemDataManager.registerGenerator(new GeneratorSX());
    }

    /**
     * 逐文件加载 JavaScript 属性。
     * <p>
     * JavaScript 属于可选扩展边界：引擎缺失、适配失败、文件语法错误或构造数据非法时，只跳过对应
     * 脚本或整个 JS 子系统，不允许异常越过本方法影响原生属性和插件生命周期。
     */
    private void loadJavaScriptAttributes() {
        File directory = new File(getDataFolder(), "Attribute" + File.separator + "JavaScript");
        try {
            if (!directory.exists() && SXAttribute.isHigherVersion()) {
                saveResource("Attribute/JavaScript/JSAttribute.js", true);
                saveResource("Attribute/SX-Attribute/JSAttribute_JS.yml", true);
            }
            if (!directory.isDirectory()) return;
            if (createJavaScriptEngine() == null) {
                getLogger().warning("JavaScript attribute subsystem disabled: no JavaScript engine is available.");
                return;
            }
            Class<?> staticClass = Class.forName(System.getProperty("java.class.version").startsWith("52")
                    ? "jdk.internal.dynalink.beans.StaticClass" : "jdk.dynalink.beans.StaticClass");
            Method forClass = staticClass.getMethod("forClass", Class.class);
            Object arrays = forClass.invoke(null, Arrays.class);
            Object attributeType = forClass.invoke(null, AttributeType.class);
            Object pluginClass = forClass.invoke(null, SXAttribute.class);
            Object schedulerClass = forClass.invoke(null, FoliaScheduler.class);
            Object bukkitClass = forClass.invoke(null, Bukkit.class);
            File[] files = directory.listFiles((parent, name) -> name.toLowerCase().endsWith(".js"));
            if (files == null) return;
            Arrays.sort(files, (left, right) -> left.getName().compareToIgnoreCase(right.getName()));
            for (File file : files) {
                loadJavaScriptAttribute(file, arrays, attributeType, pluginClass, schedulerClass, bukkitClass);
            }
        } catch (Throwable exception) {
            rethrowFatalJavaScriptFailure(exception);
            logJavaScriptFailure("subsystem initialization", null, exception);
        }
    }

    /** 单个文件拥有独立引擎和异常边界，坏文件不会污染其它脚本的全局变量。 */
    private void loadJavaScriptAttribute(File file, Object arrays, Object attributeType, Object pluginClass,
                                         Object schedulerClass, Object bukkitClass) {
        try {
            ScriptEngine engine = createJavaScriptEngine();
            if (engine == null) throw new IllegalStateException("JavaScript engine disappeared during loading");
            engine.put("Arrays", arrays);
            engine.put("SXAttributeType", attributeType);
            engine.put("SXAttribute", pluginClass);
            engine.put("FoliaScheduler", schedulerClass);
            engine.put("Bukkit", bukkitClass);
            engine.put("API", api);
            // 内置脚本不能自行 new ScriptEngineManager：Java 21 的插件类加载隔离会让它再次得到 null。
            engine.put("SXAEngine", engine);
            String source = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            String migratedSource = migrateBuiltInJavaScriptEngine(file, source);
            try (StringReader reader = new StringReader(migratedSource)) {
                engine.eval(reader);
            }
            new JSAttribute(file.getName().substring(0, file.getName().length() - 3), engine).registerAttribute();
        } catch (Throwable exception) {
            rethrowFatalJavaScriptFailure(exception);
            logJavaScriptFailure("file loading", file, exception);
        }
    }

    /**
     * 迁移已落盘的旧版内置 JSAttribute 引擎声明。
     * <p>
     * 只处理固定文件名和固定原始语句，避免改写服主自行创建或已经定制的其它 JavaScript 属性。
     */
    private String migrateBuiltInJavaScriptEngine(File file, String source) throws Exception {
        if (!"JSAttribute.js".equals(file.getName())) return source;
        String legacy = "engine: jsManager.getEngineByName(\"JavaScript\"),";
        if (!source.contains(legacy)) return source;
        String migrated = source.replace(legacy, "engine: SXAEngine,");
        Files.write(file.toPath(), migrated.getBytes(StandardCharsets.UTF_8));
        getLogger().info("Migrated JSAttribute.js to the isolated JavaScript engine.");
        return migrated;
    }

    /**
     * 从当前插件、线程上下文及 SX-Item 的类加载器发现 JavaScript 引擎。
     * <p>
     * Java 15+ 已移除内置 Nashorn，而 SX-Item 会按需加载 nashorn-core；在 Bukkit 插件隔离环境中，
     * 默认 {@link ScriptEngineManager} 看不到另一个插件的服务提供者，因此必须显式使用其类加载器。
     */
    private ScriptEngine createJavaScriptEngine() {
        Set<ClassLoader> loaders = new LinkedHashSet<>();
        loaders.add(getClass().getClassLoader());
        loaders.add(Thread.currentThread().getContextClassLoader());
        if (Bukkit.getPluginManager().getPlugin("SX-Item") != null) {
            loaders.add(Bukkit.getPluginManager().getPlugin("SX-Item").getClass().getClassLoader());
        }
        for (ClassLoader loader : loaders) {
            if (loader == null) continue;
            ScriptEngine engine = findJavaScriptEngine(loader);
            if (engine != null) return engine;
        }
        return null;
    }

    private ScriptEngine findJavaScriptEngine(ClassLoader loader) {
        try {
            ScriptEngineManager manager = new ScriptEngineManager(loader);
            for (String name : Arrays.asList("JavaScript", "javascript", "nashorn", "Nashorn")) {
                ScriptEngine engine = manager.getEngineByName(name);
                if (engine != null) return engine;
            }
        } catch (RuntimeException ignored) {
            // 服务描述文件损坏时继续尝试直接实例化工厂，不能让可选脚本影响本体。
        }
        for (String factoryName : Arrays.asList(
                "org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory",
                "jdk.nashorn.api.scripting.NashornScriptEngineFactory")) {
            try {
                Object factory = Class.forName(factoryName, true, loader).getDeclaredConstructor().newInstance();
                Object engine = factory.getClass().getMethod("getScriptEngine").invoke(factory);
                if (engine instanceof ScriptEngine) return (ScriptEngine) engine;
            } catch (ReflectiveOperationException | LinkageError ignored) {
                // 当前类加载器不含该 Nashorn 实现，继续尝试下一个候选。
            }
        }
        return null;
    }

    private void logJavaScriptFailure(String phase, File file, Throwable exception) {
        String target = file == null ? "JavaScript subsystem" : file.getName();
        getLogger().severe(target + " failed during " + phase + " and was isolated: "
                + exception.getClass().getSimpleName() + ": " + exception.getMessage());
    }

    /** JVM 无法安全继续的错误必须继续抛出；普通脚本和类链接错误才属于可隔离扩展故障。 */
    private static void rethrowFatalJavaScriptFailure(Throwable exception) {
        if (exception instanceof VirtualMachineError) throw (VirtualMachineError) exception;
    }

    @Override
    public void onEnable() {
        new Metrics(this, 3147);

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            placeholder = true;
            new Placeholders();
        } else {
            SXAttribute.getInst().getLogger().warning("No Find PlaceholderAPI!");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            try {
                MoneyUtil.setup();
                vault = true;
            } catch (NullPointerException e) {
                SXAttribute.getInst().getLogger().warning("No Find Vault-Economy!");
            }
        } else {
            SXAttribute.getInst().getLogger().warning("No Find Vault!");
        }

        // 全息插件二选一: 优先 HolographicDisplays, 其次 DecentHolograms
        if (Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
            hologramProvider = new HolographicDisplaysProvider();
            holographic = true;
        } else if (Bukkit.getPluginManager().isPluginEnabled("DecentHolograms")) {
            hologramProvider = new DecentHologramsProvider();
            holographic = true;
        } else {
            SXAttribute.getInst().getLogger().warning("No Find HolographicDisplays or DecentHolograms!");
        }

        ListenerMythicMobs.setup();
        mythicMobs = ListenerMythicMobs.getHandler() != null;

        if (Bukkit.getPluginManager().isPluginEnabled("RPGInventory")) {
            rpgInventory = true;
        } else {
            SXAttribute.getInst().getLogger().warning("No Find RPGInventory!");
        }

        nbtUtil = new NbtUtil();

        randomStringManager = new RandomStringManager();
        itemDataManager = new ItemDataManager();
        attributeManager = new SXAttributeManager();
        conditionManager = new SXConditionManager();
        slotDataManager = new SlotDataManager();
        persistentSourceManager = new PersistentSourceManager();
        sourceService = new SourceService();
        attributeEngine = new AttributeEngine();
        forgeFeatureManager = new ForgeFeatureManager();
        listenerHealthChange = new ListenerHealthChange();

        if (Config.getDamageParticleLimit() >= 0) {
            DamageParticlePacketLimiter.register(this);
        }

        if (!Config.getConfig().getString(Config.DAMAGE_EVENT_PRIORITY, "HIGH").equals("HIGH")) {
            for (Method method : ListenerDamage.class.getDeclaredMethods()) {
                if (method.getName().equals("onEntityDamageByEntityEvent")) {
                    try {
                        EventPriority priority = EventPriority.valueOf(Config.getConfig().getString(Config.DAMAGE_EVENT_PRIORITY));
                        EventHandler eventHandler = method.getAnnotation(EventHandler.class);
                        InvocationHandler invHandler = Proxy.getInvocationHandler(eventHandler);
                        Field field = invHandler.getClass().getDeclaredField("memberValues");
                        field.setAccessible(true);
                        Map<String, Object> memberValues = (Map<String, Object>) field.get(invHandler);
                        memberValues.put("priority", EventPriority.LOW);
                        SXAttribute.getInst().getLogger().info("EditDamageEventPriority: " + priority.name());

                    } catch (NoSuchFieldException | IllegalAccessException | RuntimeException e) {
                        // Java 9+ 强封装下反射 JDK 内部 memberValues 可能抛 InaccessibleObjectException(RuntimeException)
                        // 失败时放弃修改事件优先级(退化为默认 HIGH), 继续启用插件, 不再直接禁用
                        SXAttribute.getInst().getLogger().warning("EditDamageEventPriority ERROR! Fallback to default priority.");
                        e.printStackTrace();
                    }
                    break;
                }
            }

        }

        Bukkit.getPluginManager().registerEvents(new ListenerBanShieldInteract(), this);
        Bukkit.getPluginManager().registerEvents(new ListenerUpdateAttribute(), this);
        Bukkit.getPluginManager().registerEvents(new ListenerDamage(), this);
        Bukkit.getPluginManager().registerEvents(listenerHealthChange, this);
        Bukkit.getPluginManager().registerEvents(new ListenerItemSpawn(), this);
        mainCommand.setup("sxAttribute");
        SXAttribute.getInst().getLogger().info("Author: Saukiya Q群:830192024");
    }

    @Override
    public void onDisable() {
        // 各字段均在 onEnable 才赋值; 若 onLoad/onEnable 提前崩溃则为 null, 需判空避免掩盖原始异常
        DamageParticlePacketLimiter.unregister(this);
        if (attributeManager != null) attributeManager.onAttributeDisable();
        if (conditionManager != null) conditionManager.onConditionDisable();
        if (attributeEngine != null) attributeEngine.disable();
        if (sourceService != null) sourceService.disable();
        if (listenerHealthChange != null) {
            try {
                listenerHealthChange.cancel();
            } catch (IllegalStateException ignored) {
                // 兼容旧版调度器在任务尚未注册时抛出的取消异常。
            }
        }
    }
}
