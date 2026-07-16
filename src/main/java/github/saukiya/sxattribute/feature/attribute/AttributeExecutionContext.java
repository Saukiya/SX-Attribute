package github.saukiya.sxattribute.feature.attribute;

import github.saukiya.sxattribute.data.attribute.SXAttributeData;
import github.saukiya.sxattribute.data.eventdata.sub.DamageData;
import lombok.Getter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 一次属性触发的只读上下文。
 * <p>
 * 所有字符串 ID 会被转换为公式安全变量名，条件与动作共享同一变量快照，避免同一触发器
 * 因动作执行顺序不同而读到不一致的属性值。
 */
@Getter
public final class AttributeExecutionContext {

    private final LivingEntity attacker;
    private final LivingEntity defender;
    private final SXAttributeData attackerData;
    private final SXAttributeData defenderData;
    private final DamageData damageData;
    private final Map<String, Double> variables;

    public AttributeExecutionContext(LivingEntity attacker, LivingEntity defender,
                                     SXAttributeData attackerData, SXAttributeData defenderData,
                                     DamageData damageData) {
        this.attacker = attacker;
        this.defender = defender;
        this.attackerData = attackerData;
        this.defenderData = defenderData;
        this.damageData = damageData;
        this.variables = buildVariables();
    }

    public Player formulaPlayer() {
        return attacker instanceof Player ? (Player) attacker : defender instanceof Player ? (Player) defender : null;
    }

    private Map<String, Double> buildVariables() {
        Map<String, Double> values = new LinkedHashMap<>();
        for (EntityType type : EntityType.values()) {
            values.put("attacker_type_" + safe(type.name()), 0D);
            values.put("defender_type_" + safe(type.name()), 0D);
        }
        if (github.saukiya.sxattribute.SXAttribute.getAttributeEngine() != null) {
            for (AttributeDefinition definition : github.saukiya.sxattribute.SXAttribute.getAttributeEngine()
                    .registry().definitions().values()) {
                for (String field : definition.getValues().keySet()) {
                    values.put("attacker_" + safe(definition.getId()) + "_" + safe(field), 0D);
                    values.put("defender_" + safe(definition.getId()) + "_" + safe(field), 0D);
                }
            }
        }
        appendData(values, "attacker", attackerData);
        appendData(values, "defender", defenderData);
        if (attacker != null) {
            values.put("attacker_health", attacker.getHealth());
            values.put("attacker_max_health", attacker.getMaxHealth());
            values.put("attacker_type_" + safe(attacker.getType().name()), 1D);
            appendTags(values, "attacker", attacker);
            values.put("attacker_sneaking", attacker instanceof Player && ((Player) attacker).isSneaking() ? 1D : 0D);
        }
        if (defender != null) {
            values.put("defender_health", defender.getHealth());
            values.put("defender_max_health", defender.getMaxHealth());
            values.put("defender_type_" + safe(defender.getType().name()), 1D);
            appendTags(values, "defender", defender);
            values.put("defender_sneaking", defender instanceof Player && ((Player) defender).isSneaking() ? 1D : 0D);
            values.put("world_time", (double) defender.getWorld().getTime());
            values.put("world_storm", defender.getWorld().hasStorm() ? 1D : 0D);
        }
        if (attacker != null && defender != null && attacker.getWorld().equals(defender.getWorld())) {
            values.put("distance", attacker.getLocation().distance(defender.getLocation()));
        } else {
            values.put("distance", 0D);
        }
        if (damageData != null) {
            values.put("event_damage", damageData.getDamage());
            values.put("event_cancelled", damageData.isCancelled() ? 1D : 0D);
            values.put("event_crit", damageData.isCrit() ? 1D : 0D);
            values.put("event_pvp", attacker instanceof Player && defender instanceof Player ? 1D : 0D);
        }
        return values;
    }

    private void appendData(Map<String, Double> target, String prefix, SXAttributeData data) {
        if (data == null) return;
        data.getDynamicValues().forEach((attributeId, fields) -> fields.forEach((field, value) ->
                target.put(prefix + "_" + safe(attributeId) + "_" + safe(field), value)));
    }

    private void appendTags(Map<String, Double> target, String prefix, LivingEntity entity) {
        if (github.saukiya.sxattribute.SXAttribute.getSourceService() == null) return;
        target.putAll(github.saukiya.sxattribute.SXAttribute.getSourceService()
                .formulaVariables(entity.getUniqueId(), prefix));
        for (String tag : github.saukiya.sxattribute.SXAttribute.getSourceService().tags(entity.getUniqueId())) {
            target.put(prefix + "_tag_" + safe(tag), 1D);
        }
    }

    public Map<String, Double> variablesFor(AttributeDefinition definition, boolean attackerSide) {
        Map<String, Double> result = new LinkedHashMap<>(variables);
        SXAttributeData self = attackerSide ? attackerData : defenderData;
        for (String field : definition.getValues().keySet()) result.put("self_" + safe(field), 0D);
        if (self != null) {
            Map<String, Double> fields = self.getDynamicValues().get(definition.getId());
            if (fields != null) fields.forEach((field, value) -> result.put("self_" + safe(field), value));
        }
        return result;
    }

    private String safe(String value) {
        return value.replaceAll("[^A-Za-z0-9_]", "_");
    }
}
