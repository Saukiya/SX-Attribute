package github.saukiya.sxattribute.data.condition.sub;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.condition.SXConditionReturnType;
import github.saukiya.sxattribute.data.condition.SXConditionType;
import github.saukiya.sxattribute.data.condition.SubCondition;
import github.saukiya.sxattribute.util.Config;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

/**
 * 手持免疫
 *
 * @author Saukiya
 */
public class HandCondition extends SubCondition {

    public HandCondition() {
        super("Hand", SXConditionType.HAND);
    }

    @Override
    public SXConditionReturnType determine(LivingEntity entity, ItemStack item, String lore) {
        return (Config.getConfig().getStringList(Config.NAME_ARMOR).stream().anyMatch(lore::contains) || SXAttribute.getApi().getRegisterSlotMapEntrySet().stream().anyMatch(entry -> lore.contains(entry.getValue().getName()))) ? SXConditionReturnType.ITEM : SXConditionReturnType.NULL;
    }
}
