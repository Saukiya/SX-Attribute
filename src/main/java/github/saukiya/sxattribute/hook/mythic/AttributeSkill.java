package github.saukiya.sxattribute.hook.mythic;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.SXAttributeData;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.feature.attribute.AttributeExecutionContext;
import github.saukiya.sxattribute.feature.source.SourceApplyRequest;
import github.saukiya.sxattribute.feature.source.SourceWriteResult;
import github.saukiya.sxattribute.util.FoliaScheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

/**
 * MM 4/5 共用的属性技能语义。此类不引用任何 Mythic 或新版本 Bukkit 类型，
 * 旧服和新服使用相同的 Lore、动态属性、叠层与伤害规则，仅外层适配器不同。
 */
public final class AttributeSkill {
    /** 技能名带 SX 命名空间，避免抢占 AttributePlus 扩展的 DamageAP/AttrAdd。 */
    public enum Kind {DAMAGE, BASE_DAMAGE, PERCENT_DAMAGE, ADD, TAKE, COUNT, INHERIT, TIME, UPDATE, API, HEAL, RULE}

    private final Kind kind;
    private final Map<String, String> options = new LinkedHashMap<>();
    /** 配置错误每个技能实例只报告一次，避免重复施法刷屏。 */
    private final AtomicBoolean warned = new AtomicBoolean();

    /** 在加载阶段保存原始公式；变量必须在每次施法、每个目标处重新求值。 */
    public AttributeSkill(Kind kind, BiFunction<String[], String, String> config) {
        this.kind = kind;
        option(config, "attributes", "", "attributelist", "al", "attributes");
        option(config, "amount", kind == Kind.PERCENT_DAMAGE ? "0.1" : "0", "amount", "a");
        option(config, "basic", kind == Kind.BASE_DAMAGE ? "true" : "false", "basic", "b");
        option(config, "multiplier", "1", "basemultiplier", "bam", "multiplier", "m");
        option(config, "damageMultiplier", "1", "damagemultiplier", "dm");
        option(config, "ignoreImmunity", "false", "ignoreimmunity", "pi");
        option(config, "source", "", "source", "s");
        option(config, "duration", "0", "duration", "d");
        option(config, "maxStacks", "1", "maxstacks", "ms");
        option(config, "stackMode", "REPLACE", "stackmode", "sm");
        option(config, "persistent", "false", "persistent", "p");
        option(config, "tags", "", "tags");
        option(config, "exclude", "", "exclude", "blacklist");
        option(config, "type", "mythic", "type");
        option(config, "args", "", "args");
        option(config, "rule", "", "rule", "r");
        option(config, "allowSelf", "false", "allowself");
        option(config, "percent", "false", "percent");
    }

    /** 支持易读的 DamageSX 与统一前缀 sxdamage 两种拼写，大小写不敏感。 */
    public static Kind resolve(String name) {
        switch (name.toLowerCase(Locale.ROOT)) {
            case "sxdamage": case "damagesx": return Kind.DAMAGE;
            case "sxbasedamage": case "basedamagesx": return Kind.BASE_DAMAGE;
            case "sxpercentdamage": case "percentdamagesx": return Kind.PERCENT_DAMAGE;
            case "sxattradd": return Kind.ADD;
            case "sxattrtake": return Kind.TAKE;
            case "sxattrcount": return Kind.COUNT;
            case "sxattrinherit": return Kind.INHERIT;
            case "sxattrsourcetime": return Kind.TIME;
            case "sxattrupdate": return Kind.UPDATE;
            case "sxattrtrigger": return Kind.API;
            case "sxheal": return Kind.HEAL;
            case "sxsourcerule": return Kind.RULE;
            default: return null;
        }
    }

    /** 伤害技能需要 MM 的原生标记，避免被其普攻逻辑重新改写伤害。 */
    public boolean isDamageSkill() {
        return kind == Kind.DAMAGE || kind == Kind.BASE_DAMAGE || kind == Kind.PERCENT_DAMAGE;
    }

    /** 无 MM 状态包装的 API 入口；已排队的跨区操作返回接受状态，不等待区域线程。 */
    public boolean cast(Entity actor, Entity recipient, UnaryOperator<String> placeholders) {
        return cast(actor, recipient, placeholders, BooleanSupplier::getAsBoolean);
    }

