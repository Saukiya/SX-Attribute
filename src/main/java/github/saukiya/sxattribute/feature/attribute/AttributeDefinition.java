package github.saukiya.sxattribute.feature.attribute;

import github.saukiya.sxattribute.data.attribute.SubAttribute;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import github.saukiya.sxattribute.util.FormulaUtil;

/**
 * 单个配置化属性的不可变定义。
 * <p>
 * 定义只描述字段、触发器和动作，不直接持有实体状态；注册表热替换时旧定义可以安全地
 * 被正在执行的事件继续读取，不会观察到半更新配置。
 */
@Getter
public final class AttributeDefinition {

    private final String id;
    private final int priority;
    private final Map<String, ValueDefinition> values;
    private final List<MappingDefinition> mappings;
    private final List<TriggerDefinition> triggers;

    private AttributeDefinition(String id, int priority, Map<String, ValueDefinition> values,
                                List<MappingDefinition> mappings, List<TriggerDefinition> triggers) {
        this.id = id;
        this.priority = priority;
        this.values = Collections.unmodifiableMap(values);
        this.mappings = Collections.unmodifiableList(mappings);
        this.triggers = Collections.unmodifiableList(triggers);
    }

    public static AttributeDefinition parse(String id, ConfigurationSection section) {
        Map<String, ValueDefinition> values = new LinkedHashMap<>();
        ConfigurationSection valueSection = section.getConfigurationSection("Values");
        if (valueSection != null) {
            for (String field : valueSection.getKeys(false)) {
                ConfigurationSection node = valueSection.getConfigurationSection(field);
                if (node != null) values.put(field, ValueDefinition.parse(field, node));
            }
        } else {
            collectLegacyValues(section, values, "value");
        }
        List<MappingDefinition> mappings = new ArrayList<>();
        for (Map<?, ?> map : section.getMapList("Mappings")) {
            mappings.add(MappingDefinition.parse(id, values, map));
        }
        List<TriggerDefinition> triggers = new ArrayList<>();
        for (Map<?, ?> map : section.getMapList("Triggers")) {
            triggers.add(TriggerDefinition.parse(map));
        }
        return new AttributeDefinition(id, section.getInt("Priority", 1000), values, mappings, triggers);
    }

    /**
     * 将旧 Attributes 配置中的 DiscernName 节点镜像为动态字段。
     * 旧专用类仍负责行为，镜像只保证统一注册表和公式引用能够覆盖全部既有属性。
     */
    private static void collectLegacyValues(ConfigurationSection section, Map<String, ValueDefinition> values, String fallbackField) {
        if (section.isString("DiscernName")) {
            String field = uniqueField(values, section.getName() == null ? fallbackField : section.getName());
            values.put(field, ValueDefinition.legacy(field, section));
        }
        for (String key : section.getKeys(false)) {
            ConfigurationSection child = section.getConfigurationSection(key);
            if (child != null && !"Values".equalsIgnoreCase(key) && !"Triggers".equalsIgnoreCase(key)) {
                collectLegacyValues(child, values, key);
            }
        }
    }

    private static String uniqueField(Map<String, ValueDefinition> values, String requested) {
        String normalized = requested.replaceAll("[^A-Za-z0-9_]", "_");
        String candidate = normalized;
        int suffix = 2;
        while (values.containsKey(candidate)) candidate = normalized + "_" + suffix++;
        return candidate;
    }

    /**
     * 一个属性内部的命名数值字段，例如 damage、chance、resistance。
     */
    @Getter
    public static final class ValueDefinition {
        private final String field;
        private final String match;
        private final MatchMode matchMode;
        private final Aggregate aggregate;
        private final double min;
        private final double max;
        private final double combatPower;
        private final String combatPowerFormula;
        private final Pattern pattern;

        private ValueDefinition(String field, String match, MatchMode matchMode, Aggregate aggregate,
                                double min, double max, double combatPower, String combatPowerFormula) {
            this.field = field;
            this.match = match;
            this.matchMode = matchMode;
            this.aggregate = aggregate;
            this.min = min;
            this.max = max;
            this.combatPower = combatPower;
            this.combatPowerFormula = combatPowerFormula;
            this.pattern = matchMode == MatchMode.REGEX && match != null ? Pattern.compile(match) : null;
        }

        private static ValueDefinition parse(String field, ConfigurationSection section) {
            return new ValueDefinition(field, section.getString("Match", field),
                    enumValue(MatchMode.class, section.getString("MatchMode"), MatchMode.CONTAINS),
                    enumValue(Aggregate.class, section.getString("Aggregate"), Aggregate.SUM),
                    section.getDouble("Min", -Double.MAX_VALUE), section.getDouble("Max", Double.MAX_VALUE),
                    section.getDouble("CombatPower", 0D), section.getString("CombatPowerFormula"));
        }

        private static ValueDefinition legacy(String field, ConfigurationSection section) {
            return new ValueDefinition(field, section.getString("DiscernName", field), MatchMode.CONTAINS, Aggregate.SUM,
                    section.getDouble("LowerLimit", -Double.MAX_VALUE), section.getDouble("UpperLimit", Double.MAX_VALUE),
                    section.getDouble("CombatPower", 0D), null);
        }

