package github.saukiya.sxattribute.data.condition.sub;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.condition.SXConditionReturnType;
import github.saukiya.sxattribute.data.condition.SXConditionType;
import github.saukiya.sxattribute.data.condition.SubCondition;
import github.saukiya.sxattribute.util.Config;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

/**
 * 攻击速度
 *
 * @author Saukiya
 */
public class AttackSpeedCondition extends SubCondition {

    public AttackSpeedCondition() {
        super("AttackSpeed", SXConditionType.MAIN_HAND);
    }

    @Override
    public SXConditionReturnType determine(LivingEntity entity, ItemStack item, String lore) {
        if (lore.contains(Config.getConfig().getString(Config.NAME_ATTACK_SPEED))) {
            if (item != null) SXAttribute.getApi().getItemUtil().setAttackSpeed(item, Double.valueOf(getNumber(lore)));
        }
        return SXConditionReturnType.NULL;
    }
}
