package github.saukiya.sxattribute.data.attribute.sub.update;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.AttributeType;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.eventdata.EventData;
import github.saukiya.sxattribute.data.eventdata.sub.UpdateData;
import github.saukiya.sxattribute.util.AttributeConfig;
import github.saukiya.sxattribute.util.FormulaUtil;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 原版属性包装类 (数据驱动, 跨版本安全)
 * <p>
 * 统一封装"读取物品词条数值 -> 施加到原版 AttributeInstance"这一模式。所有元数据
 * (registryKey / legacyName / 识别名 / 默认基值 / 上下限 / 增幅模式 / 生效公式 / 战力系数)
 * 均来自 {@code Attributes.yml} 的对应节点, 由 {@link SXAttribute} 在 onLoad 按版本门控实例化注册,
 * 无需一属性一 Java 类。
 * <p>
 * 生效值优先用 SX-Item 公式 ({@link FormulaUtil}); 未安装 SX-Item 或公式为空时回退 {@link Mode} 预设。
 * 通过 {@link github.saukiya.sxattribute.util.AttributeUtil} 按 registry key(新版) 优先、枚举名(旧版) 回退解析属性,
 * 属性在当前版本不存在时静默跳过, 不报错。
 *
 * @author Ray_Hughes
 */
public class VanillaUpdateAttribute extends SubAttribute {

    /**
     * 增幅公式模式 (无 Formula 时的回退预设)
     * <p>
     * 玩家在物品词条上一律填写整数百分比/绝对量(如 +50), 内部按模式换算, 面板只展示玩家填写的数值。
     */
    public enum Mode {
        /** 百分比增幅: 最终值 = 默认基值 × (100 + 增幅) / 100, 玩家填 +50 表示 +50% */
        PERCENT,
        /** 绝对值叠加: 最终值 = 默认基值 + 增量, 用于距离/幸运/氧气等非百分比属性 */
        ADD,
        /** 0~1 系数: 最终值 = 默认基值 + 百分点 / 100, 玩家填 +50 表示 +0.5, 结果由 correct 限幅 */
        RATIO
    }

    private String registryKey;
    private String legacyName;
    private String discernName;
    private double defaultValue;
    private double minAmount;
    private double maxAmount;
    private Mode mode;
    private String formula;
    private double combatPower;

    /**
     * @param name 属性名 (= Attributes.yml 节点键, 也是占位符名)
     * @param sec  该属性在 Attributes.yml 的配置节点
     */
    public VanillaUpdateAttribute(String name, ConfigurationSection sec) {
        super(name, SXAttribute.getInst(), 1, AttributeType.UPDATE);
        apply(sec);
    }

    /**
     * 从配置节点读取并缓存全部元数据 (构造与重载时调用)
     *
     * @param sec 配置节点
     */
    private void apply(ConfigurationSection sec) {
        this.registryKey = sec.getString("RegistryKey", getName().toLowerCase());
        this.legacyName = sec.getString("LegacyName", getName().toUpperCase());
        this.discernName = sec.getString("DiscernName", getName());
        this.defaultValue = sec.getDouble("Default", 0D);
        String modeStr = sec.getString("Mode", "ADD");
        try {
            this.mode = Mode.valueOf(modeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            this.mode = Mode.ADD;
        }
        // 未显式配置上下限时按模式取默认值 (RATIO 0~100, 其余 -100~MAX)
        this.minAmount = sec.getDouble("Min", this.mode == Mode.RATIO ? 0 : -100);
        this.maxAmount = sec.getDouble("Max", this.mode == Mode.RATIO ? 100 : Integer.MAX_VALUE);
        this.formula = sec.getString("Formula");
        this.combatPower = sec.getDouble("CombatPower", 0D);
    }

    @Override
    protected YamlConfiguration defaultConfig(YamlConfiguration config) {
        // 数据源为 Attributes.yml, 不生成独立 <Name>.yml
        return null;
    }

    @Override
    public void onReLoad() {
        // 重载时从刷新后的 Attributes.yml 重新读取数值/公式 (注册集合固定, 开关变更需重启)
        ConfigurationSection sec = AttributeConfig.getSection(getName());
        if (sec != null) {
            apply(sec);
        }
    }

    @Override
    public void eventMethod(double[] values, EventData eventData) {
        if (eventData instanceof UpdateData && ((UpdateData) eventData).getEntity() instanceof Player) {
            Player player = (Player) ((UpdateData) eventData).getEntity();
            AttributeInstance instance = github.saukiya.sxattribute.util.AttributeUtil.getInstance(player, registryKey, legacyName);
            if (instance != null) {
                instance.setBaseValue(compute(player, values[0], instance.getBaseValue()));
            }
        }
    }

    /**
     * 计算施加到原版 AttributeInstance 的最终基值
     * <p>
     * 优先走 SX-Item 公式 (变量 {@code <l:value>/<l:default>/<l:base>/<l:min>/<l:max>});
     * 公式不可用时回退 {@link Mode} 预设。
     *
     * @param player 玩家 (供公式内 PlaceholderAPI)
     * @param amount 玩家词条累加值
     * @param base   原版当前基值
     * @return 最终基值
     */
    protected double compute(Player player, double amount, double base) {
        if (FormulaUtil.isUsable() && formula != null && !formula.isEmpty()) {
            Map<String, Double> vars = new HashMap<>();
            vars.put("value", amount);
            vars.put("default", defaultValue);
            vars.put("base", base);
            vars.put("min", minAmount);
            vars.put("max", maxAmount);
            return FormulaUtil.eval(player, formula, vars, fallbackCompute(amount));
        }
        return fallbackCompute(amount);
    }

    /**
     * Mode 预设算法 (无公式时的回退)
     *
     * @param amount 玩家词条累加值
     * @return 最终基值
     */
    private double fallbackCompute(double amount) {
        switch (mode) {
            case PERCENT:
                return defaultValue * (100 + amount) / 100D;
            case RATIO:
                return defaultValue + amount / 100D;
            case ADD:
            default:
                return defaultValue + amount;
        }
    }

    @Override
    public void loadAttribute(double[] values, String lore) {
        if (lore.contains(discernName)) {
            values[0] += getNumber(lore);
        }
    }

    @Override
    public Object getPlaceholder(double[] values, Player player, String string) {
        return string.equals(getName()) ? values[0] : null;
    }

    @Override
    public List<String> getPlaceholders() {
        return Collections.singletonList(getName());
    }

    @Override
    public void correct(double[] values) {
        values[0] = Math.min(Math.max(values[0], minAmount), maxAmount);
    }

    @Override
    public double calculationCombatPower(double[] values) {
        return values[0] * combatPower;
    }
}
