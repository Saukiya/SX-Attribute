package github.saukiya.sxattribute.data.attribute.sub.defence;

import com.sucy.skill.SkillAPI;
import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.AttributeSource;
import github.saukiya.sxattribute.data.attribute.AttributeType;
import github.saukiya.sxattribute.data.attribute.SXAttributeData;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.eventdata.EventData;
import github.saukiya.sxattribute.data.eventdata.sub.UpdateData;
import github.saukiya.sxattribute.util.AttributeUtil;
import org.bukkit.Bukkit;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.spigotmc.SpigotConfig;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * 生命 - 当前不更新怪物生命
 *
 * @author Saukiya
 */
public class Health extends SubAttribute {

    /**
     * beta.6 使用过的 SX 最大生命修饰符协议 UUID。
     * <p>
     * 切回 base value 写入时仍需按此 UUID 清理旧数据；不得修改，否则升级玩家会残留重复生命。
     */
    private static final UUID SX_HEALTH_MODIFIER_ID = UUID.fromString("ef3a83e2-6a7b-4ced-939d-b73f0afab11f");

    /** SkillAPI 的职业生命由 SX 公式显式合并，仅作为只读来源展示。 */
    private static final String SKILL_API_SOURCE = "SkillAPI-生命";

    /** Bukkit 最终生命中除 SX 与 SkillAPI 显式贡献外的部分，仅作为只读来源展示。 */
    private static final String EXTERNAL_HEALTH_SOURCE = "外部插件-生命";

    /** 浮点属性比较容差，避免重复刷新 Bukkit 属性及客户端同步。 */
    private static final double ATTRIBUTE_EPSILON = 0.000001D;

    /**
     * 最大生命写入协议。
     * HEALTH_SCALED 兼容 3.9.2 的 base value 与客户端血条压缩；ATTRIBUTE_MODIFIER 兼容 beta.6 的叠加方式。
     */
    private enum HealthMode {
        HEALTH_SCALED,
        ATTRIBUTE_MODIFIER
    }

    private boolean skillAPI = false;

    private boolean healthScaled = false;

    private int healthScaledValue = 40;

    private HealthMode healthMode = HealthMode.HEALTH_SCALED;

    /**
     * 记录本次运行期间由 SX 写入的 base value，供热切换到修饰器模式时安全释放。
     * 只有当前值仍等于记录值才恢复默认值，避免覆盖其他插件之后写入的 base value。
     * 弱键不会因模式切换记录而长期保留已离线的 Player 对象。
     */
    private final Map<Player, Double> sxBaseValues = new WeakHashMap<>();

    /**
     * 生命
     * double[0] 生命值
     */
    public Health() {
        super(SXAttribute.getInst(), 1, AttributeType.UPDATE);
    }

    /**
     * 补齐最大生命模式与客户端缩放的默认配置，使旧配置缺少新键时安全回退到 3.9.2 行为。
     */
    @Override
    protected YamlConfiguration defaultConfig(YamlConfiguration config) {
        config.set("Health.Mode", HealthMode.HEALTH_SCALED.name());
        config.set("Health.DiscernName", "生命上限");
        config.set("Health.CombatPower", 1);
        config.set("HealthScaled.Enabled", true);
        config.set("HealthScaled.Value", 40);
        return config;
    }

