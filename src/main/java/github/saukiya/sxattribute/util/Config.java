package github.saukiya.sxattribute.util;

import github.saukiya.sxattribute.SXAttribute;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;

public class Config {
    public static final String HOLOGRAPHIC_DISPLAY_TIME = "Holographic.DisplayTime";
    public static final String HEALTH_NAME_VISIBLE_SIZE = "Health.NameVisible.Size";
    public static final String HEALTH_NAME_VISIBLE_CURRENT = "Health.NameVisible.Current";
    public static final String HEALTH_NAME_VISIBLE_LOSS = "Health.NameVisible.Loss";
    public static final String HEALTH_NAME_VISIBLE_PREFIX = "Health.NameVisible.Prefix";
    public static final String HEALTH_NAME_VISIBLE_SUFFIX = "Health.NameVisible.Suffix";
    public static final String HEALTH_NAME_VISIBLE_DISPLAY_TIME = "Health.NameVisible.DisplayTime";
    public static final String HEALTH_BOSS_BAR_FORMAT = "Health.BossBar.Format";
    public static final String HEALTH_BOSS_BAR_DISPLAY_TIME = "Health.BossBar.DisplayTime";
    public static final String HEALTH_SCALED_VALUE = "HealthScaled.Value";
    public static final String PRG_INVENTORY__WHITE_SLOT = "RPGInventory.WhiteSlot";
    public static final String REPAIR_ITEM_VALUE = "RepairItemValue";
    public static final String REGISTER_SLOTS_LIST = "RegisterSlots.List";
    public static final String REGISTER_SLOTS_LOCK_NAME = "RegisterSlots.Lock.Name";
    public static final String DEFAULT_STATS = "DefaultAttribute";
    public static final String NAME_HAND_MAIN = "Condition.Hand.InMain.Name";
    public static final String NAME_HAND_OFF = "Condition.Hand.InOff.Name";
    public static final String NAME_ARMOR = "Condition.Armor";
    public static final String NAME_ROLE = "Condition.Role.Name";
    public static final String NAME_LIMIT_LEVEL = "Condition.LimitLevel.Name";
    public static final String NAME_DURABILITY = "Condition.Durability.Name";
    public static final String NAME_SELL = "Condition.Sell.Name";
    public static final String NAME_EXPIRY_TIME = "Condition.ExpiryTime.Name";
    public static final String FORMAT_EXPIRY_TIME = "Condition.ExpiryTime.Format";
    public static final String NAME_ATTACK_SPEED = "Condition.AttackSpeed.Name";

