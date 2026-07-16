package github.saukiya.sxattribute.feature.equipment.setbonus;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.PreLoadItem;
import github.saukiya.sxattribute.event.SXLoadAttributeEvent;
import github.saukiya.sxattribute.feature.equipment.core.AbstractEquipmentFeature;
import github.saukiya.sxattribute.feature.equipment.core.AggregateEquipmentFeature;
import github.saukiya.sxattribute.feature.equipment.core.FeatureItemState;
import github.saukiya.sxattribute.feature.equipment.core.FeatureResult;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 两件套、四件套、任意阈值和混搭套装奖励模块。 */
public final class SetBonusFeature extends AbstractEquipmentFeature implements AggregateEquipmentFeature {

    public SetBonusFeature() {
        super("SetBonus");
    }

    @Override
    public List<String> render(Player player, ItemStack item, YamlConfiguration state) {
        String setId = state.getString("SetId");
        if (setId == null) return Collections.emptyList();
        return Collections.singletonList(color(config().getString("Lore.Format", "&7套装: {set}").replace("{set}", setId)));
    }

    @Override
    public FeatureResult apply(Player player, ItemStack item, YamlConfiguration state) {
        String defaultSet = config().getString("DefaultSet");
        if (defaultSet == null || !config().isConfigurationSection("Sets." + defaultSet)) {
            return FeatureResult.unsupported(message("NoDefaultSet", "&c未配置默认套装"));
        }
        state.set("SetId", defaultSet);
        return FeatureResult.success(message("Success", "&a装备已归入套装 {set}").replace("{set}", defaultSet));
    }

    @Override
    public void contribute(SXLoadAttributeEvent event) {
        if (!enabled()) return;
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (PreLoadItem item : event.getItemList()) {
            String setId = FeatureItemState.read(item.getItem(), id()).getString("SetId");
            if (setId != null) counts.merge(setId, 1, Integer::sum);
        }
        for (Map.Entry<String, Integer> count : counts.entrySet()) {
            ConfigurationSection thresholds = config().getConfigurationSection("Sets." + count.getKey() + ".Thresholds");
            if (thresholds == null) continue;
            for (String threshold : thresholds.getKeys(false)) {
                try {
                    if (count.getValue() >= Integer.parseInt(threshold)) {
                        event.getAttributeData().add(SXAttribute.getAttributeManager().loadListData(thresholds.getStringList(threshold + ".Attributes")));
                    }
                } catch (NumberFormatException ignored) {
                    SXAttribute.getInst().getLogger().warning("Invalid set threshold: " + threshold);
                }
            }
        }
        ConfigurationSection mixes = config().getConfigurationSection("Mixes");
        if (mixes == null) return;
        for (String mixId : mixes.getKeys(false)) {
            ConfigurationSection requirements = mixes.getConfigurationSection(mixId + ".Requirements");
            if (requirements == null) continue;
            boolean matches = requirements.getKeys(false).stream()
                    .allMatch(set -> counts.getOrDefault(set, 0) >= requirements.getInt(set));
            if (matches) {
                event.getAttributeData().add(SXAttribute.getAttributeManager().loadListData(mixes.getStringList(mixId + ".Attributes")));
            }
        }
    }
}