    /**
     * 在属性刷新阶段按选定协议写入玩家最大生命，并在最终 Bukkit 上限确定后修正当前生命与血条显示。
     */
    @Override
    public void eventMethod(double[] values, EventData eventData) {
        if (eventData instanceof UpdateData && ((UpdateData) eventData).getEntity() instanceof Player) {
            Player player = (Player) ((UpdateData) eventData).getEntity();
            int skillAPIHealth = getSkillAPIHealth(player);
            // 生命上限公式 (变量 value=生命上限词条, skillapi=SkillAPI 附加生命); 默认 词条+SkillAPI
            double maxHealth = formula(player, "Formula", values[0] + skillAPIHealth, "value", values[0], "skillapi", skillAPIHealth);
            if (SXAttribute.isHigherVersion()) {
                AttributeInstance instance = AttributeUtil.getInstance(player, "MAX_HEALTH", "GENERIC_MAX_HEALTH");
                if (instance != null) {
                    if (healthMode == HealthMode.ATTRIBUTE_MODIFIER) {
                        applyHealthModifier(player, instance, maxHealth);
                    } else {
                        applyHealthBaseValue(player, instance, maxHealth);
                    }
                    updateInformationalSource(player, SKILL_API_SOURCE, skillAPIHealth);
                    updateInformationalSource(player, EXTERNAL_HEALTH_SOURCE, instance.getValue() - maxHealth);
                } else {
                    player.setMaxHealth(maxHealth);
                }
            } else {
                player.setMaxHealth(maxHealth);
            }
            // 必须在 Bukkit 合并外部 base/modifier 后再截断当前生命，否则会把合法的外部生命误降到 SX 值。
            double effectiveMaxHealth = SXAttribute.getApi().getMaxHealth(player);
            if (player.getHealth() > effectiveMaxHealth) player.setHealth(effectiveMaxHealth);
            if (healthMode == HealthMode.HEALTH_SCALED) {
                applyHealthScale(player);
            } else {
                // 修饰器模式不接管客户端血条显示，避免两个互斥协议同时生效。
                player.setHealthScaled(false);
            }
        }
    }

    /**
     * 使用与 3.9.2 一致的 base value 写入，保证 Bukkit/Paper 的生命缩放同步链能够稳定识别上限变化。
     * 外部 {@link AttributeModifier} 仍会叠加；其他插件若也直接写 base value，则最后写入者生效。
     */
    private void applyHealthBaseValue(Player player, AttributeInstance instance, double sxMaxHealth) {
        for (AttributeModifier modifier : instance.getModifiers()) {
            if (SX_HEALTH_MODIFIER_ID.equals(modifier.getUniqueId())) {
                instance.removeModifier(modifier);
                break;
            }
        }
        if (Math.abs(instance.getBaseValue() - sxMaxHealth) > ATTRIBUTE_EPSILON) instance.setBaseValue(sxMaxHealth);
        sxBaseValues.put(player, sxMaxHealth);
    }

    /**
     * 以 beta.6 的固定 UUID 修饰器写入 SX 相对原版默认生命的增量。
     * 固定 UUID 是升级与重复刷新时识别 SX 所有权的协议，不得随意更换。
     */
    private void applyHealthModifier(Player player, AttributeInstance instance, double sxMaxHealth) {
        releaseOwnedBaseValue(player, instance);
        double amount = sxMaxHealth - instance.getDefaultValue();
        AttributeModifier current = null;
        for (AttributeModifier modifier : instance.getModifiers()) {
            if (SX_HEALTH_MODIFIER_ID.equals(modifier.getUniqueId())) {
                current = modifier;
                break;
            }
        }
        if (current != null && current.getOperation() == AttributeModifier.Operation.ADD_NUMBER
                && Math.abs(current.getAmount() - amount) <= ATTRIBUTE_EPSILON) {
            return;
        }
        if (current != null) instance.removeModifier(current);
        if (Math.abs(amount) > ATTRIBUTE_EPSILON) {
            instance.addModifier(new AttributeModifier(SX_HEALTH_MODIFIER_ID, "SX-Attribute max health", amount,
                    AttributeModifier.Operation.ADD_NUMBER));
        }
    }

    /**
     * 热重载切换模式时只释放仍由 SX 持有的 base value。
     * 若其他插件已经改写该值，则保留现场，避免模式切换破坏外部插件状态。
     */
    private void releaseOwnedBaseValue(Player player, AttributeInstance instance) {
        Double sxBaseValue = sxBaseValues.remove(player);
        if (sxBaseValue != null && Math.abs(instance.getBaseValue() - sxBaseValue) <= ATTRIBUTE_EPSILON) {
            instance.setBaseValue(instance.getDefaultValue());
        }
    }

