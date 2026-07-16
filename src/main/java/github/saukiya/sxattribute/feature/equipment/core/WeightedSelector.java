package github.saukiya.sxattribute.feature.equipment.core;

import org.bukkit.configuration.ConfigurationSection;

/** 配置化权重选择器，权重无需归一化。 */
public final class WeightedSelector {
    private WeightedSelector() {
    }

    public static String choose(ConfigurationSection section) {
        if (section == null) return null;
        double total = 0D;
        for (String id : section.getKeys(false)) total += Math.max(0D, section.getDouble(id + ".Weight", 0D));
        if (total <= 0D) return null;
        double cursor = Math.random() * total;
        for (String id : section.getKeys(false)) {
            cursor -= Math.max(0D, section.getDouble(id + ".Weight", 0D));
            if (cursor <= 0D) return id;
        }
        return null;
    }
}
