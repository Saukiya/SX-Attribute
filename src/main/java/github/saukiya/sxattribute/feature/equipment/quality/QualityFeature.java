package github.saukiya.sxattribute.feature.equipment.quality;

import github.saukiya.sxattribute.feature.equipment.core.AbstractEquipmentFeature;
import github.saukiya.sxattribute.feature.equipment.core.FeatureResult;
import github.saukiya.sxattribute.feature.equipment.core.WeightedSelector;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** 品质抽取与属性渲染模块。 */
public final class QualityFeature extends AbstractEquipmentFeature {

    public QualityFeature() {
        super("Quality");
    }

    @Override
    public List<String> render(Player player, ItemStack item, YamlConfiguration state) {
        String id = state.getString("Id");
        ConfigurationSection quality = id == null ? null : config().getConfigurationSection("Qualities." + id);
        if (quality == null) return Collections.emptyList();
        List<String> lines = new ArrayList<>();
        lines.add(color(config().getString("Lore.Format", "&7品质: {name}").replace("{name}", quality.getString("Name", id))));
        lines.addAll(quality.getStringList("Attributes"));
        lines.replaceAll(this::color);
        return lines;
    }

    @Override
    public FeatureResult apply(Player player, ItemStack item, YamlConfiguration state) {
        String id = WeightedSelector.choose(config().getConfigurationSection("Qualities"));
        if (id == null) return FeatureResult.unsupported(message("NoOptions", "&c没有可抽取的品质"));
        state.set("Id", id);
        return FeatureResult.success(message("Success", "&a品质已变更为 {id}").replace("{id}", id));
    }
}
