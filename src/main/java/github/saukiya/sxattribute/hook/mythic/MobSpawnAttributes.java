package github.saukiya.sxattribute.hook.mythic;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.AttributeSource;
import github.saukiya.sxattribute.data.attribute.SXAttributeData;
import github.saukiya.sxattribute.data.attribute.SXAttributeManager;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.feature.attribute.AttributeExecutionContext;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/** MM 出生配置桥接；不依赖任何 MM 类型，两代监听器在实体区域使用相同的 SX 属性协议。 */
public final class MobSpawnAttributes {
    /** 独立于技能的 mythic: 命名空间和内部装备来源，避免普通 Buff 或装备重读覆盖出生属性。 */
    public static final String SOURCE = "mythic-mob:spawn";
    /** 同一怪物配置错误只报告一次，避免刷怪器持续产生重复日志。 */
    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();

    private MobSpawnAttributes() { }

    /** 官方配置对象已经处理文件位置/模板；优先使用规范节点，空列表可显式关闭旧别名。 */
    public static List<String> read(Predicate<String> contains, Function<String, List<String>> strings) {
        for (String node : new String[]{"SXAttribute", "SX-Attribute", "sxattribute"}) {
            if (contains.test(node)) return Collections.unmodifiableList(new ArrayList<>(strings.apply(node)));
        }
        return Collections.emptyList();
    }

    /** MM 4 早期等级返回 int、后期/5 返回 double；读取数值能力而不绑定某个 JVM 返回描述符。 */
    public static double level(Object event) {
        try {
            Object value = event.getClass().getMethod("getMobLevel").invoke(event);
            if (!(value instanceof Number)) throw new IllegalArgumentException("Mob level is not numeric");
            return SkillAttributes.finite(((Number) value).doubleValue());
        } catch (ReflectiveOperationException exception) {
            throw new IllegalArgumentException("Cannot read Mythic spawn level", exception);
        }
    }

    /**
     * 已取消的出生由适配器过滤。装备和属性均在实体所属区域执行，不把 Bukkit 事件交给异步任务。
     * 在正确区域时立即注册数值，其他入口通过可退役的实体调度器接管。
     */
    public static void spawn(LivingEntity entity, String type, double level, List<String> configured,
                             Consumer<Map<String, String>> equipment) {
        List<String> lines = new ArrayList<>(configured);
        SkillRegionTask task = SkillRegionTask.create(reason -> warn(type, reason));
        if (task == null) {
            warn(type, "too many pending region tasks");
            return;
        }
        task.at(entity, () -> {
            Map<String, String> mob = variables(entity, type, level);
            equipment.accept(new LinkedHashMap<>(mob));
            SXAttributeManager manager = SXAttribute.getAttributeManager();
            // 重复出生通知不重抽随机词条；出生来源的生命周期由实体属性管理器统一清理。
            if (lines.isEmpty() || manager.hasSource(entity.getUniqueId(), SOURCE)) {
                task.finish(true);
                return;
            }
            // 出生表达式能引用刚装备的 SX-Equipment；只重建内部装备源，不修改其他来源。
            manager.loadEntityData(entity);
            SXAttributeData initial = manager.getEntityData(entity);
            AttributeExecutionContext context = new AttributeExecutionContext(entity, entity, initial, initial, null);
            Map<String, Double> values = new LinkedHashMap<>(context.getVariables());
            for (SubAttribute attribute : SubAttribute.getAttributes()) {
                double[] fields = initial.getValues(attribute);
                for (int index = 0; index < fields.length; index++) {
                    values.put("attacker_" + attribute.getName() + "_" + index, fields[index]);
                    values.put("defender_" + attribute.getName() + "_" + index, fields[index]);
                }
            }
            SkillExpressions expressions = SkillExpressions.create(entity instanceof Player ? (Player) entity : null, values, mob);
            List<String> lore = render(lines, expressions, mob);
            // 所有行先求值并校验，再一次写入；映射仍留给来源聚合阶段，不能提前重复派生属性。
            SXAttributeData data = SkillAttributes.copy(manager.loadListData(lore), 1D);
            manager.putSource(entity.getUniqueId(), new AttributeSource(SOURCE, data, true));
            manager.attributeUpdateEvent(entity);
            task.finish(true);
        });
    }

    /** 一只怪物的所有行复用同一个 Handler；列表外层不重切分，单行仍支持技能的等号与 Lore 写法。 */
    static List<String> render(List<String> lines, SkillExpressions expressions, Map<String, String> variables) {
        List<String> result = new ArrayList<>();
        for (String line : lines) {
            if (line == null) throw new IllegalArgumentException("Null SXAttribute line");
            result.addAll(expressions.lore(SkillExpressions.mythic(line, token -> spawnPlaceholder(token, variables))));
        }
        return result;
    }

    /** 出生没有技能目标/trigger metadata，只提供明确的自身标记，未知标记必须报告配置错误。 */
    private static String spawnPlaceholder(String token, Map<String, String> variables) {
        String property = token.replaceFirst("^<(?:caster|mob)\\.", "");
        if (property.equals(token)) throw new IllegalArgumentException("Unsupported spawn placeholder: " + token);
        property = property.substring(0, property.length() - 1);
        String key;
        switch (property) {
            case "level": key = "mob_level"; break;
            case "name": key = "mob_name_display"; break;
            case "uuid": key = "mob_uuid"; break;
            case "health": case "hp": key = "mob_health"; break;
            case "maxhealth": case "mhp": key = "mob_max_health"; break;
            default: throw new IllegalArgumentException("Unsupported spawn placeholder: " + token);
        }
        return variables.get(key);
    }

    /** 这些键是出生配置的稳定变量协议；名称与 UUID 保持文本，等级/生命可参与数学式。 */
    private static Map<String, String> variables(LivingEntity entity, String type, double level) {
        Map<String, String> variables = new LinkedHashMap<>();
        variables.put("mob_level", String.valueOf(SkillAttributes.finite(level)));
        variables.put("mob_name_internal", type);
        variables.put("mob_name_display", entity.getCustomName() == null ? type : entity.getCustomName());
        variables.put("mob_uuid", entity.getUniqueId().toString());
        variables.put("mob_health", String.valueOf(entity.getHealth()));
        variables.put("mob_max_health", String.valueOf(entity.getMaxHealth()));
        return variables;
    }

    /** 装载/执行错误与具体怪物类型关联，便于直接定位 MM 配置。 */
    public static void warn(String type, String reason) {
        if (WARNED.add(type)) SXAttribute.getInst().getLogger().warning("Mythic SXAttribute mob '" + type + "': " + reason);
    }
}
