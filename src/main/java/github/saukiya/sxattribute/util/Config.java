package github.saukiya.sxattribute.util;

import github.saukiya.sxattribute.SXAttribute;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

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
    /** 控制 SX-Item 物品与随机表达式桥接；默认关闭以避免 SX-Attribute 自带实现抢占配置。 */
    public static final String SX_ITEM_ENABLED = "SXItem.Enabled";

    public static final String DAMAGE_EVENT_PRIORITY = "DamageEvent.Priority";
    public static final String DAMAGE_EVENT_BLACK_CAUSE_LIST = "DamageEvent.BlackCauseList";
    public static final String DAMAGE_CALCULATION_TO_EVE = "DamageEvent.DamageCalculationToEVE";
    public static final String DAMAGE_GAUGES = "DamageEvent.DamageGauges";
    public static final String BAN_SHIELD_DEFENSE = "DamageEvent.BanShieldDefense";
    public static final String BOW_CLOSE_RANGE_ATTACK = "DamageEvent.BowCloseRangeAttack";
    public static final String MINIMUM_DAMAGE = "DamageEvent.MinimumDamage";
    /**
     * 伤害指示粒子出站包的数量上限；负数表示不拦截，零表示完全取消该粒子包。
     */
    public static final String DAMAGE_PARTICLE_LIMIT = "DamageEvent.DamageParticleLimit";
    public static final String CLEAR_DEFAULT_ATTRIBUTE = "ClearDefaultAttribute";

    public static final String PRG_INVENTORY_SLOT = "RPGInventorySlot";
    public static final String REPAIR_ITEM_VALUE = "RepairItemValue";
    public static final String REGISTER_SLOTS_ENABLED = "RegisterSlots.Enabled";
    public static final String REGISTER_SLOTS_LIST = "RegisterSlots.List";
    public static final String DEFAULT_ATTRIBUTE = "DefaultAttribute";

    /**
     * 可作为属性文本来源的 NBT 节点列表。节点路径交由统一 NBT 适配器解析，
     * 因而必须沿用该适配器支持的点号路径格式。
     */
    public static final String NBT_ATTRIBUTE_NODES = "NBTAttribute.Nodes";

    /** 装备拓展显示模式：LORE 由本插件直接渲染，VARIABLE 交给 SX-Item 锁变量模板。 */
    public static final String EQUIPMENT_FEATURE_LORE_MODE = "EquipmentFeature.LoreMode";

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
    private static boolean sxItemEnabled;
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
    private static int damageParticleLimit;
    @Getter
    private static List<String> bossBarBlackCauseList;
    @Getter
    private static boolean clearItemDurability;
    @Getter
    private static boolean mythicMobs;
    @Getter
    private static List<String> nbtAttributeNodes = Collections.emptyList();
    @Getter
    private static EquipmentFeatureLoreMode equipmentFeatureLoreMode = EquipmentFeatureLoreMode.VARIABLE;

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
        healthBossBar = config.getBoolean(HEALTH_BOSS_BAR_ENABLED) && SXAttribute.isVersionAtLeast(9);
        bossBarBlackCauseList = config.getStringList(HEALTH_BOSS_BAR_BLACK_CAUSE_LIST);
        holographic = config.getBoolean(HOLOGRAPHIC_ENABLED);
        holographicBlackList = config.getStringList(HOLOGRAPHIC_BLACK_CAUSE_LIST);
        damageEventBlackList = config.getStringList(DAMAGE_EVENT_BLACK_CAUSE_LIST);
        holographicHealthTake = config.getBoolean(HOLOGRAPHIC_HEALTH_TAKE_ENABLED);
        itemDisplayName = config.getBoolean(ITEM_DISPLAY_NAME);
        sxItemEnabled = config.getBoolean(SX_ITEM_ENABLED, false);
        damageCalculationToEVE = config.getBoolean(DAMAGE_CALCULATION_TO_EVE);
        damageGauges = config.getBoolean(DAMAGE_GAUGES);
        clearDefaultAttribute = config.getBoolean(CLEAR_DEFAULT_ATTRIBUTE);
        banShieldDefense = config.getBoolean(BAN_SHIELD_DEFENSE);
        bowCloseRangeAttack = config.getBoolean(BOW_CLOSE_RANGE_ATTACK);
        rpgInvSlotList = config.getIntegerList(PRG_INVENTORY_SLOT);
        registerSlot = config.getBoolean(REGISTER_SLOTS_ENABLED);
        minimumDamage = config.getDouble(MINIMUM_DAMAGE);
        damageParticleLimit = config.getInt(DAMAGE_PARTICLE_LIMIT, 8);
        clearItemDurability = config.getBoolean(CLEAR_ITEM_DURABILITY, true);
        mythicMobs = config.getBoolean(COMPATIBILITY_MYTHIC_MOBS, true);
        // 在加载时清理空节点并冻结快照，避免异步装备刷新读到重载中的可变列表。
        nbtAttributeNodes = Collections.unmodifiableList(config.getStringList(NBT_ATTRIBUTE_NODES).stream()
                .map(String::trim)
                .filter(node -> !node.isEmpty())
                .distinct()
                .collect(Collectors.toList()));
        try {
            equipmentFeatureLoreMode = EquipmentFeatureLoreMode.valueOf(
                    config.getString(EQUIPMENT_FEATURE_LORE_MODE, EquipmentFeatureLoreMode.VARIABLE.name())
                            .trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            equipmentFeatureLoreMode = EquipmentFeatureLoreMode.VARIABLE;
            SXAttribute.getInst().getLogger().warning("Unknown EquipmentFeature.LoreMode; using VARIABLE");
        }
    }

    /** 装备拓展显示权归属，两个模式共享相同的 State NBT，不影响属性计算。 */
    public enum EquipmentFeatureLoreMode {
        /** SX-Attribute 直接增删 ItemMeta Lore。 */
        LORE,
        /** SX-Attribute 提供锁变量，SX-Item 物品模板负责生成 Lore。 */
        VARIABLE
    }
}
