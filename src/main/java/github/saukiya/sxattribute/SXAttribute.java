package github.saukiya.sxattribute;

import github.saukiya.sxattribute.api.SXAPI;
import github.saukiya.sxattribute.command.MainCommand;
import github.saukiya.sxattribute.data.RandomStringManager;
import github.saukiya.sxattribute.data.SlotDataManager;
import github.saukiya.sxattribute.data.attribute.AttributeType;
import github.saukiya.sxattribute.data.attribute.SXAttributeManager;
import github.saukiya.sxattribute.data.attribute.sub.attack.*;
import github.saukiya.sxattribute.data.attribute.sub.defence.*;
import github.saukiya.sxattribute.data.attribute.sub.other.EventMessage;
import github.saukiya.sxattribute.data.attribute.sub.other.ExpAddition;
import github.saukiya.sxattribute.data.attribute.sub.other.JSAttribute;
import github.saukiya.sxattribute.data.attribute.sub.other.MythicMobsDrop;
import github.saukiya.sxattribute.data.attribute.sub.update.AttackSpeed;
import github.saukiya.sxattribute.data.attribute.sub.update.Command;
import github.saukiya.sxattribute.data.attribute.sub.update.WalkSpeed;
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
import java.util.stream.IntStream;

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

    @SneakyThrows
    @Override
    public void onLoad() {
        super.onLoad();
        inst = this;
        String version = Bukkit.getBukkitVersion().split("-")[0].replace(" ", "");
        String[] strSplit = version.split("[.]");
        IntStream.range(0, strSplit.length).forEach(i -> versionSplit[i] = Integer.valueOf(strSplit[i]));
        SXAttribute.getInst().getLogger().info("ServerVersion: " + version);
        Config.loadConfig();
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
        if (Bukkit.getPluginManager().getPlugin("MythicMobs") != null) {
            new MythicMobsDrop().registerAttribute();
        }
        new HealthRegen().registerAttribute();

        new Health().registerAttribute();
        new WalkSpeed().registerAttribute();
        if (SXAttribute.getVersionSplit()[1] > 8) {
            new AttackSpeed().registerAttribute();
        }
        new Command().registerAttribute();

        File jsAttributeFiles = new File(getDataFolder(), "Attribute" + File.separator + "JavaScript");
        if (!jsAttributeFiles.exists() && SXAttribute.getVersionSplit()[1] > 8) {
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

        if (SXAttribute.getVersionSplit()[1] > 8) {
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

        if (Bukkit.getPluginManager().isPluginEnabled("MythicMobs")) {
            mythicMobs = true;
            Bukkit.getPluginManager().registerEvents(new ListenerMythicmobsSpawn(), this);
        } else {
            SXAttribute.getInst().getLogger().warning("No Find MythicMobs!");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("RPGInventory")) {
            rpgInventory = true;
        } else {
            SXAttribute.getInst().getLogger().warning("No Find RPGInventory!");
        }

        try {
            nbtUtil = new NbtUtil();
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
            SXAttribute.getInst().getLogger().warning("Reflection Error!");
            this.setEnabled(false);
            return;
        }

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

                    } catch (NoSuchFieldException | IllegalAccessException e) {
                        SXAttribute.getInst().getLogger().warning("EditDamageEventPriority ERROR!");
                        e.printStackTrace();
                        this.setEnabled(false);
                        return;
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
        SXAttribute.getInst().getLogger().info("Author: Saukiya");
        if (Config.getConfig().getBoolean(Config.QAQ)) {
            Bukkit.getConsoleSender().sendMessage("");
            Bukkit.getConsoleSender().sendMessage("§c   ______  __             ___   __  __       _ __          __");
            Bukkit.getConsoleSender().sendMessage("§c  / ___/ |/ /            /   | / /_/ /______(_) /_  __  __/ /____");
            Bukkit.getConsoleSender().sendMessage("§c  \\__ \\|   /   ______   / /| |/ __/ __/ ___/ / __ \\/ / / / __/ _ \\");
            Bukkit.getConsoleSender().sendMessage("§c ___/ /   |   /_____/  / ___ / /_/ /_/ /  / / /_/ / /_/ / /_/  __/");
            Bukkit.getConsoleSender().sendMessage("§c/____/_/|_|           /_/  |_\\__/\\__/_/  /_/_.___/\\__,_/\\__/\\___/");
            Bukkit.getConsoleSender().sendMessage("");
        }
    }

    @Override
    public void onDisable() {
        attributeManager.onAttributeDisable();
        conditionManager.onConditionDisable();
        listenerHealthChange.cancel();
    }
}