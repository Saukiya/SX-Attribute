package github.saukiya.sxattribute.feature.equipment.core;

import github.saukiya.sxattribute.SXAttribute;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

/**
 * SX-Item NBT 状态编解码器。
 * <p>
 * 每个模块只占用自己的命名空间；Lore 永远可由该状态重建，不作为真实数据源。
 */
public final class FeatureItemState {

    private static final String PREFIX = "SX-Attribute.Feature.";
    private static boolean nbtFailureLogged;

    private FeatureItemState() {
    }

    public static YamlConfiguration read(ItemStack item, String featureId) {
        YamlConfiguration state = new YamlConfiguration();
        if (!isUsable(item)) return state;
        String raw;
        try {
            raw = SXAttribute.getNbtUtil().getNBT(item, PREFIX + featureId + ".State");
        } catch (RuntimeException | LinkageError exception) {
            reportNbtFailure(featureId, exception);
            return state;
        }
        if (raw != null && !raw.isEmpty()) {
            try {
                state.loadFromString(raw);
            } catch (InvalidConfigurationException exception) {
                SXAttribute.getInst().getLogger().warning("Invalid feature NBT ignored: " + featureId);
            }
        }
        return state;
    }

    public static void write(ItemStack item, String featureId, YamlConfiguration state) {
        if (!isUsable(item)) return;
        try {
            SXAttribute.getNbtUtil().setNBT(item, PREFIX + featureId + ".State", state.saveToString());
        } catch (RuntimeException | LinkageError exception) {
            reportNbtFailure(featureId, exception);
        }
    }

    public static java.util.List<String> rendered(ItemStack item, String featureId) {
        if (!isUsable(item)) return java.util.Collections.emptyList();
        try {
            return SXAttribute.getNbtUtil().getNBTList(item, PREFIX + featureId + ".Rendered");
        } catch (RuntimeException | LinkageError exception) {
            reportNbtFailure(featureId, exception);
            return java.util.Collections.emptyList();
        }
    }

    public static void rendered(ItemStack item, String featureId, java.util.List<String> lines) {
        if (!isUsable(item)) return;
        try {
            SXAttribute.getNbtUtil().setNBTList(item, PREFIX + featureId + ".Rendered", lines);
        } catch (RuntimeException | LinkageError exception) {
            reportNbtFailure(featureId, exception);
        }
    }

    /** 空槽位和 AIR 不是可持久化物品，禁止传给版本相关 NBT 包装器。 */
    public static boolean isUsable(ItemStack item) {
        return item != null && item.getType() != Material.AIR;
    }

    /** NBT 适配器不兼容时只记录一次并让装备模块无状态降级，避免每次攻击重复刷屏。 */
    private static void reportNbtFailure(String featureId, Throwable exception) {
        if (nbtFailureLogged) return;
        nbtFailureLogged = true;
        SXAttribute.getInst().getLogger().severe("Equipment feature NBT is unavailable; stateful modules were isolated. "
                + "First failure in " + featureId + ": " + exception.getClass().getSimpleName()
                + ": " + exception.getMessage());
    }
}
