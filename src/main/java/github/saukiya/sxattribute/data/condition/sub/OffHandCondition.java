package github.saukiya.sxattribute.data.condition.sub;

import github.saukiya.sxattribute.data.condition.SXConditionReturnType;
import github.saukiya.sxattribute.data.condition.SXConditionType;
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
public class OffHandCondition extends SubCondition {

    public OffHandCondition() {
        super("OffHand", SXConditionType.OFF_HAND);
    }

    @Override
    public SXConditionReturnType determine(LivingEntity entity, ItemStack item, String lore) {
        if (lore.contains(Config.getConfig().getString(Config.NAME_HAND_MAIN))) {
            if (item != null)
                Message.send(entity, Message.PLAYER__NO_USE_SLOT, getItemName(item), Config.getConfig().getString(Config.NAME_HAND_MAIN));
            return SXConditionReturnType.ITEM;
        }
        return SXConditionReturnType.NULL;
    }
}