    /**
     * 按最终 Bukkit 最大生命决定是否启用客户端生命缩放。
     * 外部插件贡献也属于最终上限，因此不能只用 SX 词条值判断是否需要压缩。
     */
    private void applyHealthScale(Player player) {
        if (healthScaled && healthScaledValue < SXAttribute.getApi().getMaxHealth(player)) {
            player.setHealthScaled(true);
            player.setHealthScale(healthScaledValue);
        } else {
            player.setHealthScaled(false);
        }
    }

    /**
     * 更新只读诊断来源。该来源不参与 SX 求和，只让 source/statssource 能解释最终生命差额。
     */
    private void updateInformationalSource(Player player, String sourceName, double health) {
        if (Math.abs(health) <= ATTRIBUTE_EPSILON) {
            SXAttribute.getAttributeManager().removeSource(player.getUniqueId(), sourceName);
            return;
        }
        SXAttributeData data = new SXAttributeData();
        data.getValues(this)[0] = health;
        SXAttribute.getAttributeManager().putSource(player.getUniqueId(), new AttributeSource(sourceName, data, true, false));
    }

    /** 可选插件安装失败或运行中停用时不能中断 SX 自身的生命刷新。 */
    private int getSkillAPIHealth(Player player) {
        if (!skillAPI || !Bukkit.getPluginManager().isPluginEnabled("SkillAPI")) return 0;
        try {
            return SkillAPI.getPlayerData(player).getClasses().stream().mapToInt(aClass -> (int) aClass.getHealth()).sum();
        } catch (LinkageError failure) {
            // 存在同名插件并不保证提供旧 SkillAPI 接口；本轮停用联动，重载时允许重新探测。
            skillAPI = false;
            SXAttribute.getInst().getLogger().warning("SkillAPI health integration is unavailable: " + failure);
            return 0;
        }
    }

    /** 初始化生命协议配置，并探测可选的 SkillAPI 生命来源。 */
    @Override
    public void onEnable() {
        loadHealthSettings();
        skillAPI = Bukkit.getPluginManager().isPluginEnabled("SkillAPI");
    }

    /** 热重载生命协议、缩放参数与可选接口状态，玩家属性在下一次刷新时平滑切换。 */
    @Override
    public void onReLoad() {
        loadHealthSettings();
        skillAPI = Bukkit.getPluginManager().isPluginEnabled("SkillAPI");
    }

    /**
     * 读取最大生命协议及客户端缩放参数。
     * 非法模式回退到已在 Paper 26.1.2 验证过的 HEALTH_SCALED，避免配置拼写错误导致生命叠加异常。
     */
    private void loadHealthSettings() {
        String configuredMode = config().getString("Health.Mode", HealthMode.HEALTH_SCALED.name());
        try {
            healthMode = HealthMode.valueOf(configuredMode.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException exception) {
            healthMode = HealthMode.HEALTH_SCALED;
            SXAttribute.getInst().getLogger().warning("Unknown Health.Mode '" + configuredMode
                    + "'; using HEALTH_SCALED.");
        }
        healthScaled = config().getBoolean("HealthScaled.Enabled");
        healthScaledValue = config().getInt("HealthScaled.Value", 40);
    }

    @Override
    public Object getPlaceholder(double[] values, Player player, String string) {
        switch (string) {
            case "MaxHealth":
                return SXAttribute.getApi().getMaxHealth(player);
            case "Health":
                return player.getHealth();
            case "HealthValue":
                return values[0];
            default:
                return null;
        }
    }

    @Override
    public List<String> getPlaceholders() {
        return Arrays.asList(
                "MaxHealth",
                "Health",
                "HealthValue"
        );
    }

    @Override
    public void loadAttribute(double[] values, String lore) {
        if (lore.contains(getString("Health.DiscernName"))) {
            values[0] += getNumber(lore);
        }
    }

    @Override
    public void correct(double[] values) {
        values[0] = Math.max(values[0], 1D);
        values[0] = Math.min(values[0], SpigotConfig.maxHealth);
    }

    @Override
    public double calculationCombatPower(double[] values) {
        return values[0] * config().getInt("Health.CombatPower");
    }
}
