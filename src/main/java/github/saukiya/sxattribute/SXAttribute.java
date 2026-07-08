package github.saukiya.sxattribute;

import github.saukiya.sxattribute.api.SXAPI;
import github.saukiya.sxattribute.command.MainCommand;
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
import github.saukiya.sxattribute.listener.*;
import github.saukiya.sxattribute.util.*;
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
import javax.script.ScriptException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;

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

    @Getter
    private static MainCommand mainCommand;

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

        // 原版属性包装 (数据驱动): 遍历 Attributes.yml 中含 RegistryKey 的数值型节点,
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

        File jsAttributeFiles = new File(getDataFolder(), "Attribute" + File.separator + "JavaScript");
        if (!jsAttributeFiles.exists() && SXAttribute.isHigherVersion()) {
            saveResource("Attribute/JavaScript/JSAttribute.js", true);
            saveResource("Attribute/SX-Attribute/JSAttribute_JS.yml", true);
        }
        if (jsAttributeFiles.exists() && jsAttributeFiles.isDirectory()) {
            ScriptEngineManager jsManager = new ScriptEngineManager();
            if (jsManager.getEngineByName("JavaScript") != null) {
                Class<?> clazz = Class.forName(System.getProperty("java.class.version").startsWith("52") ?
                        "jdk.internal.dynalink.beans.StaticClass" :
                        "jdk.dynalink.beans.StaticClass");
                Method method = clazz.getMethod("forClass", Class.class);
                Object arrays = method.invoke(null, Arrays.class);
                Object sxAttributeType = method.invoke(null, AttributeType.class);
                Object sxAttribute = method.invoke(null, SXAttribute.class);
                Object bukkit = method.invoke(null, Bukkit.class);
                for (File jsFile : jsAttributeFiles.listFiles()) {
                    if (jsFile.getName().endsWith(".js")) {
                        ScriptEngine engine = jsManager.getEngineByName("JavaScript");
                        engine.put("Arrays", arrays);
                        engine.put("SXAttributeType", sxAttributeType);
                        engine.put("SXAttribute", sxAttribute);
                        engine.put("Bukkit", bukkit);
                        engine.put("API", api);
                        try {
                            engine.eval(new InputStreamReader(new FileInputStream(jsFile), StandardCharsets.UTF_8));
                            new JSAttribute(jsFile.getName().replace(".js", ""), engine).registerAttribute();
                        } catch (ScriptException | FileNotFoundException e) {
                            SXAttribute.getInst().getLogger().info("==========================================================================================");
                            e.printStackTrace();
                            SXAttribute.getInst().getLogger().warning("Error JavaScript: " + jsFile.getName());
                            SXAttribute.getInst().getLogger().info("==========================================================================================");
                        }
                    }
                }
            }
        }

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

        if (Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
            holographic = true;
        } else {
            SXAttribute.getInst().getLogger().warning("No Find HolographicDisplays!");
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
        listenerHealthChange = new ListenerHealthChange();

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
        if (attributeManager != null) attributeManager.onAttributeDisable();
        if (conditionManager != null) conditionManager.onConditionDisable();
        if (listenerHealthChange != null) {
            try {
                listenerHealthChange.cancel();
            } catch (IllegalStateException ignored) {
                // BukkitRunnable 未被调度时 cancel() 会抛 IllegalStateException, 忽略
            }
        }
    }
}