    /**
     * 同区域即时执行；否则先进入施法者区域，跨区非战斗操作通过数值快照接力。
     * scope 只在同时拥有两端时执行，保证 MM 的伤害标记与实际 damage 调用处于同一个作用域。
     */
    public boolean cast(Entity actor, Entity recipient, UnaryOperator<String> placeholders,
                        Function<BooleanSupplier, Boolean> scope) {
        if (!(actor instanceof LivingEntity) || !(recipient instanceof LivingEntity)) return false;
        LivingEntity caster = (LivingEntity) actor;
        LivingEntity target = (LivingEntity) recipient;
        if (FoliaScheduler.owns(caster) && FoliaScheduler.owns(target)) {
            try {
                return alive(caster) && alive(target) && scope.apply(() -> execute(caster, target, placeholders));
            } catch (RuntimeException exception) {
                warn(exception.toString());
                return false;
            }
        }
        SkillRegionTask task = SkillRegionTask.create(this::warn);
        if (task == null) {
            warn("too many pending region skills (limit 1024)");
            return false;
        }
        task.at(caster, () -> {
            if (FoliaScheduler.owns(target)) {
                task.finish(alive(target) && scope.apply(() -> execute(caster, target, placeholders)));
            } else if (isDamageSkill() || kind == Kind.API) {
                // 任意第三方攻击/脚本可同步访问两端；不能伪造实体、匿名扣血或阻塞区域线程来冒充完整结算。
                task.reject("cross-region damage/API requires both entities in one region; use target-only skills for remote effects");
            } else {
                remote(caster, target, placeholders, task);
            }
        });
        return task.accepted();
    }

    /** 跨区仅传递属性与变量副本；PAPI 始终在公式玩家的区域求值，最终写入在目标区域执行。 */
    private void remote(LivingEntity caster, LivingEntity target, UnaryOperator<String> placeholders, SkillRegionTask task) {
        SkillEntitySnapshot attacker = new SkillEntitySnapshot(caster, true, kind == Kind.INHERIT);
        Map<String, String> first = renderSide(options, placeholders, true);
        task.at(target, () -> {
            SkillEntitySnapshot defender = new SkillEntitySnapshot(target, false, false);
            Map<String, String> rendered = renderSide(first, placeholders, false);
            // 未知/第三方/trigger 占位符可能访问任意实体，不能宣称在远程目标线程调用它们是安全的。
            rendered.values().forEach(value -> SkillExpressions.mythic(value, token -> {
                throw new IllegalArgumentException("Cross-region placeholder needs SX snapshot variable: " + token);
            }));
            LivingEntity formulaOwner = caster instanceof Player ? caster : target;
            task.at(formulaOwner, () -> {
                Player player = formulaOwner instanceof Player ? (Player) formulaOwner : null;
                SkillExpressions expressions = SkillExpressions.create(player, attacker.withTarget(defender));
                BooleanSupplier operation = prepareTarget(caster, target, rendered, expressions, attacker.sources);
                task.at(target, () -> task.finish(operation.getAsBoolean()));
            });
        });
    }

    /** 只接受实体自身的基础字段，不能把 caster.target 等可追踪第三实体的 MM 标记带入跨区路径。 */
    private Map<String, String> renderSide(Map<String, String> input, UnaryOperator<String> placeholders, boolean caster) {
        Map<String, String> result = new LinkedHashMap<>();
        input.forEach((key, value) -> result.put(key, SkillExpressions.mythic(value, token -> {
            String owner = caster ? "(?:caster|mob)" : "target";
            boolean matches = token.matches("<" + owner + "\\.(?:name|uuid|level|health|hp|maxhealth|mhp|x|y|z)>");
            return matches ? placeholders.apply(token) : token;
        })));
        return result;
    }

    private boolean alive(LivingEntity entity) {
        return entity.isValid() && !entity.isDead();
    }

    /** 同一机制的多区域回调可能并发失败，只输出一次原因以免范围技能刷屏。 */
    private void warn(String reason) {
        if (warned.compareAndSet(false, true)) {
            SXAttribute.getInst().getLogger().warning("Mythic SX " + kind + " failed: " + reason);
        }
    }

