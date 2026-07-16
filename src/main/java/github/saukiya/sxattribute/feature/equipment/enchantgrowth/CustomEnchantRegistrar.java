package github.saukiya.sxattribute.feature.equipment.enchantgrowth;

import github.saukiya.sxattribute.SXAttribute;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 分版本自定义附魔注册适配器。
 * <p>
 * Bukkit 1.20.5 前后的附魔抽象类二进制协议不兼容，不能在通用插件主包中安全构造实例。
 * Beta 阶段仅接管服务端已真实注册的同命名空间附魔；缺少版本适配器时返回 false，
 * 调用方必须拒绝应用，禁止退化成 Lore 伪装或破坏已经冻结的注册表。
 */
public final class CustomEnchantRegistrar {

    private final Map<String, Enchantment> registered = new LinkedHashMap<>();

    public boolean register(String id, ConfigurationSection config) {
        NamespacedKey key = new NamespacedKey(SXAttribute.getInst(), id.toLowerCase());
        Enchantment existing = Enchantment.getByKey(key);
        if (existing != null) {
            registered.put(id, existing);
            return true;
        }
        SXAttribute.getInst().getLogger().severe("Real custom enchant adapter is unavailable for " + id
                + "; the enchant is disabled instead of creating a fake Lore enchant.");
        return false;
    }

    public Enchantment get(String id) {
        return registered.get(id);
    }
}
