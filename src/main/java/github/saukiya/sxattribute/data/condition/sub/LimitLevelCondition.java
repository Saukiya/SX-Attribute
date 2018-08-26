package github.saukiya.sxattribute.data.condition.sub;

import github.saukiya.sxattribute.data.condition.SXConditionReturnType;
import github.saukiya.sxattribute.data.condition.SubCondition;
import github.saukiya.sxattribute.util.Config;
import github.saukiya.sxattribute.util.Message;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

/**
 * 限制等级
 *
 * @author Saukiya
 */
public class LimitLevelCondition extends SubCondition {

    public LimitLevelCondition() {
        super("LimitLevel");
    }

    @Override
    public SXConditionReturnType determine(LivingEntity entity, ItemStack item, String lore) {
        if (lore.contains(Config.getConfig().getString(Config.NAME_LIMIT_LEVEL)) && Integer.valueOf(getNumber(lore)) > getLevel(entity)) {
            if (item != null) Message.send(entity, Message.PLAYER__NO_LEVEL_USE, getItemName(item));
            return SXConditionReturnType.ITEM;
        }
        return SXConditionReturnType.NULL;
    }
}