    private boolean execute(LivingEntity caster, LivingEntity target, UnaryOperator<String> placeholders) {
        Map<String, String> rendered = new LinkedHashMap<>();
        options.forEach((key, value) -> rendered.put(key, SkillExpressions.mythic(value, placeholders)));
        SXAttributeData casterData = SXAttribute.getApi().getEntityData(caster);
        SXAttributeData targetData = SXAttribute.getApi().getEntityData(target);
        AttributeExecutionContext context = new AttributeExecutionContext(caster, target, casterData, targetData, null);
        Map<String, Double> variables = context.getVariables();
        appendLegacy(variables, "attacker", casterData);
        appendLegacy(variables, "defender", targetData);
        SkillExpressions expressions = SkillExpressions.create(context.formulaPlayer(), variables);
        switch (kind) {
            case DAMAGE: case BASE_DAMAGE: case PERCENT_DAMAGE:
                if (target instanceof ArmorStand || !caster.getWorld().equals(target.getWorld())
                        || caster.equals(target) && !bool(rendered, "allowSelf")) return false;
                SXAttributeData extra = SXAttribute.getApi().loadListData(expressions.lore(rendered.get("attributes")));
                if (SXAttribute.getAttributeEngine() != null) SXAttribute.getAttributeEngine().applyMappings(extra, caster);
                SXAttributeData attack = bool(rendered, "basic")
                        ? SkillAttributes.copy(casterData, nonnegative(rendered, "multiplier", expressions)) : new SXAttributeData();
                attack.add(extra);
                // 只修正实际提供的内置属性，避免空快照因 Damage.correct 自动获得 1 点基础攻击。
                for (SubAttribute attribute : SubAttribute.getAttributes()) {
                    if (attack.isValid(attribute)) attribute.correct(attack.getValues(attribute));
                }
                if (SXAttribute.getAttributeEngine() != null) SXAttribute.getAttributeEngine().correct(attack);
                exclude(attack, rendered.get("exclude"));
                double amount = nonnegative(rendered, "amount", expressions);
                if (kind == Kind.PERCENT_DAMAGE) amount = SkillAttributes.finite(amount * target.getMaxHealth());
                double multiplier = nonnegative(rendered, "damageMultiplier", expressions);
                if (multiplier == 0D || amount == 0D && !attack.isValid()) return false;
                return new SkillDamageContext(caster, target, attack, multiplier, rendered.get("type"),
                        SkillAttributes.split(rendered.get("args"))).damage(amount, bool(rendered, "ignoreImmunity"));
            case API:
                SXAttribute.getApi().triggerDynamicAttributes(caster, target);
                return true;
            default:
                SXAttributeData sources = kind == Kind.INHERIT
                        ? SkillAttributes.copy(SXAttribute.getAttributeManager().sumSources(caster.getUniqueId()), 1D) : null;
                return prepareTarget(caster, target, rendered, expressions, sources).getAsBoolean();
        }
    }

    /**
     * 准备阶段只计算数值/来源请求；返回操作只访问目标。非战斗技能复用此路径，
     * 确保跨区与同区的层数、持久化、取消事件和伤害之外的语义完全一致。
     */
    private BooleanSupplier prepareTarget(LivingEntity caster, LivingEntity target, Map<String, String> rendered,
                                          SkillExpressions expressions, SXAttributeData sources) {
        switch (kind) {
            case ADD:
                SourceApplyRequest request = request(rendered, expressions.lore(rendered.get("attributes")), expressions);
                return () -> applied(SXAttribute.getApi().applyManagedSource(target, request));
            case TAKE:
                String removedSource = source(rendered);
                return () -> SXAttribute.getApi().removeManagedSource(target, removedSource) == SourceWriteResult.REMOVED;
            case INHERIT:
                // 原始来源在施法者区域采集；目标聚合时才应用映射，防止派生属性被计算两次。
                if (caster == target) throw new IllegalArgumentException("Cannot inherit own attributes");
                SXAttributeData snapshot = SkillAttributes.copy(sources, nonnegative(rendered, "multiplier", expressions));
                snapshot.add(SXAttribute.getApi().loadListData(expressions.lore(rendered.get("attributes"))));
                exclude(snapshot, rendered.get("exclude"));
                SourceApplyRequest inherited = request(rendered, Collections.emptyList(), expressions);
                return () -> applied(SXAttribute.getSourceService().applySnapshot(target, inherited, snapshot));
            case COUNT:
                double scale = nonnegative(rendered, "multiplier", expressions);
                SXAttributeData addition = SXAttribute.getApi().loadListData(expressions.lore(rendered.get("attributes")));
                String countedSource = source(rendered);
                // 在目标区域读取当时的来源并计算，保留时间/层数，不覆盖接力期间发生的来源更新。
                return () -> applied(SXAttribute.getSourceService().transformSnapshot(target, countedSource, current -> {
                    SXAttributeData changed = SkillAttributes.copy(current, scale).add(addition);
                    exclude(changed, rendered.get("exclude"));
                    return changed;
                }));
            case TIME:
                String timedSource = source(rendered);
                long duration = ticks(rendered, expressions);
                return () -> applied(SXAttribute.getSourceService().changeDuration(target, timedSource, duration));
            case UPDATE:
                return () -> {
                    SXAttribute.getApi().updateData(target);
                    SXAttribute.getApi().attributeUpdate(target);
                    return true;
                };
            case HEAL:
                double healing = nonnegative(rendered, "amount", expressions);
                boolean percent = bool(rendered, "percent");
                return () -> {
                    // 百分比与生命上限取执行时的目标状态；回血事件和 HEAL 触发器均在目标区域发布。
                    double amount = percent ? SkillAttributes.finite(healing * target.getMaxHealth()) : healing;
                    EntityRegainHealthEvent event = new EntityRegainHealthEvent(target, amount, EntityRegainHealthEvent.RegainReason.CUSTOM);
                    Bukkit.getPluginManager().callEvent(event);
                    if (event.isCancelled() || !Double.isFinite(event.getAmount()) || event.getAmount() < 0D || target.isDead()) return false;
                    target.setHealth(Math.min(target.getMaxHealth(), target.getHealth() + event.getAmount()));
                    return true;
                };
            case RULE:
                String rule = rendered.get("rule");
                return () -> applied(SXAttribute.getApi().applyManagedSourceRule(target, rule));
            default: throw new IllegalArgumentException("Skill requires both entity owners: " + kind);
        }
    }

