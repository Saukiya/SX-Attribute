package github.saukiya.sxattribute.hook.mythic;

import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.skills.SkillString;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** MM 4.1 没有 PlaceholderString；按能力探测使用早期 SkillString 或后期完整 metadata 解析。 */
public final class Mythic4Placeholders {
    private final Method factory;
    private final Method getter;
    /** MM 会登记 PlaceholderParser，所以固定配置的解析器必须复用，不能每次命中重复创建。 */
    private final Map<String, Object> parsers = new ConcurrentHashMap<>();

    /** 只在加载 MM 4 技能时探测，ClassNotFoundException 表示使用早期兼容路径。 */
    public Mythic4Placeholders() {
        Method create = null;
        Method get = null;
        try {
            ClassLoader loader = SkillMetadata.class.getClassLoader();
            Class<?> parser = Class.forName("io.lumine.xikage.mythicmobs.skills.placeholders.parsers.PlaceholderString", false, loader);
            Class<?> metadata = Class.forName("io.lumine.xikage.mythicmobs.skills.placeholders.PlaceholderMeta", false, loader);
            create = parser.getMethod("of", String.class);
            get = parser.getMethod("get", metadata, AbstractEntity.class);
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {
            create = null;
        }
        factory = create;
        getter = get;
    }

    /** 新式 caster 前缀在早期 MM 转为 mob；不支持的变量保留原文，由公式校验报告失败。 */
    public String render(String text, SkillMetadata metadata, AbstractEntity target) {
        if (!text.contains("<") && !text.contains("%")) return text;
        if (factory == null) {
            return SkillString.parseMobVariables(text.replace("<caster.", "<mob."), metadata.getCaster(), target, metadata.getTrigger());
        }
        try {
            Object parser = parsers.computeIfAbsent(text, key -> {
                try {
                    return factory.invoke(null, key);
                } catch (ReflectiveOperationException exception) {
                    throw new IllegalArgumentException("Cannot parse MM 4 placeholder: " + key, exception);
                }
            });
            return (String) getter.invoke(parser, metadata, target);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalArgumentException("Cannot render MM 4 placeholder: " + text, exception);
        }
    }
}
