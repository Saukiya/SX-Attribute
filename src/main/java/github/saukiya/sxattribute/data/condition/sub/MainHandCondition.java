package github.saukiya.sxattribute.data.condition.sub;

import github.saukiya.sxattribute.data.condition.SXConditionReturnType;
import github.saukiya.sxattribute.data.condition.SXConditionType;
import github.saukiya.sxattribute.data.condition.SubCondition;
import github.saukiya.sxattribute.util.Config;
import github.saukiya.sxattribute.util.Message;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

/**
 * 限制主手
 *
 * @author Saukiya
 */
public class MainHandCondition extends SubCondition {

    public MainHandCondition() {
        super("MainHand", SXConditionType.MAIN_HAND);
    }

    @Override
    public SXConditionReturnType determine(LivingEntity entity, ItemStack item, String lore) {
        if (lore.contains(Config.getConfig().getString(Config.NAME_HAND_OFF))) {
            if (item != null)
                Message.send(entity, Message.PLAYER__NO_USE_SLOT, getItemName(item), Config.getConfig().getString(Config.NAME_HAND_OFF));
            return SXConditionReturnType.ITEM;
        }
        return SXConditionReturnType.NULL;
    }
}
