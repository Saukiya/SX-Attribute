package github.saukiya.sxattribute.hook.mythic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.ThreadSafetyLevel;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** MM 5 使用稳定的 ISkillMechanic 扩展接口，避免依赖在 5.13 移除的 SkillMechanic 旧构造器。 */
public final class Mythic5Skills implements Listener {
    /** 冷启动与 /mm reload 都通过官方加载事件注册，不触发全服 Mythic 重载。 */
    @EventHandler
    public void onMechanic(MythicMechanicLoadEvent event) {
        AttributeSkill.Kind kind = AttributeSkill.resolve(event.getMechanicName());
        if (kind == null) return;
        // 与 MM 4 使用相同原始参数协议，SX 表达式内部的分号不能被 MM 配置解析器截断。
        Map<String, String> parameters = SkillParameters.parse(event.getConfig().getLine());
        event.register(new Mechanic(new AttributeSkill(kind, (keys, fallback) -> {
            for (String key : keys) {
                if (parameters.containsKey(key)) return parameters.get(key);
            }
            return event.getConfig().getString(keys, fallback);
        })));
    }

    /** MM 5 特有类型限制在独立类中，未安装或安装 MM 4 时不会被 JVM 链接。 */
    private static final class Mechanic implements ITargetedEntitySkill {
        private final AttributeSkill skill;
        /** 解析器按配置文本缓存；每次仅更新 metadata/目标，避免 MM 内部解析器注册表持续增长。 */
        private final Map<String, PlaceholderString> placeholders = new ConcurrentHashMap<>();

        private Mechanic(AttributeSkill skill) {
            this.skill = skill;
        }

        /** Bukkit 实体读写与伤害事件只能在同步/拥有实体的区域线程执行。 */
        @Override
        public ThreadSafetyLevel getThreadSafetyLevel() {
            return ThreadSafetyLevel.SYNC_ONLY;
        }

        /** 变量按当前 metadata 与目标解析；恢复原有伤害标记，允许嵌套技能正确返回外层状态。 */
        @Override
        public SkillResult castAtEntity(SkillMetadata metadata, AbstractEntity target) {
            if (metadata.getCaster() == null || target == null) return SkillResult.INVALID_TARGET;
            return skill.cast(metadata.getCaster().getEntity().getBukkitEntity(), target.getBukkitEntity(),
                    text -> !text.contains("<") && !text.contains("%") ? text
                            : placeholders.computeIfAbsent(text, PlaceholderString::of).get(metadata, target), action -> {
                        if (!skill.isDamageSkill()) return action.getAsBoolean();
                        // 只有执行线程同时拥有两端时才修改 MM 施法者状态，finally 也在同一区域执行。
                        boolean previous = metadata.getCaster().isUsingDamageSkill();
                        metadata.getCaster().setUsingDamageSkill(true);
                        try {
                            return action.getAsBoolean();
                        } finally {
                            metadata.getCaster().setUsingDamageSkill(previous);
                        }
                    }) ? SkillResult.SUCCESS : SkillResult.ERROR;
        }
    }
}
