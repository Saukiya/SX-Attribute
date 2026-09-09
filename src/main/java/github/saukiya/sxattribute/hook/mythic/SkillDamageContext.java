package github.saukiya.sxattribute.hook.mythic;

import github.saukiya.sxattribute.data.attribute.SXAttributeData;
import lombok.Getter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 将一次 Bukkit 原生伤害与技能快照绑定，保留保护插件、击杀归属及原版减伤事件。
 * 上下文只活在 damage() 调用栈中，不写实体 metadata，避免反伤和多目标技能串用属性。
 */
@Getter
public final class SkillDamageContext {
    private static final ThreadLocal<SkillDamageContext> CURRENT = new ThreadLocal<>();
    /** 限制 onDamaged 再次施放伤害技能形成的同步递归，正常连续/延迟施法不受影响。 */
    private static final int MAX_DEPTH = 8;
    private final LivingEntity caster;
    private final LivingEntity target;
    private final SXAttributeData attributes;
    private final double multiplier;
    private final String type;
    private final List<String> arguments;
    private final int depth;
    private EntityDamageByEntityEvent event;

    /** 参数由共享技能执行器校验；构造时隔离调用者传入的可变集合。 */
    public SkillDamageContext(LivingEntity caster, LivingEntity target, SXAttributeData attributes,
                              double multiplier, String type, List<String> arguments) {
        this.caster = caster;
        this.target = target;
        this.attributes = SkillAttributes.copy(attributes, 1D);
        this.multiplier = SkillAttributes.finite(multiplier);
        this.type = type;
        this.arguments = Collections.unmodifiableList(new ArrayList<>(arguments));
        SkillDamageContext parent = CURRENT.get();
        this.depth = parent == null ? 1 : parent.depth + 1;
    }

    /** LOWEST 阶段只认领第一条匹配的事件，后续同一实体对的递归事件不能偷用本次快照。 */
    public static void capture(EntityDamageByEntityEvent event) {
        SkillDamageContext current = CURRENT.get();
        if (current != null && current.event == null && event.getDamager().equals(current.caster)
                && event.getEntity().equals(current.target)) current.event = event;
    }

    /** HIGH 阶段按事件对象身份查询，不能仅凭施法者 UUID 判定。 */
    public static SkillDamageContext find(EntityDamageByEntityEvent event) {
        SkillDamageContext current = CURRENT.get();
        return current != null && current.event == event ? current : null;
    }

    /**
     * 必须在拥有施法者和目标的线程执行；异常、取消和免疫都恢复上下文及原受伤间隔。
     * 返回 true 表示 Bukkit 产生了未取消的伤害事件，不承诺目标实际掉血（例如吸收生命）。
     */
    public boolean damage(double amount, boolean ignoreImmunity) {
        if (depth > MAX_DEPTH) return false;
        SkillDamageContext parent = CURRENT.get();
        int previousTicks = target.getNoDamageTicks();
        CURRENT.set(this);
        try {
            if (ignoreImmunity) target.setNoDamageTicks(0);
            target.damage(amount, caster);
            return event != null && !event.isCancelled();
        } finally {
            if (parent == null) CURRENT.remove();
            else CURRENT.set(parent);
            if (ignoreImmunity && target.isValid()) target.setNoDamageTicks(previousTicks);
        }
    }
}
