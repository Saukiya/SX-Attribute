package github.saukiya.sxattribute.feature.attribute;

import github.saukiya.sxattribute.data.attribute.SXAttributeData;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.util.AttributeConfig;
import github.saukiya.sxattribute.util.FormulaUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 属性定义的原子快照。
 */
public final class AttributeRegistry {

    private final Map<String, AttributeDefinition> definitions;
    private final Map<AttributeDefinition.AttributeTrigger, List<AttributeInvocation>> invocations;

    private AttributeRegistry(Map<String, AttributeDefinition> definitions,
                              Map<AttributeDefinition.AttributeTrigger, List<AttributeInvocation>> invocations) {
        this.definitions = Collections.unmodifiableMap(definitions);
        this.invocations = Collections.unmodifiableMap(invocations);
    }

    public static AttributeRegistry load() {
        Map<String, AttributeDefinition> definitions = new LinkedHashMap<>();
        Map<AttributeDefinition.AttributeTrigger, List<AttributeInvocation>> invocations = new LinkedHashMap<>();
        if (!AttributeConfig.isEnabled()) return new AttributeRegistry(definitions, invocations);
        for (String id : AttributeConfig.attributeNames()) {
            ConfigurationSection section = AttributeConfig.getSection(id);
            if (section == null || !AttributeConfig.isEnabled(id)) continue;
            AttributeDefinition definition = AttributeDefinition.parse(id, section);
            if (definition.getValues().isEmpty() && definition.getTriggers().isEmpty()) continue;
            definitions.put(id, definition);
            for (AttributeDefinition.TriggerDefinition trigger : definition.getTriggers()) {
                invocations.computeIfAbsent(trigger.getEvent(), ignored -> new ArrayList<>())
                        .add(new AttributeInvocation(definition, trigger));
            }
        }
        for (List<AttributeInvocation> values : invocations.values()) {
            values.sort(Comparator.comparingInt(AttributeInvocation::priority));
        }
        return new AttributeRegistry(definitions, invocations);
    }

    public Map<String, AttributeDefinition> definitions() {
        return definitions;
    }

    public List<AttributeInvocation> invocations(AttributeDefinition.AttributeTrigger trigger) {
        return invocations.getOrDefault(trigger, Collections.emptyList());
    }

    public void parseLore(SXAttributeData data, String lore) {
        for (AttributeDefinition definition : definitions.values()) {
            for (AttributeDefinition.ValueDefinition value : definition.getValues().values()) {
                if (!value.matches(lore)) continue;
                double parsed = SubAttribute.getNumber(lore);
                Map<String, Double> fields = data.getDynamicValues()
                        .computeIfAbsent(definition.getId(), ignored -> new LinkedHashMap<>());
                double aggregated = fields.containsKey(value.getField())
                        ? value.aggregate(fields.get(value.getField()), parsed) : parsed;
                fields.put(value.getField(), aggregated);
            }
        }
    }

    /**
     * 根据聚合后的原始字段生成派生属性。
     * <p>
     * 所有映射先读取同一份原始快照，再把结果作为标准属性文本送回解析器；因此目标既可为
     * 内置属性，也可为其它自定义属性，同时不会因配置顺序产生级联或循环放大。
     */
    public void applyMappings(SXAttributeData data, Player player) {
        Map<String, Map<String, Double>> snapshot = snapshot(data);
        Map<String, Double> sharedVariables = mappingVariables(snapshot);
        for (AttributeDefinition definition : definitions.values()) {
            Map<String, Double> sourceFields = snapshot.get(definition.getId());
            if (sourceFields == null) continue;
            Map<String, Double> variables = new LinkedHashMap<>(sharedVariables);
            appendSourceVariables(variables, definition, sourceFields);
            for (AttributeDefinition.MappingDefinition mapping : definition.getMappings()) {
                double sourceValue = sourceFields.getOrDefault(mapping.getSource(), 0D);
                if (sourceValue == 0D) continue;
                variables.put("value", sourceValue);
                // Scale 只负责计算“每点源属性对应多少目标属性”的倍率，本身也经过统一公式引擎。
                double scale = FormulaUtil.eval(player, mapping.getScale(), variables, mapping.getScaleFallback());
                // Formula 负责最终结果；不配置或求值失败时回退 value * scale，配置后可实现非线性或跨属性计算。
                variables.put("scale", scale);
                double fallback = sourceValue * scale;
                double mappedValue = FormulaUtil.eval(player, mapping.getFormula(), variables, fallback);
                if (!Double.isFinite(mappedValue) || mappedValue == 0D) continue;
                applyMappedLine(data, mapping.getTarget() + ": " + decimal(mappedValue));
            }
        }
    }

