package github.saukiya.sxattribute.data.condition.sub;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.condition.EquipmentType;
import github.saukiya.sxattribute.data.condition.SubCondition;
import github.saukiya.sxattribute.util.Config;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

/**
 * 手持免疫
 *
 * @author Saukiya
 */
public class Hand extends SubCondition {

    public Hand() {
        super(SXAttribute.getInst(), EquipmentType.MAIN_HAND, EquipmentType.OFF_HAND);
    }

    @Override
    public boolean determine(LivingEntity entity, ItemStack item, String lore) {
        return Config.getConfig().getStringList(Config.NAME_ARMOR).stream().noneMatch(lore::contains) && SXAttribute.getSlotDataManager().getSlotList().stream().noneMatch(slot -> lore.contains(slot.getName()));
    }
}