    public static final String NAME_EXP_ADDITION = "Attribute.ExpAddition.Name";
    public static final String NAME_SPEED = "Attribute.Speed.Name";
    public static final String NAME_HEALTH = "Attribute.Health.Name";
    public static final String NAME_HEALTH_REGEN = "Attribute.HealthRegen.Name";
    public static final String NAME_DODGE = "Attribute.Dodge.Name";
    public static final String NAME_DEFENSE = "Attribute.Defense.Name";
    public static final String NAME_PVP_DEFENSE = "Attribute.PVPDefense.Name";
    public static final String NAME_PVE_DEFENSE = "Attribute.PVEDefense.Name";
    public static final String NAME_TOUGHNESS = "Attribute.Toughness.Name";
    public static final String NAME_REFLECTION_RATE = "Attribute.ReflectionRate.Name";
    public static final String NAME_REFLECTION = "Attribute.Reflection.Name";
    public static final String NAME_BLOCK_RATE = "Attribute.BlockRate.Name";
    public static final String NAME_BLOCK = "Attribute.Block.Name";
    public static final String NAME_DAMAGE = "Attribute.Damage.Name";
    public static final String NAME_PVP_DAMAGE = "Attribute.PVPDamage.Name";
    public static final String NAME_PVE_DAMAGE = "Attribute.PVEDamage.Name";
    public static final String NAME_HIT_RATE = "Attribute.HitRate.Name";
    public static final String NAME_REAL = "Attribute.Real.Name";
    public static final String NAME_CRIT_RATE = "Attribute.Crit.Name";
    public static final String NAME_CRIT = "Attribute.CritDamage.Name";
    public static final String NAME_LIFE_STEAL = "Attribute.LifeSteal.Name";
    public static final String NAME_LIFE_STEAL_RATE = "Attribute.LifeStealRate.Name";
    public static final String NAME_IGNITION = "Attribute.Ignition.Name";
    public static final String NAME_WITHER = "Attribute.Wither.Name";
    public static final String NAME_POISON = "Attribute.Poison.Name";
    public static final String NAME_BLINDNESS = "Attribute.Blindness.Name";
    public static final String NAME_SLOWNESS = "Attribute.Slowness.Name";
    public static final String NAME_LIGHTNING = "Attribute.Lightning.Name";
    public static final String NAME_TEARING = "Attribute.Tearing.Name";
    public static final String VALUE_EXP_ADDITION = "Attribute.ExpAddition.Value";
    public static final String VALUE_SPEED = "Attribute.Speed.Value";
    public static final String VALUE_HEALTH = "Attribute.Health.Value";
    public static final String VALUE_HEALTH_REGEN = "Attribute.HealthRegen.Value";
    public static final String VALUE_DODGE = "Attribute.Dodge.Value";
    public static final String VALUE_DEFENSE = "Attribute.Defense.Value";
    public static final String VALUE_PVP_DEFENSE = "Attribute.PVPDefense.Value";
    public static final String VALUE_PVE_DEFENSE = "Attribute.PVEDefense.Value";
    public static final String VALUE_TOUGHNESS = "Attribute.Toughness.Value";
    public static final String VALUE_REFLECTION_RATE = "Attribute.ReflectionRate.Value";
    public static final String VALUE_REFLECTION = "Attribute.Reflection.Value";
    public static final String VALUE_BLOCK_RATE = "Attribute.BlockRate.Value";
    public static final String VALUE_BLOCK = "Attribute.Block.Value";
    public static final String VALUE_DAMAGE = "Attribute.Damage.Value";
    public static final String VALUE_PVE_DAMAGE = "Attribute.PVPDamage.Value";
    public static final String VALUE_PVP_DAMAGE = "Attribute.PVEDamage.Value";
    public static final String VALUE_HIT_RATE = "Attribute.HitRate.Value";
    public static final String VALUE_REAL = "Attribute.Real.Value";
    public static final String VALUE_CRIT_RATE = "Attribute.Crit.Value";
    public static final String VALUE_CRIT = "Attribute.CritDamage.Value";
    public static final String VALUE_LIFE_STEAL = "Attribute.LifeSteal.Value";
    public static final String VALUE_LIFE_STEAL_RATE = "Attribute.LifeStealRate.Value";
    public static final String VALUE_IGNITION = "Attribute.Ignition.Value";
    public static final String VALUE_WITHER = "Attribute.Wither.Value";
    public static final String VALUE_POISON = "Attribute.Poison.Value";
    public static final String VALUE_BLINDNESS = "Attribute.Blindness.Value";
    public static final String VALUE_SLOWNESS = "Attribute.Slowness.Value";
    public static final String VALUE_LIGHTNING = "Attribute.Lightning.Value";
    public static final String VALUE_TEARING = "Attribute.Tearing.Value";

    private static final String CONFIG_VERSION = "ConfigVersion";
    private static final String DECIMAL_FORMAT = "DecimalFormat";
    private static final String PRIORITY_EXP_ADDITION = "AttributePriority.ExpAddition";
    private static final String PRIORITY_SPEED = "AttributePriority.Speed";

    private static final String PRIORITY_HEALTH = "AttributePriority.Health";
    private static final String PRIORITY_HEALTH_REGEN = "AttributePriority.HealthRegen";
    private static final String PRIORITY_DODGE = "AttributePriority.Dodge";
    private static final String PRIORITY_DEFENSE = "AttributePriority.Defense";
    private static final String PRIORITY_TOUGHNESS = "AttributePriority.Toughness";
    private static final String PRIORITY_REFLECTION = "AttributePriority.Reflection";
    private static final String PRIORITY_BLOCK = "AttributePriority.Block";

