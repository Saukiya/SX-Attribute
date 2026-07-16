package github.saukiya.sxattribute.feature.equipment.core;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 强化与升星共享的等级状态机。
 * <p>
 * 成功率、属性值和失败策略均读取子模块独立配置，公共实现只维护原子等级迁移。
 */
public abstract class AbstractLevelFeature extends AbstractEquipmentFeature {

    protected AbstractLevelFeature(String id) {
        super(id);
    }

    @Override
    public List<String> render(Player player, ItemStack item, YamlConfiguration state) {
        int level = state.getInt("Level", 0);
        List<String> result = new ArrayList<>();
        if (level <= 0) return result;
        result.add(color(config().getString("Lore.Level", "&7等级: {level}").replace("{level}", String.valueOf(level))));
        ConfigurationSection attributes = config().getConfigurationSection("Attributes");
        if (attributes != null) {
            for (String id : attributes.getKeys(false)) {
                String name = attributes.getString(id + ".Name", id);
                double value = formula(player, "Attributes." + id + ".Formula", level,
                        "level", level, "max", config().getInt("MaxLevel", 10));
                result.add(color(config().getString("Lore.Attribute", "&7{name}: {value}")
                        .replace("{name}", name).replace("{value}", github.saukiya.sxattribute.SXAttribute.getDf().format(value))));
            }
        }
        return result;
    }

    @Override
    public FeatureResult apply(Player player, ItemStack item, YamlConfiguration state) {
        int level = state.getInt("Level", 0);
        int max = config().getInt("MaxLevel", 10);
        if (level >= max) return FeatureResult.unsupported(message("MaxLevel", "&c已达到最高等级"));
        double chance = formula(player, "Formula.SuccessChance", 100D, "level", level, "next", level + 1, "max", max);
        if (Math.random() * 100D < chance) {
            state.set("Level", level + 1);
            return FeatureResult.success(message("Success", "&a操作成功，当前等级 {level}")
                    .replace("{level}", String.valueOf(level + 1)));
        }
        double protection = formula(player, "Failure.ProtectionChance", 0D,
                "level", level, "next", level + 1, "max", max);
        if (Math.random() * 100D < protection) {
            return FeatureResult.failed(false, false, message("Protected", "&e操作失败，但装备受到保护"));
        }
        String policy = config().getString("Failure.Policy", "KEEP").toUpperCase();
        switch (policy) {
            case "DOWNGRADE":
                state.set("Level", Math.max(0, level - Math.max(1, config().getInt("Failure.Levels", 1))));
                return FeatureResult.failed(true, false, message("Downgrade", "&c失败并降低等级"));
            case "RESET":
                state.set("Level", 0);
                return FeatureResult.failed(true, false, message("Reset", "&c失败并重置等级"));
            case "DESTROY":
                return FeatureResult.failed(true, true, message("Destroy", "&4失败，装备已损毁"));
            case "KEEP":
            default:
                return FeatureResult.failed(false, false, message("Keep", "&c失败，等级保持不变"));
        }
    }
}
