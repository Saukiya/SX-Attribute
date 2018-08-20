package github.saukiya.sxattribute;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import github.saukiya.sxattribute.api.SXAttributeAPI;
import github.saukiya.sxattribute.bstats.Metrics;
import github.saukiya.sxattribute.command.MainCommand;
import github.saukiya.sxattribute.data.ItemDataManager;
import github.saukiya.sxattribute.data.RandomStringManager;
import github.saukiya.sxattribute.data.RegisterSlotManager;
import github.saukiya.sxattribute.data.attribute.SXAttributeData;
import github.saukiya.sxattribute.data.attribute.SXAttributeManager;
import github.saukiya.sxattribute.data.condition.SXConditionManager;
import github.saukiya.sxattribute.inventory.DisplaySlotInventory;
import github.saukiya.sxattribute.inventory.StatsInventory;
import github.saukiya.sxattribute.listener.*;
import github.saukiya.sxattribute.util.*;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Random;
import java.util.stream.IntStream;


public class SXAttribute extends JavaPlugin {

    @Getter
    private static final int[] versionSplit = new int[3];
    @Getter
    private static final Random random = new Random();
    @Getter
    private static JavaPlugin plugin;
    @Getter
    @Setter
    private static DecimalFormat df = new DecimalFormat("#.##");
    @Getter
    @Setter
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    @Getter
    private static SXAttributeAPI api;

    @Getter
    private static boolean placeholder = true;

    @Getter
    private static boolean holographic = true;

    @Getter
    private static boolean vault = true;

    @Getter
    private static boolean rpgInventory = true;

    @Getter
    private static boolean sxLevel = true;

    @Getter
    private ItemUtil itemUtil;

    @Getter
    private MainCommand mainCommand;

    @Getter
    private SXAttributeManager attributeManager;

    @Getter
    private SXConditionManager conditionManager;

    @Getter
    private RandomStringManager randomStringManager;

    @Getter
    private ItemDataManager itemDataManager;

    @Getter
    private RegisterSlotManager registerSlotManager;

    @Getter
    private StatsInventory statsInventory;

    @Getter
    private DisplaySlotInventory displaySlotInventory;

    @Getter
    private OnUpdateStatsListener onUpdateStatsListener;

    @Getter
    private OnItemDurabilityListener onItemDurabilityListener;

    @Getter
    private OnDamageListener onDamageListener;

    @Getter
    private OnHealthChangeDisplayListener onHealthChangeDisplayListener;

    @Override
    public void onLoad() {
        super.onLoad();
        plugin = this;
        api = new SXAttributeAPI(this);
        try {
            Config.loadConfig();
            Message.loadMessage();
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§cIO Error!");
        }
        mainCommand = new MainCommand(this);
        attributeManager = new SXAttributeManager(this);
        conditionManager = new SXConditionManager(this);
    }

    @Override
    public void onEnable() {
        Long oldTimes = System.currentTimeMillis();
        String version = Bukkit.getBukkitVersion().split("-")[0].replace(" ", "");
        Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "ServerVersion: " + version);
        String[] strSplit = version.split("[.]");
        IntStream.range(0, strSplit.length).forEachOrdered(i -> versionSplit[i] = Integer.valueOf(strSplit[i]));
        new Metrics(this);
        try {
            itemUtil = new ItemUtil(this);
            randomStringManager = new RandomStringManager(this);
            itemDataManager = new ItemDataManager(this);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§cIO Error!");
            this.setEnabled(false);
            return;
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
            Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§cReflection Error!");
            this.setEnabled(false);
            return;
        }
        registerSlotManager = new RegisterSlotManager(this);
        statsInventory = new StatsInventory(this);
        displaySlotInventory = new DisplaySlotInventory(this);
        onUpdateStatsListener = new OnUpdateStatsListener(this);
        onItemDurabilityListener = new OnItemDurabilityListener(this);
        onDamageListener = new OnDamageListener(this);
        onHealthChangeDisplayListener = new OnHealthChangeDisplayListener(this);
        attributeManager.loadDefaultAttributeData();
        attributeManager.onAttributeEnable();
        SXAttributeData attributeData = new SXAttributeData();
        Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "Load §c" + attributeData.getAttributeMap().size() + "§r Attributes");
        Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "Load §c" + conditionManager.getConditionMap().size() + "§r Condition");

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new Placeholders(this);
            int size = attributeData.getAttributeMap().values().stream().mapToInt(subAttribute -> subAttribute.getPlaceholders().size()).sum();
            Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "Load §c" + size + "§r Placeholders");
        } else {
            placeholder = false;
            Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§cNo Find PlaceholderAPI!");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            MoneyUtil.setup();
            Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "Find Vault");
        } else {
            vault = false;
            Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§cNo Find Vault!");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
            Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "Find HolographicDisplays");
        } else {
            holographic = false;
            Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§cNo Find HolographicDisplays!");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("MythicMobs")) {
            Bukkit.getPluginManager().registerEvents(new OnMythicmobsDropOrSpawnListener(this), this);
            Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "Find MythicMobs");
        } else {
            Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§cNo Find MythicMobs!");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("RPGInventory")) {
            Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "Find RPGInventory");
        } else {
            rpgInventory = false;
            Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§cNo Find RPGInventory!");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("SX-Level")) {
            Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "Find SX-Level");
        } else {
            sxLevel = false;
            Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§cNo Find SX-Level!");
        }

        Bukkit.getPluginManager().registerEvents(new OnBanShieldInteractListener(), this);
        Bukkit.getPluginManager().registerEvents(new OnInventoryClickListener(this), this);
        Bukkit.getPluginManager().registerEvents(new OnInventoryCloseListener(), this);
        Bukkit.getPluginManager().registerEvents(onUpdateStatsListener, this);
        Bukkit.getPluginManager().registerEvents(onItemDurabilityListener, this);
        Bukkit.getPluginManager().registerEvents(onDamageListener, this);
        Bukkit.getPluginManager().registerEvents(onHealthChangeDisplayListener, this);
        Bukkit.getPluginManager().registerEvents(new OnItemSpawnListener(), this);
        Bukkit.getPluginManager().registerEvents(sxLevel ? new OnSXExpChangeListener(this) : new OnExpChangeListener(this), this);

        mainCommand.setUp("sxAttribute");
        Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "Load Time: §c" + (System.currentTimeMillis() - oldTimes) + "§r ms");
        Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§cAuthor: Saukiya QQ: 1940208750");
        Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§cThis plugin was first launched on www.mcbbs.net!");
        Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§4Reprint is prohibited without permission!");
    }

    public void onDisable() {
        if (SXAttribute.isHolographic()) {
            onDamageListener.getHologramsList().forEach(Hologram::delete);
        }
        if (Config.isHealthBossBar()) {
            onHealthChangeDisplayListener.getBossList().forEach((bossBarData -> bossBarData.getBossBar().removeAll()));
        }
        if (Config.isHealthNameVisible()) {
            onHealthChangeDisplayListener.getNameList().forEach((nameData) ->{
                if (nameData.getEntity() != null && !nameData.getEntity().isDead()) {
                    nameData.getEntity().setCustomName(nameData.getName());
                    nameData.getEntity().setCustomNameVisible(nameData.isVisible());
                }
            });
        }
    }
}