    private static final String PRIORITY_DAMAGE = "AttributePriority.Damage";
    private static final String PRIORITY_HIT_RATE = "AttributePriority.HitRate";
    private static final String PRIORITY_REAL = "AttributePriority.Real";
    private static final String PRIORITY_CRIT = "AttributePriority.Crit";
    private static final String PRIORITY_LIFE_STEAL = "AttributePriority.LifeSteal";
    private static final String PRIORITY_IGNITION = "AttributePriority.Ignition";
    private static final String PRIORITY_WITHER = "AttributePriority.Wither";
    private static final String PRIORITY_POISON = "AttributePriority.Poison";
    private static final String PRIORITY_BLINDNESS = "AttributePriority.Blindness";
    private static final String PRIORITY_SLOWNESS = "AttributePriority.Slowness";
    private static final String PRIORITY_LIGHTNING = "AttributePriority.Lightning";
    private static final String PRIORITY_TEARING = "AttributePriority.Tearing";

    private static final String PRIORITY_ATTACK_SPEED = "ConditionPriority.AttackSpeed";
    private static final String PRIORITY_EXPIRY_TIME = "ConditionPriority.ExpiryTime";
    private static final String PRIORITY_LIMIT_LEVEL = "ConditionPriority.LimitLevel";
    private static final String PRIORITY_MAIN_HAND = "ConditionPriority.MainHand";
    private static final String PRIORITY_OFF_HAND = "ConditionPriority.OffHand";
    private static final String PRIORITY_HAND = "ConditionPriority.Hand";
    private static final String PRIORITY_ROLE = "ConditionPriority.Role";

    private static final String REGISTER_SLOTS_ENABLED = "RegisterSlots.Enabled";
    private static final String REGISTER_SLOTS_LOCK_ENABLED = "RegisterSlots.Lock.Enabled";
    private static final String ITEM_UPDATE_ENABLED = "ItemUpdate.Enabled";
    private static final String HOLOGRAPHIC_ENABLED = "Holographic.Enabled";
    private static final String HOLOGRAPHIC_HEALTH_TAKE_ENABLED = "Holographic.HealthOrTake.Enabled";
    private static final String HEALTH_NAME_VISIBLE_ENABLED = "Health.NameVisible.Enabled";
    private static final String HEALTH_BOSS_BAR_ENABLED = "Health.BossBar.Enabled";
    private static final String HEALTH_SCALED_ENABLED = "HealthScaled.Enabled";
    private static final String ITEM_DISPLAY_NAME = "ItemDisplayName";
    private static final String DAMAGE_CALCULATION_TO_EVE = "DamageCalculationToEVE";
    private static final String DAMAGE_GAUGES = "DamageGauges";
    private static final String BAN_SHIELD_DEFENSE = "BanShieldDefense";
    private static final String CLEAR_DEFAULT_ATTRIBUTE_THIS_PLUGIN = "ClearDefaultAttribute.ThisPlugin";
    private static final String CLEAR_DEFAULT_ATTRIBUTE_ALL = "ClearDefaultAttribute.All";
    private static final String CLEAR_DEFAULT_ATTRIBUTE_RESET = "ClearDefaultAttribute.Reset";
    private static final String RANDOM_STRING = "RandomString";

    private static final File FILE = new File(SXAttribute.getPlugin().getDataFolder(), "Config.yml");
    @Getter
    private static YamlConfiguration config;
    @Getter
    private static boolean itemUpdate;
    @Getter
    private static boolean healthNameVisible;
    @Getter
    private static boolean healthBossBar;
    @Getter
    private static boolean healthScaled;
    @Getter
    private static boolean holographic;
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
    private static boolean clearDefaultAttributePlugin;
    @Getter
    private static boolean clearDefaultAttributeAll;
    @Getter
    private static boolean clearDefaultAttributeReset = false;
    @Getter
    private static boolean randomString;
    @Getter
    private static boolean registerSlot;
    @Getter
    private static boolean registerSlotsLock;

