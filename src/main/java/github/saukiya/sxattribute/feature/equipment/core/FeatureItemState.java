package github.saukiya.sxattribute.feature.equipment.core;

import github.saukiya.sxattribute.SXAttribute;
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

    private FeatureItemState() {
    }

    public static YamlConfiguration read(ItemStack item, String featureId) {
        YamlConfiguration state = new YamlConfiguration();
        String raw = SXAttribute.getNbtUtil().getNBT(item, PREFIX + featureId + ".State");
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
        SXAttribute.getNbtUtil().setNBT(item, PREFIX + featureId + ".State", state.saveToString());
    }

    public static java.util.List<String> rendered(ItemStack item, String featureId) {
        return SXAttribute.getNbtUtil().getNBTList(item, PREFIX + featureId + ".Rendered");
    }

    public static void rendered(ItemStack item, String featureId, java.util.List<String> lines) {
        SXAttribute.getNbtUtil().setNBTList(item, PREFIX + featureId + ".Rendered", lines);
    }
}
