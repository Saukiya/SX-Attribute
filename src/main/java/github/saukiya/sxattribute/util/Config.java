package github.saukiya.sxattribute.util;

import github.saukiya.sxattribute.SXAttribute;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;

public class Config {
    public static final String COMMAND_STATS_DISPLAY_SKULL_SKIN = "CommandStatsDisplaySkullSkin";
    public static final String DECIMAL_FORMAT = "DecimalFormat";

    public static final String HOLOGRAPHIC_ENABLED = "Holographic.Enabled";
    public static final String HOLOGRAPHIC_DISPLAY_TIME = "Holographic.DisplayTime";
    public static final String HOLOGRAPHIC_BLACK_CAUSE_LIST = "Holographic.BlackCauseList";
    public static final String HOLOGRAPHIC_HEALTH_TAKE_ENABLED = "Holographic.HealthOrTake.Enabled";

    public static final String HEALTH_NAME_ENABLED = "HealthDisplays.Name.Enabled";
    public static final String HEALTH_NAME_SIZE = "HealthDisplays.Name.Size";
    public static final String HEALTH_NAME_CURRENT = "HealthDisplays.Name.Current";
    public static final String HEALTH_NAME_LOSS = "HealthDisplays.Name.Loss";
    public static final String HEALTH_NAME_PREFIX = "HealthDisplays.Name.Prefix";
    public static final String HEALTH_NAME_SUFFIX = "HealthDisplays.Name.Suffix";
    public static final String HEALTH_NAME_DISPLAY_TIME = "HealthDisplays.Name.DisplayTime";
    public static final String HEALTH_BOSS_BAR_ENABLED = "HealthDisplays.BossBar.Enabled";
    public static final String HEALTH_BOSS_BAR_FORMAT = "HealthDisplays.BossBar.Format";
    public static final String HEALTH_BOSS_BAR_DISPLAY_TIME = "HealthDisplays.BossBar.DisplayTime";
    public static final String HEALTH_BOSS_BAR_BLACK_CAUSE_LIST = "HealthDisplays.BossBar.BlackCauseList";

    public static final String ITEM_DISPLAY_NAME = "ItemDisplayName";

    public static final String DAMAGE_EVENT_PRIORITY = "DamageEvent.Priority";
    public static final String DAMAGE_EVENT_BLACK_CAUSE_LIST = "DamageEvent.BlackCauseList";
    public static final String DAMAGE_CALCULATION_TO_EVE = "DamageEvent.DamageCalculationToEVE";
    public static final String DAMAGE_GAUGES = "DamageEvent.DamageGauges";
    public static final String BAN_SHIELD_DEFENSE = "DamageEvent.BanShieldDefense";
    public static final String BOW_CLOSE_RANGE_ATTACK = "DamageEvent.BowCloseRangeAttack";
    public static final String MINIMUM_DAMAGE = "DamageEvent.MinimumDamage";
    public static final String CLEAR_DEFAULT_ATTRIBUTE = "ClearDefaultAttribute";

    public static final String PRG_INVENTORY_SLOT = "RPGInventorySlot";
    public static final String REPAIR_ITEM_VALUE = "RepairItemValue";
    public static final String REGISTER_SLOTS_ENABLED = "RegisterSlots.Enabled";
    public static final String REGISTER_SLOTS_LIST = "RegisterSlots.List";
    public static final String DEFAULT_ATTRIBUTE = "DefaultAttribute";

    public static final String NAME_HAND_MAIN = "Condition.Hand.MainName";
    public static final String NAME_HAND_OFF = "Condition.Hand.OffName";
    public static final String NAME_ARMOR = "Condition.Armor";
    public static final String NAME_ROLE = "Condition.Role.Name";
    public static final String NAME_LIMIT_LEVEL = "Condition.LimitLevel.Name";
    public static final String NAME_DURABILITY = "Condition.Durability.Name";
    public static final String CLEAR_ITEM_DURABILITY = "Condition.Durability.ClearItem";
    public static final String NAME_SELL = "Condition.Sell.Name";
    public static final String NAME_EXPIRY_TIME = "Condition.ExpiryTime.Name";
    public static final String FORMAT_EXPIRY_TIME = "Condition.ExpiryTime.Format";

