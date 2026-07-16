package github.saukiya.sxattribute.feature.equipment.affix;

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

/**
 * 随机词缀池模块。NBT 仅存词缀 ID 与掷值，名称和属性模板始终从配置重建。
 */
public final class AffixFeature extends AbstractEquipmentFeature {

    public AffixFeature() {
        super("Affix");
    }

    @Override
    public List<String> render(Player player, ItemStack item, YamlConfiguration state) {
        List<String> result = new ArrayList<>();
        for (String encoded : state.getStringList("Entries")) {
            Entry entry = Entry.parse(encoded);
            ConfigurationSection affix = config().getConfigurationSection("Affixes." + entry.id);
            if (affix == null) continue;
            String value = format(entry.value);
            for (String line : affix.getStringList("Lore")) {
                result.add(color(line.replace("{value}", value).replace("{id}", entry.id)));
            }
        }
        return result;
    }

    @Override
    public FeatureResult apply(Player player, ItemStack item, YamlConfiguration state) {
        List<String> allowed = config().getStringList("AllowedMaterials");
        if (!allowed.isEmpty() && allowed.stream().noneMatch(name -> name.equalsIgnoreCase(item.getType().name()))) {
            return FeatureResult.unsupported(message("MaterialDenied", "&c该装备类型不允许生成此词缀池"));
        }
        rollIdentities(player, state, Collections.emptyList());
        return FeatureResult.success(message("Success", "&a词缀已生成"));
    }

    public void rollIdentities(Player player, YamlConfiguration state, List<Integer> lockedSlots) {
        List<String> old = new ArrayList<>(state.getStringList("Entries"));
        int count = Math.max(1, (int) formula(player, "Formula.Count", config().getInt("Count", 2), "current", old.size()));
        List<String> entries = new ArrayList<>();
        for (int slot = 0; slot < count; slot++) {
            if (lockedSlots.contains(slot) && slot < old.size()) {
                entries.add(old.get(slot));
                continue;
            }
            String id = WeightedSelector.choose(config().getConfigurationSection("Affixes"));
            if (id == null) continue;
            entries.add(new Entry(id, randomValue(id)).encode());
        }
        state.set("Entries", entries);
    }

    public void rerollValues(YamlConfiguration state) {
        List<String> entries = new ArrayList<>();
        for (String encoded : state.getStringList("Entries")) {
            Entry old = Entry.parse(encoded);
            entries.add(new Entry(old.id, randomValue(old.id)).encode());
        }
        state.set("Entries", entries);
    }

    private double randomValue(String id) {
        ConfigurationSection affix = config().getConfigurationSection("Affixes." + id);
        if (affix == null) return 0D;
        double min = affix.getDouble("Min", 0D);
        double max = affix.getDouble("Max", min);
        return min + Math.random() * Math.max(0D, max - min);
    }

    private String format(double value) {
        return github.saukiya.sxattribute.SXAttribute.getDf().format(value);
    }

    private static final class Entry {
        private final String id;
        private final double value;

        private Entry(String id, double value) {
            this.id = id;
            this.value = value;
        }

        private String encode() {
            return id + "|" + value;
        }

        private static Entry parse(String encoded) {
            String[] split = encoded.split("[|]", 2);
            try {
                return new Entry(split[0], split.length > 1 ? Double.parseDouble(split[1]) : 0D);
            } catch (NumberFormatException ignored) {
                return new Entry(split[0], 0D);
            }
        }
    }
}
