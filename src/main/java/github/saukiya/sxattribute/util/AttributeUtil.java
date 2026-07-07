package github.saukiya.sxattribute.util;

import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * 跨版本 Attribute 解析工具
 * <p>
 * 高版本(1.21.3+/26.x)将 org.bukkit.attribute.Attribute 由枚举改为 Registry 接口,
 * 且常量重命名(GENERIC_MAX_HEALTH -> MAX_HEALTH)。旧代码若在编译期硬引用
 * Attribute.GENERIC_XXX 静态字段, 用旧 jar 跑新服会抛 NoSuchFieldError。
 * <p>
 * 本类不引用任何具体枚举常量, 改为运行期按名称解析(先新名后旧名), 兼容 1.8 ~ 26.x。
 *
 * @author Ray_Hughes
 */
public class AttributeUtil {

    /**
     * 已解析的 Attribute 缓存, key 为无前缀名称(如 "max_health")
     */
    private static final Map<String, Attribute> CACHE = new HashMap<>();

    /**
     * 按属性短名解析 Attribute (跨版本安全)
     *
     * @param newName 新版名称(枚举名, 如 MAX_HEALTH; 同时对应命名空间键 max_health)
     * @param oldName 旧版名称(枚举名, 如 GENERIC_MAX_HEALTH)
     * @return Attribute, 解析失败返回 null
     */
    public static Attribute get(String newName, String oldName) {
        String cacheKey = newName;
        if (CACHE.containsKey(cacheKey)) {
            return CACHE.get(cacheKey);
        }
        Attribute attribute = resolve(newName, oldName);
        CACHE.put(cacheKey, attribute);
        return attribute;
    }

    private static Attribute resolve(String newName, String oldName) {
        // 1. 高版本 Registry 方式: 按命名空间键 minecraft:max_health 解析, 不触碰枚举常量
        try {
            Attribute byKey = org.bukkit.Registry.ATTRIBUTE.get(NamespacedKey.minecraft(newName.toLowerCase()));
            if (byKey != null) return byKey;
        } catch (Throwable ignored) {
            // 1.13 以下无 Registry.ATTRIBUTE, 忽略进入枚举回退
        }
        // 2. 枚举时代(1.9 ~ 1.21.x): 用 valueOf 按名称查, 先旧名后新名
        Attribute byEnum = valueOf(oldName);
        if (byEnum != null) return byEnum;
        return valueOf(newName);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Attribute valueOf(String name) {
        try {
            // Attribute 在枚举时代是 Enum, 反射调用 valueOf 避免编译期常量引用
            return (Attribute) Enum.valueOf((Class<? extends Enum>) (Class<?>) Attribute.class, name);
        } catch (Throwable ignored) {
            return null;
        }
    }

    /**
     * 获取实体某属性的 AttributeInstance (跨版本安全)
     *
     * @param entity  实体
     * @param newName 新版名称
     * @param oldName 旧版名称
     * @return AttributeInstance, 无该属性或解析失败返回 null
     */
    public static AttributeInstance getInstance(LivingEntity entity, String newName, String oldName) {
        Attribute attribute = get(newName, oldName);
        if (attribute == null) return null;
        return entity.getAttribute(attribute);
    }
}
