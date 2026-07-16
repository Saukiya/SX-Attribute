package github.saukiya.sxattribute.feature.equipment.reroll;

import github.saukiya.sxattribute.feature.equipment.affix.AffixFeature;
import github.saukiya.sxattribute.feature.equipment.core.AbstractEquipmentFeature;
import github.saukiya.sxattribute.feature.equipment.core.FeatureItemState;
import github.saukiya.sxattribute.feature.equipment.core.FeatureResult;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

/** 洗练模块：保留词缀身份，仅重新随机数值。 */
public final class RerollFeature extends AbstractEquipmentFeature {

    private final AffixFeature affix;

    public RerollFeature(AffixFeature affix) {
        super("Reroll");
        this.affix = affix;
    }

    @Override
    public List<String> render(Player player, ItemStack item, YamlConfiguration state) {
        return Collections.emptyList();
    }

    @Override
    public FeatureResult apply(Player player, ItemStack item, YamlConfiguration state) {
        YamlConfiguration affixState = FeatureItemState.read(item, affix.id());
        affix.rerollValues(affixState);
        FeatureItemState.write(item, affix.id(), affixState);
        return FeatureResult.success(message("Success", "&a词缀数值已洗练"));
    }
}
