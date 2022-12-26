package github.saukiya.sxattribute.data.condition.sub;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.condition.EquipmentType;
import github.saukiya.sxattribute.data.condition.SubCondition;
import github.saukiya.sxattribute.util.Config;
import github.saukiya.sxattribute.util.Message;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

/**
 * 限制副手
 *
 * @author Saukiya
 */
public class OffHand extends SubCondition {

    public OffHand() {
        super(SXAttribute.getInst(), EquipmentType.OFF_HAND);
    }

    @Override
    public boolean determine(LivingEntity entity, ItemStack item, String lore) {
        if (lore.contains(Config.getConfig().getString(Config.NAME_HAND_MAIN))) {
            if (item != null)
                Message.send(entity, Message.PLAYER__NO_USE_SLOT, getItemName(item), Config.getConfig().getString(Config.NAME_HAND_MAIN));
            return false;
        }
        return true;
    }
}
