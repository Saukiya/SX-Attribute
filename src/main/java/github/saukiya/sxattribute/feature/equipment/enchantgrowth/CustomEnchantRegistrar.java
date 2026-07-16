package github.saukiya.sxattribute.feature.equipment.enchantgrowth;

import github.saukiya.sxattribute.SXAttribute;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 分版本自定义附魔注册适配器。
 * <p>
 * Bukkit 1.20.5 前后的附魔抽象类二进制协议不兼容，不能在通用插件主包中安全构造实例。
 * Beta 阶段仅接管服务端已真实注册的同命名空间附魔；缺少版本适配器时返回 false，
 * 调用方可按配置退化为 NBT + Lore 模式，但不能修改已经冻结的服务端注册表。
 */
public final class CustomEnchantRegistrar {

    private final Map<String, Enchantment> registered = new LinkedHashMap<>();
    /** 记录已降级的 ID，避免重载时重复输出同一条适配提示。 */
    private final Set<String> fallbackIds = new HashSet<>();

    /**
     * 尝试接管服务端已经真实注册的附魔。
     *
     * @return true 表示可写入 Bukkit 附魔；false 表示调用方应使用配置允许的 Lore 降级模式
     */
    public boolean register(String id, ConfigurationSection config) {
        try {
            NamespacedKey key = new NamespacedKey(SXAttribute.getInst(), id.toLowerCase());
            Enchantment existing = findRegistered(key, id);
            if (existing != null) {
                registered.put(id, existing);
                fallbackIds.remove(id);
                return true;
            }
        } catch (RuntimeException | LinkageError exception) {
            fallback(id, exception.getClass().getSimpleName() + ": " + exception.getMessage());
            return false;
        }
        fallback(id, "no compatible registry adapter or pre-registered enchant was found");
        return false;
    }

    /**
     * 跨 Bukkit 版本查询真实附魔。
     * <p>
     * 1.12.2 没有 {@code Enchantment.getByKey}，因此不能在字节码中直接调用；新版本优先反射调用
     * NamespacedKey 查询，旧版本退回名称查询。此方法只接管已真实注册的附魔，Lore 降级由成长模块处理。
     */
    private Enchantment findRegistered(NamespacedKey key, String id) {
        try {
            Method getByKey = Enchantment.class.getMethod("getByKey", NamespacedKey.class);
            Object result = getByKey.invoke(null, key);
            if (result instanceof Enchantment) return (Enchantment) result;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            // 旧 Bukkit 不提供 NamespacedKey 查询，继续使用稳定存在的名称接口。
        }
        Enchantment byName = Enchantment.getByName(id);
        return byName != null ? byName : Enchantment.getByName(id.toUpperCase());
    }

    private void fallback(String id, String reason) {
        if (!fallbackIds.add(id)) return;
        SXAttribute.getInst().getLogger().warning("Custom enchant " + id
                + " will use NBT + Lore mode because no real registry entry is available: " + reason + ".");
    }

    /** @return 已接管的真实 Bukkit 附魔；返回 null 时由调用方决定是否使用 Lore 模式。 */
    public Enchantment get(String id) {
        return registered.get(id);
    }
}