    /** 来源名称是长期标识，统一加命名空间，防止移除技能时覆盖装备/其它插件的来源。 */
    private String source(Map<String, String> values) {
        String name = values.get("source").trim();
        if (name.isEmpty()) throw new IllegalArgumentException("source is required");
        return "mythic:" + name;
    }

    private SourceApplyRequest request(Map<String, String> values, List<String> lore, SkillExpressions expressions) {
        double stacks = nonnegative(values, "maxStacks", expressions);
        if (stacks < 1D || stacks > Integer.MAX_VALUE || stacks != Math.rint(stacks)) {
            throw new IllegalArgumentException("maxStacks must be a positive integer");
        }
        return new SourceApplyRequest(source(values), lore, ticks(values, expressions), (int) stacks,
                SourceApplyRequest.StackMode.valueOf(values.get("stackMode").toUpperCase(Locale.ROOT)),
                bool(values, "persistent"), SkillAttributes.split(values.get("tags")));
    }

    private long ticks(Map<String, String> values, SkillExpressions expressions) {
        double duration = nonnegative(values, "duration", expressions);
        if (duration > (Long.MAX_VALUE - System.currentTimeMillis()) / 50D || duration != Math.rint(duration)) {
            throw new IllegalArgumentException("duration must be whole ticks within timestamp range");
        }
        return (long) duration;
    }

    private boolean applied(SourceWriteResult result) {
        if (result != SourceWriteResult.APPLIED) throw new IllegalArgumentException("Source write: " + result);
        return true;
    }

    /** 数值字段统一使用当前施法的 SX 上下文，边界约束在展开之后检查。 */
    private double nonnegative(Map<String, String> values, String key, SkillExpressions expressions) {
        double value = expressions.number(values.get(key));
        if (value < 0D) throw new IllegalArgumentException(key + " must be nonnegative");
        return value;
    }

    private boolean bool(Map<String, String> values, String key) {
        String value = values.get(key);
        if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
            throw new IllegalArgumentException(key + " must be true or false");
        }
        return Boolean.parseBoolean(value);
    }

    private void exclude(SXAttributeData data, String names) {
        for (String name : SkillAttributes.split(names)) {
            SubAttribute attribute = SubAttribute.getSubAttribute(name);
            if (attribute != null) Arrays.fill(data.getValues(attribute), 0D);
            data.getDynamicValues().remove(name);
        }
    }

    private void appendLegacy(Map<String, Double> variables, String prefix, SXAttributeData data) {
        for (SubAttribute attribute : SubAttribute.getAttributes()) {
            double[] values = data.getValues(attribute);
            for (int index = 0; index < values.length; index++) variables.put(prefix + "_" + attribute.getName() + "_" + index, values[index]);
        }
    }

    private void option(BiFunction<String[], String, String> config, String key, String fallback, String... names) {
        String value = config.apply(names, fallback).trim();
        // MM 将机制参数中的保留字符编码为占位标记；先还原算式符号，否则 10<&da>2 会被当成 102。
        value = value.replace("<&da>", "-").replace("<&csp>", " ");
        // 早期 MM 保留参数上的引号；统一只去掉配对的最外层引号，不能破坏 Lore 内的文本。
        if (value.length() >= 2 && (value.startsWith("\"") && value.endsWith("\"")
                || value.startsWith("'") && value.endsWith("'"))) value = value.substring(1, value.length() - 1);
        options.put(key, value);
    }

}