        public boolean matches(String lore) {
            String plain = org.bukkit.ChatColor.stripColor(lore == null ? "" : lore.replace('&', '§'));
            if (match == null || plain == null) return false;
            switch (matchMode) {
                case PREFIX:
                    return plain.startsWith(match);
                case EQUALS:
                    return plain.equals(match);
                case REGEX:
                    return pattern.matcher(plain).find();
                case CONTAINS:
                default:
                    return plain.contains(match);
            }
        }

        public double aggregate(double current, double parsed) {
            switch (aggregate) {
                case MAX:
                    return Math.max(current, parsed);
                case MIN:
                    return Math.min(current, parsed);
                case LAST:
                    return parsed;
                case SUM:
                default:
                    return current + parsed;
            }
        }

        public double correct(double value) {
            return Math.min(max, Math.max(min, value));
        }

        public double combatContribution(double value) {
            if (combatPowerFormula == null || combatPowerFormula.isEmpty()) return value * combatPower;
            Map<String, Double> variables = new LinkedHashMap<>();
            variables.put("value", value);
            return FormulaUtil.eval(null, combatPowerFormula, variables, value * combatPower);
        }
    }

    /**
     * 将当前自定义属性的一个字段换算为另一条可识别属性文本。
     * <p>
     * Target 使用 Lore 识别名而不是 Java 类名，使同一协议既能映射内置属性，也能映射用户定义的属性。
     * 映射始终以未派生的字段快照为输入，避免映射顺序或循环引用导致结果不稳定。
     */
    @Getter
    public static final class MappingDefinition {
        private final String source;
        private final String target;
        private final String scale;
        private final double scaleFallback;
        private final String formula;

        private MappingDefinition(String source, String target, String scale, double scaleFallback, String formula) {
            this.source = source;
            this.target = target;
            this.scale = scale;
            this.scaleFallback = scaleFallback;
            this.formula = formula;
        }

        private static MappingDefinition parse(String attributeId, Map<String, ValueDefinition> values, Map<?, ?> map) {
            String source = text(map.get("Source"));
            String target = text(map.get("Target"));
            if (source == null || !values.containsKey(source)) {
                throw new IllegalArgumentException("Attribute " + attributeId + " mapping references unknown Source: " + source);
            }
            if (target == null) {
                throw new IllegalArgumentException("Attribute " + attributeId + " mapping requires Target");
            }
            Object rawScale = map.containsKey("Scale") ? map.get("Scale") : 1D;
            return new MappingDefinition(source, target, String.valueOf(rawScale),
                    scaleFallback(rawScale), text(map.get("Formula")));
        }

        /** 只有纯数值可脱离公式引擎安全回退，表达式无法在缺少引擎时自行解释。 */
        private static double scaleFallback(Object value) {
            if (value instanceof Number) return ((Number) value).doubleValue();
            try {
                return Double.parseDouble(String.valueOf(value).trim());
            } catch (NumberFormatException ignored) {
                return 1D;
            }
        }

        private static String text(Object value) {
            if (value == null) return null;
            String text = String.valueOf(value).trim();
            return text.isEmpty() ? null : text;
        }
    }

    /**
     * 触发器定义。动作保持原始配置 Map，由白名单执行器解释。
     */
    @Getter
    public static final class TriggerDefinition {
        private final AttributeTrigger event;
        private final int priority;
        private final String when;
        private final List<Map<String, Object>> actions;

        private TriggerDefinition(AttributeTrigger event, int priority, String when, List<Map<String, Object>> actions) {
            this.event = event;
            this.priority = priority;
            this.when = when;
            this.actions = Collections.unmodifiableList(actions);
        }

        @SuppressWarnings("unchecked")
        private static TriggerDefinition parse(Map<?, ?> map) {
            AttributeTrigger event = enumValue(AttributeTrigger.class, String.valueOf(map.get("Event")), AttributeTrigger.API);
            int priority = number(map.get("Priority"), 1000).intValue();
            String when = map.containsKey("When") ? String.valueOf(map.get("When")) : null;
            List<Map<String, Object>> actions = new ArrayList<>();
            Object rawActions = map.get("Actions");
            if (rawActions instanceof List) {
                for (Object rawAction : (List<?>) rawActions) {
                    if (rawAction instanceof Map) actions.add(new LinkedHashMap<>((Map<String, Object>) rawAction));
                }
            }
            return new TriggerDefinition(event, priority, when, actions);
        }
    }

    public enum MatchMode {CONTAINS, PREFIX, EQUALS, REGEX}

    public enum Aggregate {SUM, MAX, MIN, LAST}

    public enum AttributeTrigger {
        LOAD, EQUIP, UNEQUIP, DAMAGE_ATTACK, DAMAGE_DEFEND, DAMAGE_AFTER, PROJECTILE_SHOOT,
        HEAL, EXP_GAIN, KILL, DEATH, TICK, API
    }

    private static Number number(Object value, Number fallback) {
        return value instanceof Number ? (Number) value : fallback;
    }

    private static <T extends Enum<T>> T enumValue(Class<T> type, String value, T fallback) {
        if (value == null) return fallback;
        try {
            return Enum.valueOf(type, value.toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return fallback;
        }
    }
}
