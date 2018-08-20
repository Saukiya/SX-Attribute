package github.saukiya.sxattribute.data.condition;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.condition.sub.*;
import lombok.Getter;

/**
 * @author Saukiya
 */
public class SXConditionManager {

    @Getter
    private final ConditionMap conditionMap = SubCondition.conditionMap;

    public SXConditionManager(SXAttribute plugin) {
        new MainHandCondition().registerCondition(SXAttribute.getPlugin());
        new OffHandCondition().registerCondition(SXAttribute.getPlugin());
        new HandCondition(plugin).registerCondition(SXAttribute.getPlugin());
        new LimitLevelCondition().registerCondition(SXAttribute.getPlugin());
        new RoleCondition().registerCondition(SXAttribute.getPlugin());
        new ExpiryTimeCondition().registerCondition(SXAttribute.getPlugin());
        new AttackSpeedCondition().registerCondition(SXAttribute.getPlugin());
    }

}