    /**
     * 创建默认Config文件
     */
    private static void createDefaultConfig() {
        config.set(CONFIG_VERSION, SXAttribute.getPlugin().getDescription().getVersion());
        config.set(DECIMAL_FORMAT, "#.##");
        // 物品更新机制
        config.set(ITEM_UPDATE_ENABLED, false);
        // 全息显示
        config.set(HOLOGRAPHIC_ENABLED, true);
        config.set(HOLOGRAPHIC_DISPLAY_TIME, 40);
        config.set(HOLOGRAPHIC_HEALTH_TAKE_ENABLED, false);
        // 血量头顶显示
        config.set(HEALTH_NAME_VISIBLE_ENABLED, true);
        config.set(HEALTH_NAME_VISIBLE_SIZE, 10);
        config.set(HEALTH_NAME_VISIBLE_CURRENT, "❤");
        config.set(HEALTH_NAME_VISIBLE_LOSS, "&7❤");
        config.set(HEALTH_NAME_VISIBLE_PREFIX, "&8[&c");
        config.set(HEALTH_NAME_VISIBLE_SUFFIX, "&8] &7- &8[&c{0}&8]");
        config.set(HEALTH_NAME_VISIBLE_DISPLAY_TIME, 4);
        // 血量显示
        config.set(HEALTH_BOSS_BAR_ENABLED, true);
        config.set(HEALTH_BOSS_BAR_FORMAT, "&a&l{0}:&8&l[&a&l{1}&7&l/&c&l{2}&8&l]");
        config.set(HEALTH_BOSS_BAR_DISPLAY_TIME, 4);
        // 血条压缩
        config.set(HEALTH_SCALED_ENABLED, true);
        config.set(HEALTH_SCALED_VALUE, 40);
        // 展示物品名称
        config.set(ITEM_DISPLAY_NAME, true);
        // 怪V怪的属性计算
        config.set(DAMAGE_CALCULATION_TO_EVE, false);
        // 伤害计量器
        config.set(DAMAGE_GAUGES, true);
        // 伤害计量器
        config.set(DAMAGE_GAUGES, true);
        // 禁止盾牌右键
        config.set(BAN_SHIELD_DEFENSE, false);
        // 清除默认属性标签
        config.set(CLEAR_DEFAULT_ATTRIBUTE_THIS_PLUGIN, true);
        config.set(CLEAR_DEFAULT_ATTRIBUTE_ALL, false);
        config.set(CLEAR_DEFAULT_ATTRIBUTE_RESET, false);
        // 不读取的槽位
        config.set(PRG_INVENTORY__WHITE_SLOT, Arrays.asList(5, 12, 21));
        // 随机字符串
        config.set(RANDOM_STRING, true);
        // 修复价格
        config.set(REPAIR_ITEM_VALUE, 3.5);
        // 注册槽位
        config.set(REGISTER_SLOTS_ENABLED, false);
        config.set(REGISTER_SLOTS_LIST, Arrays.asList("17#戒指#421", "26#项链#421", "35#项链#421"));
        // 是否锁槽
        config.set(REGISTER_SLOTS_LOCK_ENABLED, false);
        config.set(REGISTER_SLOTS_LOCK_NAME, "&7&o%SlotName%槽");
        // 默认属性
        config.set(DEFAULT_STATS, Arrays.asList("生命上限: 20", "暴击伤害: 100", "速度: 100"));

        config.set(NAME_HAND_MAIN, "主武器");
        config.set(NAME_HAND_OFF, "副武器");
        config.set(NAME_ARMOR, Arrays.asList("头盔", "盔甲", "护腿", "靴子"));
        config.set(NAME_ROLE, "限制职业");
        config.set(NAME_LIMIT_LEVEL, "限制等级");
        config.set(NAME_EXP_ADDITION, "经验加成");
        config.set(NAME_DURABILITY, "耐久度");
        config.set(NAME_SELL, "出售价格");
        config.set(NAME_EXPIRY_TIME, "到期时间");
        config.set(FORMAT_EXPIRY_TIME, "yyyy/MM/dd HH:mm");
        config.set(NAME_SPEED, "速度");
        config.set(NAME_ATTACK_SPEED, "攻击速度");

        config.set(NAME_HEALTH, "生命上限");
        config.set(NAME_HEALTH_REGEN, "生命恢复");
        config.set(NAME_DODGE, "闪避几率");
        config.set(NAME_DEFENSE, "防御力");
        config.set(NAME_PVP_DEFENSE, "PVP防御力");
        config.set(NAME_PVE_DEFENSE, "PVE防御力");
        config.set(NAME_TOUGHNESS, "韧性");
        config.set(NAME_REFLECTION_RATE, "反射几率");
        config.set(NAME_REFLECTION, "反射伤害");
        config.set(NAME_BLOCK_RATE, "格挡几率");
        config.set(NAME_BLOCK, "格挡伤害");

        config.set(NAME_DAMAGE, "攻击力");
        config.set(NAME_PVP_DAMAGE, "PVP攻击力");
        config.set(NAME_PVE_DAMAGE, "PVE攻击力");
        config.set(NAME_HIT_RATE, "命中几率");
        config.set(NAME_REAL, "破甲几率");
        config.set(NAME_CRIT_RATE, "暴击几率");
        config.set(NAME_CRIT, "暴击伤害");
        config.set(NAME_LIFE_STEAL_RATE, "吸血几率");
        config.set(NAME_LIFE_STEAL, "吸血倍率");
        config.set(NAME_IGNITION, "点燃几率");
        config.set(NAME_WITHER, "凋零几率");
        config.set(NAME_POISON, "中毒几率");
        config.set(NAME_BLINDNESS, "失明几率");
        config.set(NAME_SLOWNESS, "缓慢几率");
        config.set(NAME_LIGHTNING, "雷霆几率");
        config.set(NAME_TEARING, "撕裂几率");

        config.set(VALUE_EXP_ADDITION, 1);
        config.set(VALUE_SPEED, 1);

        config.set(VALUE_HEALTH, 1);
        config.set(VALUE_HEALTH_REGEN, 1);
        config.set(VALUE_DODGE, 1);
        config.set(VALUE_DEFENSE, 1);
        config.set(VALUE_PVP_DEFENSE, 1);
        config.set(VALUE_PVE_DEFENSE, 1);
        config.set(VALUE_TOUGHNESS, 1);
        config.set(VALUE_REFLECTION_RATE, 1);
        config.set(VALUE_REFLECTION, 1);
        config.set(VALUE_BLOCK_RATE, 1);
        config.set(VALUE_BLOCK, 1);

        config.set(VALUE_DAMAGE, 1);
        config.set(VALUE_PVP_DAMAGE, 1);
        config.set(VALUE_PVE_DAMAGE, 1);
        config.set(VALUE_HIT_RATE, 1);
        config.set(VALUE_REAL, 1);
        config.set(VALUE_CRIT_RATE, 1);
        config.set(VALUE_CRIT, 1);
        config.set(VALUE_LIFE_STEAL, 1);
        config.set(VALUE_LIFE_STEAL_RATE, 1);
        config.set(VALUE_IGNITION, 1);
        config.set(VALUE_WITHER, 1);
        config.set(VALUE_POISON, 1);
        config.set(VALUE_BLINDNESS, 1);
        config.set(VALUE_SLOWNESS, 1);
        config.set(VALUE_LIGHTNING, 1);
        config.set(VALUE_TEARING, 1);

        config.set(PRIORITY_EXP_ADDITION, 25);
        config.set(PRIORITY_SPEED, 26);

        config.set(PRIORITY_DODGE, 1);
        config.set(PRIORITY_DEFENSE, 6);
        config.set(PRIORITY_REFLECTION, 7);
        config.set(PRIORITY_BLOCK, 8);
        config.set(PRIORITY_TOUGHNESS, 101);
        config.set(PRIORITY_HEALTH, 102);
        config.set(PRIORITY_HEALTH_REGEN, 103);

        config.set(PRIORITY_HIT_RATE, 100);
        config.set(PRIORITY_DAMAGE, 3);
        config.set(PRIORITY_CRIT, 4);
        config.set(PRIORITY_REAL, 5);
        config.set(PRIORITY_LIFE_STEAL, 9);
        config.set(PRIORITY_IGNITION, 10);
        config.set(PRIORITY_WITHER, 11);
        config.set(PRIORITY_POISON, 12);
        config.set(PRIORITY_BLINDNESS, 13);
        config.set(PRIORITY_SLOWNESS, 14);
        config.set(PRIORITY_LIGHTNING, 15);
        config.set(PRIORITY_TEARING, 16);

        config.set(PRIORITY_HAND, 1);
        config.set(PRIORITY_MAIN_HAND, 2);
        config.set(PRIORITY_OFF_HAND, 3);
        config.set(PRIORITY_LIMIT_LEVEL, 4);
        config.set(PRIORITY_ROLE, 5);
        config.set(PRIORITY_EXPIRY_TIME, 6);
        config.set(PRIORITY_ATTACK_SPEED, 7);
    }

