package github.saukiya.sxattribute.hook.mythic;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.SXAttributeData;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.feature.attribute.AttributeExecutionContext;
import github.saukiya.sxattribute.util.FoliaScheduler;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import java.util.LinkedHashMap;
import java.util.Map;

/** 在实体所属区域采集数值；跨区只传递私有副本，不读取另一端的实时 Bukkit 状态。 */
final class SkillEntitySnapshot {
    final SXAttributeData sources;
    private final Location location;
    private final Map<String, Double> variables;

    /** 继承才需要原始来源，其他技能避免额外聚合装备来源。 */
    SkillEntitySnapshot(LivingEntity entity, boolean attacker, boolean inherit) {
        if (!FoliaScheduler.owns(entity)) throw new IllegalStateException("Snapshot requires entity ownership");
        SXAttributeData data = SkillAttributes.copy(SXAttribute.getApi().getEntityData(entity), 1D);
        String prefix = attacker ? "attacker" : "defender";
        AttributeExecutionContext context = new AttributeExecutionContext(attacker ? entity : null,
                attacker ? null : entity, attacker ? data : null, attacker ? null : data, null);
        variables = new LinkedHashMap<>();
        context.getVariables().forEach((key, value) -> {
            // 不能把另一端尚未采集的默认零值覆盖到已采集的快照上。
            if (key.startsWith(prefix + "_") || !attacker && key.startsWith("world_")) variables.put(key, value);
        });
        for (SubAttribute attribute : SubAttribute.getAttributes()) {
            double[] values = data.getValues(attribute);
            for (int index = 0; index < values.length; index++) {
                variables.put(prefix + "_" + attribute.getName() + "_" + index, values[index]);
            }
        }
        location = entity.getLocation().clone();
        sources = inherit ? SkillAttributes.copy(SXAttribute.getAttributeManager().sumSources(entity.getUniqueId()), 1D) : null;
    }

    /** 距离由两端采样位置计算，不调用实时实体；跨世界与原上下文一致返回零。 */
    Map<String, Double> withTarget(SkillEntitySnapshot target) {
        Map<String, Double> result = new LinkedHashMap<>(variables);
        result.putAll(target.variables);
        result.put("distance", location.getWorld().equals(target.location.getWorld()) ? location.distance(target.location) : 0D);
        return result;
    }
}
