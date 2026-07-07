package github.saukiya.sxattribute.data.attribute.sub.update;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.AttributeType;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.eventdata.EventData;
import github.saukiya.sxattribute.data.eventdata.sub.UpdateData;
import github.saukiya.sxattribute.util.AttributeUtil;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * 原版属性包装基类 (跨版本安全)
 * <p>
 * 统一封装"读取物品词条数值 -> 施加到原版 AttributeInstance"这一模式,
 * 消除各原版属性子类间的重复代码。子类仅需提供属性名对、默认基值、增幅模式等元数据。
 * <p>
 * 通过现有 {@link AttributeUtil} 按 registry key(新版) 优先、枚举名(旧版) 回退的方式解析属性,
 * 跨 1.20.5 ~ 26.x 安全; 属性在当前版本不存在时 {@code getInstance} 返回 null 并跳过, 静默降级不报错。
 *
 * @author Ray_Hughes
 */
public abstract class VanillaUpdateAttribute extends SubAttribute {

    /**
     * 增幅公式模式
     * <p>
     * 玩家在物品词条上一律填写整数百分比/绝对量(如 +50), 内部按模式换算, 面板只展示玩家填写的数值。
     */
    public enum Mode {
        /** 百分比增幅: 最终值 = 默认基值 × (100 + 增幅) / 100, 玩家填 +50 表示 +50% */
        PERCENT,
        /** 绝对值叠加: 最终值 = 默认基值 + 增量, 用于距离/幸运/氧气等非百分比属性 */
        ADD,
        /** 0~1 系数: 最终值 = 默认基值 + 百分点 / 100, 玩家填 +50 表示 +0.5, 结果由 correct 限幅到 0~1 */
        RATIO
    }

    /**
     * double[0] 增幅数值 (玩家填写的百分比 / 绝对量)
     */
    public VanillaUpdateAttribute() {
        super(SXAttribute.getInst(), 1, AttributeType.UPDATE);
    }

    /**
     * @return 原版 registry key (新版名, 如 "scale")
     */
    protected abstract String registryKey();

    /**
     * @return 旧版枚举名 (如 "GENERIC_SCALE"); 高版本 Registry-only 属性无枚举名, 返回占位大写名即可
     */
    protected abstract String legacyName();

    /**
     * @return 默认识别名 (物品词条前缀, 如 "体型增幅")
     */
    protected abstract String discernName();

    /**
     * @return 原版属性默认基值
     */
    protected abstract double baseValue();

    /**
     * @return 增幅公式模式
     */
    protected abstract Mode mode();

    /**
     * @return 增幅下界 (对 values[0] 限幅); RATIO 默认 0, 其余默认 -100 (即 -100% 归零)
     */
    protected double minAmount() {
        return mode() == Mode.RATIO ? 0 : -100;
    }

    /**
     * @return 增幅上界 (对 values[0] 限幅); RATIO 默认 100 (对应系数 1.0), 其余默认不限
     */
    protected double maxAmount() {
        return mode() == Mode.RATIO ? 100 : Integer.MAX_VALUE;
    }

    @Override
    protected YamlConfiguration defaultConfig(YamlConfiguration config) {
        String n = getName();
        config.set(n + ".DiscernName", discernName());
        config.set(n + ".Default", baseValue());
        config.set(n + ".CombatPower", 0);
        return config;
    }

    @Override
    public void eventMethod(double[] values, EventData eventData) {
        if (eventData instanceof UpdateData && ((UpdateData) eventData).getEntity() instanceof Player) {
            Player player = (Player) ((UpdateData) eventData).getEntity();
            AttributeInstance instance = AttributeUtil.getInstance(player, registryKey(), legacyName());
            if (instance != null) {
                instance.setBaseValue(compute(values[0]));
            }
        }
    }

    /**
     * 按增幅模式计算最终基值
     *
     * @param amount 玩家填写的增幅数值
     * @return 施加到原版 AttributeInstance 的最终基值
     */
    protected double compute(double amount) {
        double def = config().getDouble(getName() + ".Default", baseValue());
        switch (mode()) {
            case PERCENT:
                return def * (100 + amount) / 100D;
            case RATIO:
                return def + amount / 100D;
            case ADD:
            default:
                return def + amount;
        }
    }

    @Override
    public void loadAttribute(double[] values, String lore) {
        if (lore.contains(getString(getName() + ".DiscernName"))) {
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
        values[0] = Math.min(Math.max(values[0], minAmount()), maxAmount());
    }

    @Override
    public double calculationCombatPower(double[] values) {
        return values[0] * config().getInt(getName() + ".CombatPower");
    }
}