    /**
     * 检查版本更新
     *
     * @return boolean
     * @throws IOException IOException
     */
    private static boolean detectionVersion() throws IOException {
        if (!config.getString(CONFIG_VERSION,"").equals(SXAttribute.getPlugin().getDescription().getVersion())) {
            config.save(new File(FILE.toString().replace(".yml", "_" + config.getString(CONFIG_VERSION) + ".yml")));
            config = new YamlConfiguration();
            createDefaultConfig();
            return true;
        }
        return false;
    }

    /**
     * 加载Config类
     *
     * @throws IOException                   IOException
     * @throws InvalidConfigurationException InvalidConfigurationException
     */
    public static void loadConfig() throws IOException, InvalidConfigurationException {
        config = new YamlConfiguration();
        if (!FILE.exists()) {
            Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§cCreate Config.yml");
            createDefaultConfig();
            config.save(FILE);
        } else {
            config.load(FILE);
            if (detectionVersion()) {
                config.save(FILE);
                Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "§eUpdate Config.yml");
            } else {
                Bukkit.getConsoleSender().sendMessage(Message.getMessagePrefix() + "Find Config.yml");
            }
        }
        SXAttribute.setDf(new DecimalFormat(config.getString(DECIMAL_FORMAT)));
        SXAttribute.setSdf(new SimpleDateFormat(config.getString(FORMAT_EXPIRY_TIME)));
        itemUpdate = config.getBoolean(ITEM_UPDATE_ENABLED);
        healthNameVisible = config.getBoolean(HEALTH_NAME_VISIBLE_ENABLED);
        healthBossBar = config.getBoolean(HEALTH_BOSS_BAR_ENABLED);
        healthScaled = config.getBoolean(HEALTH_SCALED_ENABLED);
        holographic = config.getBoolean(HOLOGRAPHIC_ENABLED);
        holographicHealthTake = config.getBoolean(HOLOGRAPHIC_HEALTH_TAKE_ENABLED);
        itemDisplayName = config.getBoolean(ITEM_DISPLAY_NAME);
        damageCalculationToEVE = config.getBoolean(DAMAGE_CALCULATION_TO_EVE);
        damageGauges = config.getBoolean(DAMAGE_GAUGES);
        clearDefaultAttributePlugin = config.getBoolean(CLEAR_DEFAULT_ATTRIBUTE_THIS_PLUGIN);
        clearDefaultAttributeAll = config.getBoolean(CLEAR_DEFAULT_ATTRIBUTE_ALL);
        // 当 All Plugin 开启时 重置为false，否则读取配置文件
        // 意思是，开启消除默认标签时，该功能不启用
        clearDefaultAttributeReset = (!clearDefaultAttributePlugin || !clearDefaultAttributeAll) && config.getBoolean(CLEAR_DEFAULT_ATTRIBUTE_RESET);
        banShieldDefense = config.getBoolean(BAN_SHIELD_DEFENSE);
        randomString = config.getBoolean(RANDOM_STRING);
        registerSlot = config.getBoolean(REGISTER_SLOTS_ENABLED);
        registerSlotsLock = registerSlot && config.getBoolean(REGISTER_SLOTS_LOCK_ENABLED);
    }
}
