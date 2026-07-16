package github.saukiya.sxattribute.data.attribute;

import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * 实体属性存储区
 *
 * @author Saukiya
 */
public class SXAttributeData {

    @Getter
    private double combatPower = 0D;

    @Getter
    private double[][] values = new double[SubAttribute.getAttributes().size()][];

    /**
     * 配置化属性数据区：属性 ID -> 命名字段 -> 数值。
     * <p>
     * 旧二维数组继续服务既有 SubAttribute/API；新属性使用稳定字符串 ID，因而可在热重载时
     * 新增或删除定义，而不需要重新分配全服实体的数组槽位。
     */
    @Getter
    private final Map<String, Map<String, Double>> dynamicValues = new LinkedHashMap<>();

    public SXAttributeData() {
        for (SubAttribute attribute : SubAttribute.getAttributes()) {
            values[attribute.getPriority()] = new double[attribute.getLength()];
        }
    }

    public double[] getValues(String attributeName) {
        return getValues(SubAttribute.getSubAttribute(attributeName));
    }

    public double[] getValues(SubAttribute attribute) {
        return attribute != null ? getValues()[attribute.getPriority()] : new double[12];
    }

    public boolean isValid() {
        return IntStream.range(0, getValues().length).anyMatch(i -> IntStream.range(0, getValues()[i].length).anyMatch(i1 -> getValues()[i][i1] != 0))
                || dynamicValues.values().stream().anyMatch(fields -> fields.values().stream().anyMatch(value -> value != 0D));
    }

    public boolean isValid(SubAttribute attribute) {
        double[] values = getValues(attribute);
        return IntStream.range(0, values.length).anyMatch(i -> values[i] != 0);
    }

    /**
     * 增加另一个SXAttributeData的数据
     *
     * @param attributeData SXAttributeData
     * @return this
     */
    public SXAttributeData add(SXAttributeData attributeData) {
        if (attributeData != null && attributeData.isValid()) {
            for (int i = 0; i < getValues().length; i++) {
                for (int i1 = 0; i1 < getValues()[i].length; i1++) {
                    getValues()[i][i1] += attributeData.getValues()[i][i1];
                }
            }
            if (github.saukiya.sxattribute.SXAttribute.getAttributeEngine() != null) {
                github.saukiya.sxattribute.SXAttribute.getAttributeEngine().merge(this, attributeData);
            } else {
                attributeData.dynamicValues.forEach((attributeId, fields) -> fields.forEach((field, value) ->
                        addDynamicValue(attributeId, field, value)));
            }
        }
        return this;
    }

    /**
     * 减去另一个SXAttributeData的数据
     *
     * @param attributeData SXAttributeData
     * @return this
     */
    public SXAttributeData take(SXAttributeData attributeData) {
        if (attributeData != null && attributeData.isValid()) {
            for (int i = 0; i < getValues().length; i++) {
                for (int i1 = 0; i1 < getValues()[i].length; i1++) {
                    getValues()[i][i1] -= attributeData.getValues()[i][i1];
                }
            }
            attributeData.dynamicValues.forEach((attributeId, fields) -> fields.forEach((field, value) ->
                    addDynamicValue(attributeId, field, -value)));
        }
        return this;
    }

    /**
     * 按属性 ID 与字段名累加动态值。
     */
    public void addDynamicValue(String attributeId, String field, double value) {
        dynamicValues.computeIfAbsent(attributeId, ignored -> new LinkedHashMap<>())
                .merge(field, value, Double::sum);
    }

    /**
     * 读取动态属性字段；未定义时返回 0，便于公式直接引用。
     */
    public double getDynamicValue(String attributeId, String field) {
        Map<String, Double> fields = dynamicValues.get(attributeId);
        return fields == null ? 0D : fields.getOrDefault(field, 0D);
    }

    /**
     * 将属性计算成战斗点数
     *
     * @return double 战斗点数
     */
    public double calculationCombatPower() {
        this.combatPower = 0D;
        for (SubAttribute attribute : SubAttribute.getAttributes()) {
            this.combatPower += attribute.calculationCombatPower(getValues()[attribute.getPriority()]);
        }
        if (github.saukiya.sxattribute.SXAttribute.getAttributeEngine() != null) {
            this.combatPower += github.saukiya.sxattribute.SXAttribute.getAttributeEngine().combatPower(this);
        }
        return this.combatPower;
    }

    /**
     * 纠正数据范围
     */
    public void correct() {
        for (SubAttribute attribute : SubAttribute.getAttributes()) {
            attribute.correct(getValues()[attribute.getPriority()]);
        }
        if (github.saukiya.sxattribute.SXAttribute.getAttributeEngine() != null) {
            github.saukiya.sxattribute.SXAttribute.getAttributeEngine().correct(this);
        }
    }
}
