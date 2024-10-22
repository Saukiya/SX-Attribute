package github.saukiya.sxattribute;

import github.saukiya.sxattribute.api.SXAPI;
import github.saukiya.sxattribute.api.TempAttributeAPI;
import github.saukiya.sxattribute.command.sub.*;
import github.saukiya.sxattribute.data.SlotDataManager;
import github.saukiya.sxattribute.data.attribute.AttributeType;
import github.saukiya.sxattribute.data.attribute.SXAttributeManager;
import github.saukiya.sxattribute.data.attribute.sub.attack.*;
import github.saukiya.sxattribute.data.attribute.sub.defence.*;
import github.saukiya.sxattribute.data.attribute.sub.other.EventMessage;
import github.saukiya.sxattribute.data.attribute.sub.other.ExpAddition;
import github.saukiya.sxattribute.data.attribute.sub.other.JSAttribute;
import github.saukiya.sxattribute.data.attribute.sub.update.AttackSpeed;
import github.saukiya.sxattribute.data.attribute.sub.update.Command;
import github.saukiya.sxattribute.data.attribute.sub.update.WalkSpeed;
import github.saukiya.sxattribute.data.condition.SXConditionManager;
import github.saukiya.sxattribute.data.condition.sub.*;
import github.saukiya.sxattribute.listener.*;
import github.saukiya.sxattribute.util.Config;
import github.saukiya.sxattribute.util.Message;
import github.saukiya.sxattribute.util.MoneyUtil;
import github.saukiya.sxattribute.util.PlaceholderUtil;
import github.saukiya.sxitem.command.MainCommand;
import github.saukiya.sxitem.util.LogUtil;
import github.saukiya.sxitem.util.NMS;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * SX-Attribute
 *
 * @author Saukiya
 */
public class SXAttribute extends JavaPlugin {

    @Getter
    @Setter
    private static DecimalFormat df = new DecimalFormat("#.##");

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
    private static SlotDataManager slotDataManager;

    @Getter
    private static ListenerHealthChange listenerHealthChange;

    @Getter
    private static boolean holographic, vault, rpgInventory;

    @Getter
    private static MainCommand mainCommand;

    private static LogUtil logUtil;

    @SneakyThrows
    @Override
    public void onLoad() {
        super.onLoad();
        inst = this;
        logUtil = new LogUtil(this);
        Config.loadConfig();
        Message.loadMessage();
        mainCommand = new MainCommand(this);
        mainCommand.register(new StatsCommand());
        mainCommand.register(new RepairCommand());
        mainCommand.register(new SellCommand());
        mainCommand.register(new AttributeListCommand());
        mainCommand.register(new ConditionListCommand());
        mainCommand.register(new ReloadCommand());

        new Crit().registerAttribute();
        new Damage().registerAttribute();
        new HitRate().registerAttribute();
        new Ignition().registerAttribute();
        new LifeSteal().registerAttribute();
        new Lightning().registerAttribute();
        new AttackPotion().registerAttribute();
        new Real().registerAttribute();
        new Tearing().registerAttribute();

        new AttackElement().registerAttribute();

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
        if (NMS.compareTo(1, 9, 0) >= 0) {
            new AttackSpeed().registerAttribute();
        }
        new Command().registerAttribute();

        File jsAttributeFiles = new File(getDataFolder(), "Attribute" + File.separator + "JavaScript");
        if (!jsAttributeFiles.exists() && NMS.compareTo(1, 9, 0) >= 0) {
            saveResource("Attribute/JavaScript/JSAttribute.js", true);
            saveResource("Attribute/SX-Attribute/JSAttribute_JS.yml", true);
        }
        if (jsAttributeFiles.exists() && jsAttributeFiles.isDirectory()) {
            ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
            if (scriptEngineManager.getEngineByName("js") == null) {
                scriptEngineManager.registerEngineName("js", new NashornScriptEngineFactory());
            }

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
                    ScriptEngine engine = scriptEngineManager.getEngineByName("js");
                    engine.put("Arrays", arrays);
                    engine.put("SXAttributeType", sxAttributeType);
                    engine.put("SXAttribute", sxAttribute);
                    engine.put("Bukkit", bukkit);
                    engine.put("API", api);
                    try {
                        engine.eval(new FileReader(jsFile));
                        new JSAttribute(jsFile.getName().replace(".js", ""), engine).registerAttribute();
                    } catch (ScriptException | FileNotFoundException e) {
                        getLogger().info("========================================================");
                        e.printStackTrace();
                        getLogger().warning("Error JavaScript: " + jsFile.getName());
                        getLogger().info("========================================================");
                    }
                }
            }
        }

        if (NMS.compareTo(1, 9, 0) >= 0) {
            new MainHand().registerCondition();
            new OffHand().registerCondition();
        }
        new Hand().registerCondition();
        new LimitLevel().registerCondition();
        new Role().registerCondition();
        new ExpiryTime().registerCondition();
        new Durability().registerCondition();

    }

    @Override
    public void onEnable() {
        new Metrics(this, 3147);
        long oldTimes = System.currentTimeMillis();
        PlaceholderUtil.setup();

        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            try {
                MoneyUtil.setup();
                vault = true;
            } catch (NullPointerException e) {
                getLogger().warning("No Find Vault-Economy!");
            }
        } else {
            getLogger().warning("No Find Vault!");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
            holographic = true;
        } else {
            getLogger().warning("No Find HolographicDisplays!");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("RPGInventory")) {
            rpgInventory = true;
        } else {
            getLogger().warning("No Find RPGInventory!");
        }

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
                        getLogger().info("EditDamageEventPriority: " + priority.name());

                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        getLogger().warning("EditDamageEventPriority ERROR!");
                        e.printStackTrace();
                        this.setEnabled(false);
                        return;
                    }
                    break;
                }
            }

        }

        if (NMS.compareTo("v1_9_R0") >= 0) {
            Bukkit.getPluginManager().registerEvents(new ListenerBanShieldInteract(), this);
        }
        Bukkit.getPluginManager().registerEvents(new ListenerUpdateAttribute(), this);
        Bukkit.getPluginManager().registerEvents(new ListenerDamage(), this);
        Bukkit.getPluginManager().registerEvents(listenerHealthChange, this);
        Bukkit.getPluginManager().registerEvents(new ListenerItemSpawn(), this);

        TempAttributeAPI.startUpdate();

        mainCommand.onEnable("SxAttribute");
        getLogger().info("Loading Time: " + (System.currentTimeMillis() - oldTimes) + " ms");
        getLogger().info("Author: " + getDescription().getAuthors());
    }

    @Override
    public void onDisable() {
        attributeManager.onAttributeDisable();
        conditionManager.onConditionDisable();
        listenerHealthChange.cancel();
        mainCommand.onDisable();
        logUtil.onDisable();
    }
}
