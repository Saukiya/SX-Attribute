package github.saukiya.sxattribute.hook.mythic;

import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMechanicLoadEvent;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.EventPriority;
import github.saukiya.sxattribute.event.SXDamageEvent;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;

import java.util.Map;

/** MM 4 的技能必须继承 SkillMechanic 并返回 boolean，不能复用 MM 5 的接口签名。 */
public final class Mythic4Skills implements Listener {
    /** MM 4 在 HIGHEST 用最近技能伤害覆盖事件；只同步当前 SX 调用栈的施法者，避免串入嵌套技能。 */
    private static final ThreadLocal<ActiveMob> DAMAGE_CASTER = new ThreadLocal<>();

    /** SX 在 HIGH 完成计算后发布此事件，必须在 MM 的 HIGHEST 覆盖之前更新其原生数值槽。 */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamageCalculated(SXDamageEvent event) {
        ActiveMob caster = DAMAGE_CASTER.get();
        if (caster != null && event.getData().isSkillDamage()
                && caster.getEntity().getBukkitEntity().equals(event.getData().getAttacker())) {
            caster.setLastDamageSkillAmount(event.getData().getEvent().getDamage());
        }
    }

    /** 每次 MM 加载技能都创建独立参数对象，避免重载后保留旧公式。 */
    @EventHandler
    public void onMechanic(MythicMechanicLoadEvent event) {
        AttributeSkill.Kind kind = AttributeSkill.resolve(event.getMechanicName());
        if (kind != null) event.register(new Mechanic(kind, event.getConfig()));
    }

    /** 旧版本专属继承树只会在检测到 MM 4 后加载。 */
    private static final class Mechanic extends SkillMechanic implements ITargetedEntitySkill {
        private final AttributeSkill skill;
        private final Mythic4Placeholders placeholders = new Mythic4Placeholders();

        private Mechanic(AttributeSkill.Kind kind, MythicLineConfig config) {
            super(config.getLine(), config);
            Map<String, String> parameters = SkillParameters.parse(config.getLine());
            // 单键重载跨早期/后期 MM 4 保持一致，避免绑定曾变化的数组/varargs 方法描述符。
            skill = new AttributeSkill(kind, (keys, fallback) -> {
                for (String key : keys) {
                    String value = parameters.containsKey(key) ? parameters.get(key) : config.getString(key, null);
                    if (value != null) return value;
                }
                return fallback;
            });
            // forceSync 是 MM 4 的同步调度协议，不能以 MM 5 的 ThreadSafetyLevel 替代。
            this.forceSync = true;
        }

        /** 通过兼容解析器支持 MM 4 新旧占位符实现，不强制链接后期才加入的 PlaceholderString。 */
        @Override
        public boolean castAtEntity(SkillMetadata metadata, AbstractEntity target) {
            if (metadata.getCaster() == null || target == null) return false;
            return skill.cast(metadata.getCaster().getEntity().getBukkitEntity(), target.getBukkitEntity(),
                    text -> placeholders.render(text, metadata, target), action -> {
                        if (!skill.isDamageSkill()) return action.getAsBoolean();
                        // 区域接力可能延后执行；标记必须包围实际伤害调用，不能包围调度提交。
                        boolean previous = metadata.getCaster().isUsingDamageSkill();
                        ActiveMob caster = metadata.getCaster() instanceof ActiveMob ? (ActiveMob) metadata.getCaster() : null;
                        ActiveMob outerCaster = DAMAGE_CASTER.get();
                        double previousAmount = caster == null ? 0D : caster.getLastDamageSkillAmount();
                        if (caster == null) DAMAGE_CASTER.remove();
                        else DAMAGE_CASTER.set(caster);
                        metadata.getCaster().setUsingDamageSkill(true);
                        try {
                            return action.getAsBoolean();
                        } finally {
                            // 内层施法结束后还原外层数值，异常/取消也不能污染下一次 MM 技能。
                            if (caster != null) caster.setLastDamageSkillAmount(previousAmount);
                            if (outerCaster == null) DAMAGE_CASTER.remove();
                            else DAMAGE_CASTER.set(outerCaster);
                            metadata.getCaster().setUsingDamageSkill(previous);
                        }
                    });
        }
    }
}
