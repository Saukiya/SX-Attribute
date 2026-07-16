package github.saukiya.sxattribute.feature.equipment.reforge;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.feature.equipment.affix.AffixFeature;
import github.saukiya.sxattribute.feature.equipment.core.AbstractEquipmentFeature;
import github.saukiya.sxattribute.feature.equipment.core.FeatureItemState;
import github.saukiya.sxattribute.feature.equipment.core.FeatureResult;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

/** 重铸模块：更换词缀身份，并按配置保留锁定槽位。 */
public final class ReforgeFeature extends AbstractEquipmentFeature {

    private final AffixFeature affix;

    public ReforgeFeature(AffixFeature affix) {
        super("Reforge");
        this.affix = affix;
    }

    @Override
    public List<String> render(Player player, ItemStack item, YamlConfiguration state) {
        return Collections.emptyList();
    }

    @Override
    public FeatureResult apply(Player player, ItemStack item, YamlConfiguration state) {
        YamlConfiguration affixState = FeatureItemState.read(item, affix.id());
        affix.rollIdentities(player, affixState, state.getIntegerList("LockedSlots"));
        FeatureItemState.write(item, affix.id(), affixState);
        return FeatureResult.success(message("Success", "&a词缀身份已重铸"));
    }
}