    public static final String ATTRIBUTE_PRIORITY = "AttributePriority";
    public static final String CONDITION_PRIORITY = "ConditionPriority";

    public static final String COMPATIBILITY_MYTHIC_MOBS = "Compatibility.MythicMobs";

    @Getter
    private static YamlConfiguration config;
    @Getter
    private static boolean commandStatsDisplaySkullSkin;
    @Getter
    private static List<String> damageEventBlackList;
    @Getter
    private static boolean healthNameVisible;
    @Getter
    private static boolean healthBossBar;
    @Getter
    private static boolean holographic;
    @Getter
    private static List<String> holographicBlackList;
    @Getter
    private static boolean holographicHealthTake;
    @Getter
    private static boolean itemDisplayName;
    @Getter
    private static boolean damageCalculationToEVE;
    @Getter
    private static boolean damageGauges;
    @Getter
    private static boolean banShieldDefense;
    @Getter
    private static boolean bowCloseRangeAttack;
    @Getter
    private static List<Integer> rpgInvSlotList;
    @Getter
    private static boolean clearDefaultAttribute;
    @Getter
    private static boolean registerSlot;
    @Getter
    private static double minimumDamage;
    @Getter
    private static List<String> bossBarBlackCauseList;
    @Getter
    private static boolean clearItemDurability;
    @Getter
    private static boolean mythicMobs;

    /**
     * 加载Config类
     */
    public static void loadConfig() {
        File file = new File(SXAttribute.getInst().getDataFolder(), "Config.yml");
        if (!file.exists()) {
            SXAttribute.getInst().getLogger().info("Create Config.yml");
            SXAttribute.getInst().saveResource("Config.yml", true);
        }
        config = YamlConfiguration.loadConfiguration(file);
        SXAttribute.setDf(new DecimalFormat(config.getString(DECIMAL_FORMAT)));
        commandStatsDisplaySkullSkin = config.getBoolean(COMMAND_STATS_DISPLAY_SKULL_SKIN);
        healthNameVisible = config.getBoolean(HEALTH_NAME_ENABLED);
        healthBossBar = config.getBoolean(HEALTH_BOSS_BAR_ENABLED) && SXAttribute.getVersionSplit()[1] >= 9;
        bossBarBlackCauseList = config.getStringList(HEALTH_BOSS_BAR_BLACK_CAUSE_LIST);
        holographic = config.getBoolean(HOLOGRAPHIC_ENABLED);
        holographicBlackList = config.getStringList(HOLOGRAPHIC_BLACK_CAUSE_LIST);
        damageEventBlackList = config.getStringList(DAMAGE_EVENT_BLACK_CAUSE_LIST);
        holographicHealthTake = config.getBoolean(HOLOGRAPHIC_HEALTH_TAKE_ENABLED);
        itemDisplayName = config.getBoolean(ITEM_DISPLAY_NAME);
        damageCalculationToEVE = config.getBoolean(DAMAGE_CALCULATION_TO_EVE);
        damageGauges = config.getBoolean(DAMAGE_GAUGES);
        clearDefaultAttribute = config.getBoolean(CLEAR_DEFAULT_ATTRIBUTE);
        banShieldDefense = config.getBoolean(BAN_SHIELD_DEFENSE);
        bowCloseRangeAttack = config.getBoolean(BOW_CLOSE_RANGE_ATTACK);
        rpgInvSlotList = config.getIntegerList(PRG_INVENTORY_SLOT);
        registerSlot = config.getBoolean(REGISTER_SLOTS_ENABLED);
        minimumDamage = config.getDouble(MINIMUM_DAMAGE);
        clearItemDurability = config.getBoolean(CLEAR_ITEM_DURABILITY, true);
        mythicMobs = config.getBoolean(COMPATIBILITY_MYTHIC_MOBS, true);
    }
}