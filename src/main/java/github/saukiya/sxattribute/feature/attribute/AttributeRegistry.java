package github.saukiya.sxattribute.feature.attribute;

import github.saukiya.sxattribute.data.attribute.SXAttributeData;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.util.AttributeConfig;
import org.bukkit.configuration.ConfigurationSection;

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