    /**
     * 当前定义的字段同时提供短变量、source_ 与 self_ 三种名称。
     * source_ 明确表达映射来源，self_ 与触发器公式保持一致，短变量便于同一属性内编写简洁公式。
     */
    private void appendSourceVariables(Map<String, Double> variables, AttributeDefinition definition,
                                       Map<String, Double> sourceFields) {
        // 未出现在任何来源中的声明字段也必须初始化为 0，保证可选变量不会让整条公式求值失败。
        definition.getValues().keySet().forEach(field -> putSourceVariable(variables, field, 0D));
        sourceFields.forEach((field, fieldValue) -> putSourceVariable(variables, field, fieldValue));
    }

    private void putSourceVariable(Map<String, Double> variables, String field, double fieldValue) {
        String safeField = safe(field);
        variables.put(safeField, fieldValue);
        variables.put("source_" + safeField, fieldValue);
        variables.put("self_" + safeField, fieldValue);
    }

    /**
     * 建立跨属性变量表，并为未取得值的声明字段提供 0；这样公式可以安全引用可选属性。
     */
    private Map<String, Double> mappingVariables(Map<String, Map<String, Double>> snapshot) {
        Map<String, Double> variables = new LinkedHashMap<>();
        definitions.values().forEach(definition -> definition.getValues().keySet().forEach(field ->
                variables.put(safe(definition.getId()) + "_" + safe(field), 0D)));
        snapshot.forEach((attributeId, fields) -> fields.forEach((field, value) ->
                variables.put(safe(attributeId) + "_" + safe(field), value)));
        return variables;
    }

    private Map<String, Map<String, Double>> snapshot(SXAttributeData data) {
        Map<String, Map<String, Double>> snapshot = new LinkedHashMap<>();
        data.getDynamicValues().forEach((attributeId, fields) ->
                snapshot.put(attributeId, new LinkedHashMap<>(fields)));
        return snapshot;
    }

    /**
     * 复用 Lore 识别协议是映射兼容旧属性数组的关键；只写 dynamicValues 会导致 Damage、Crit 等旧行为读取不到派生值。
     */
    private void applyMappedLine(SXAttributeData data, String lore) {
        for (SubAttribute attribute : SubAttribute.getAttributes()) {
            if (AttributeConfig.isEnabled(attribute.getName())) {
                attribute.loadAttribute(data.getValues()[attribute.getPriority()], lore);
            }
        }
        parseLore(data, lore);
    }

    private String safe(String value) {
        return value.replaceAll("[^A-Za-z0-9_]", "_");
    }

    /** BigDecimal 避免科学计数法被旧 Lore 数字解析器截断。 */
    private String decimal(double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }

    public void correct(SXAttributeData data) {
        for (AttributeDefinition definition : definitions.values()) {
            Map<String, Double> fields = data.getDynamicValues().get(definition.getId());
            if (fields == null) continue;
            definition.getValues().forEach((field, value) -> fields.computeIfPresent(field,
                    (ignored, current) -> value.correct(current)));
        }
    }

    /** 按字段声明的 Aggregate 规则合并两个动态数据快照。 */
    public void merge(SXAttributeData target, SXAttributeData addition) {
        addition.getDynamicValues().forEach((attributeId, fields) -> {
            AttributeDefinition definition = definitions.get(attributeId);
            Map<String, Double> targetFields = target.getDynamicValues()
                    .computeIfAbsent(attributeId, ignored -> new LinkedHashMap<>());
            fields.forEach((field, value) -> {
                AttributeDefinition.ValueDefinition valueDefinition = definition == null
                        ? null : definition.getValues().get(field);
                double merged = !targetFields.containsKey(field) ? value
                        : valueDefinition == null ? targetFields.get(field) + value
                        : valueDefinition.aggregate(targetFields.get(field), value);
                targetFields.put(field, merged);
            });
        });
    }

    public double combatPower(SXAttributeData data) {
        double result = 0D;
        for (AttributeDefinition definition : definitions.values()) {
            for (AttributeDefinition.ValueDefinition value : definition.getValues().values()) {
                result += value.combatContribution(data.getDynamicValue(definition.getId(), value.getField()));
            }
        }
        return result;
    }

    public static final class AttributeInvocation {
        private final AttributeDefinition definition;
        private final AttributeDefinition.TriggerDefinition trigger;

        private AttributeInvocation(AttributeDefinition definition, AttributeDefinition.TriggerDefinition trigger) {
            this.definition = definition;
            this.trigger = trigger;
        }

        public AttributeDefinition definition() {
            return definition;
        }

        public AttributeDefinition.TriggerDefinition trigger() {
            return trigger;
        }

        private int priority() {
            return trigger.getPriority() * 100000 + definition.getPriority();
        }
    }
}
