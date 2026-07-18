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
            raw = SXAttribute.getNbtUtil().getNBT(item, statePath(featureId));
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
            SXAttribute.getNbtUtil().setNBT(item, statePath(featureId), state.saveToString());
        } catch (RuntimeException | LinkageError exception) {
            reportNbtFailure(featureId, exception);
        }
    }

    /**
     * 在 SX-Item 自动更新生成的新物品上迁移模块状态。
     * <p>
     * 这里只复制作为真实数据源的 State，不复制可重建的 Rendered；SX-Item 随后从物品模板中的
     * {@code <l:...>} 锁变量生成 Lore，从而保证更新材质或基础 Lore 时装备成长状态不会丢失，
     * 也不会带入过期显示文本。
     *
     * @param source SX-Item 更新前的原物品
     * @param target SX-Item 根据最新配置生成的新物品
     * @param featureId 装备模块 ID
     */
    public static void synchronize(ItemStack source, ItemStack target, String featureId) {
        if (!isUsable(source) || !isUsable(target)) return;
        try {
            String raw = SXAttribute.getNbtUtil().getNBT(source, statePath(featureId));
            if (raw != null) {
                SXAttribute.getNbtUtil().setNBT(target, statePath(featureId), raw);
            }
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

    /**
     * 保存模块计算出的原始属性文本。该节点不带 {@code §X}，是属性解析的数据源；
     * Rendered 或 SX-Item Lock 中的副本只负责显示，必须带显示专用标记。
     */
    public static void attributes(ItemStack item, String featureId, java.util.List<String> lines) {
        if (!isUsable(item)) return;
        try {
            SXAttribute.getNbtUtil().setNBTList(item, PREFIX + featureId + ".Attributes", lines);
        } catch (RuntimeException | LinkageError exception) {
            reportNbtFailure(featureId, exception);
        }
    }

    /** 模块状态的持久化路径必须集中生成，避免读取、写入和 SX-Item 更新迁移使用不同键。 */
    private static String statePath(String featureId) {
        return PREFIX + featureId + ".State";
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
