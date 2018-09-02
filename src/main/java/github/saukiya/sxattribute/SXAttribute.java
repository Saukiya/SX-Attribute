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

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Random;
import java.util.stream.IntStream;


public class SXAttribute extends JavaPlugin {

    @Getter
    private static final int[] versionSplit = new int[3];
    @Getter
    private static final Random random = new Random();

    @Getter
    private static String pluginName;
    @Getter
    private static String pluginVersion;
    @Getter
    private static File pluginFile;
    @Getter
    @Setter
    private static DecimalFormat df = new DecimalFormat("#.##");
    @Getter
    @Setter
    private static SimpleDateFormatUtils sdf;
    @Getter
    private static SXAttributeAPI api;

    @Getter
    private static boolean placeholder = false;

    @Getter
    private static boolean holographic = false;

    @Getter
    private static boolean vault = false;

    @Getter
    private static boolean rpgInventory = false;

    @Getter
    private static boolean sxLevel = false;

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
    private OnDamageListener onDamageListener;

    @Getter
    private OnHealthChangeDisplayListener onHealthChangeDisplayListener;

    @Override
    public void onLoad() {
        super.onLoad();
        pluginFile = this.getDataFolder();
        pluginName = this.getName();
        pluginVersion = this.getDescription().getVersion();
        api = new SXAttributeAPI(this);
        try {
            Config.loadConfig();
            Message.loadMessage();
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§cIO Error!");
        }
        sdf = new SimpleDateFormatUtils();
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

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            placeholder = true;
            new Placeholders(this);
            Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "Find Placeholders");
        } else {
            Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§cNo Find PlaceholderAPI!");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            try {
                MoneyUtil.setup();
                vault = true;
                Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "Find Vault");
            } catch (NullPointerException e) {
                Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§cNo Find Vault-Economy!");
            }
        } else {
            Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§cNo Find Vault!");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
            holographic = true;
            Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "Find HolographicDisplays");
        } else {
            Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§cNo Find HolographicDisplays!");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("MythicMobs")) {
            Bukkit.getPluginManager().registerEvents(new OnMythicmobsSpawnListener(this), this);
            Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "Find MythicMobs");
        } else {
            Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§cNo Find MythicMobs!");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("RPGInventory")) {
            rpgInventory = true;
            Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "Find RPGInventory");
        } else {
            Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§cNo Find RPGInventory!");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("SX-Level")) {
            sxLevel = true;
            Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "Find SX-Level");
        } else {
            Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§cNo Find SX-Level!");
        }

        attributeManager.loadDefaultAttributeData();
        attributeManager.onAttributeEnable();
        SXAttributeData attributeData = new SXAttributeData();
        Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "Load §c" + attributeData.getAttributeMap().size() + "§r Attributes");

        conditionManager.onConditionEnable();
        Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "Load §c" + conditionManager.getConditionMap().size() + "§r Condition");

        registerSlotManager = new RegisterSlotManager(this);
        statsInventory = new StatsInventory(this);
        displaySlotInventory = new DisplaySlotInventory(this);
        onUpdateStatsListener = new OnUpdateStatsListener(this);
        onDamageListener = new OnDamageListener(this);
        onHealthChangeDisplayListener = new OnHealthChangeDisplayListener(this);
        Bukkit.getPluginManager().registerEvents(new OnBanShieldInteractListener(), this);
        Bukkit.getPluginManager().registerEvents(new OnInventoryClickListener(this), this);
        Bukkit.getPluginManager().registerEvents(new OnInventoryCloseListener(), this);
        Bukkit.getPluginManager().registerEvents(onUpdateStatsListener, this);
        Bukkit.getPluginManager().registerEvents(onDamageListener, this);
        Bukkit.getPluginManager().registerEvents(onHealthChangeDisplayListener, this);
        Bukkit.getPluginManager().registerEvents(new OnItemSpawnListener(), this);
        mainCommand.setUp("sxAttribute");
        Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "Load Time: §c" + (System.currentTimeMillis() - oldTimes) + "§r ms");
        Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§cAuthor: Saukiya QQ: 1940208750");
        Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§cThis plugin was first launched on www.mcbbs.net!");
        Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§4Reprint is prohibited without permission!");
    }

    @Override
    public void onDisable() {
        attributeManager.onAttributeDisable();
        conditionManager.onConditionDisable();
        if (SXAttribute.isHolographic() && onDamageListener.getHologramsList().size() > 0) {
            onDamageListener.getHologramsList().forEach(Hologram::delete);
        }
        if (Config.isHealthBossBar() && onHealthChangeDisplayListener.getBossList().size() > 0) {
            onHealthChangeDisplayListener.getBossList().forEach((bossBarData -> bossBarData.getBossBar().removeAll()));
        }
        if (Config.isHealthNameVisible() && onHealthChangeDisplayListener.getNameList().size() > 0) {
            onHealthChangeDisplayListener.getNameList().forEach((nameData) -> {
                if (nameData.getEntity() != null && !nameData.getEntity().isDead()) {
                    nameData.getEntity().setCustomName(nameData.getName());
                    nameData.getEntity().setCustomNameVisible(nameData.isVisible());
                }
            });
        }
    }
}
