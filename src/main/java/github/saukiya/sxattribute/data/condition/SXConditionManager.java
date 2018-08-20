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
        new MainHandCondition().registerCondition();
        new OffHandCondition().registerCondition();
        new HandCondition(plugin).registerCondition();
        new LimitLevelCondition().registerCondition();
        new RoleCondition().registerCondition();
        new ExpiryTimeCondition().registerCondition();
        new AttackSpeedCondition().registerCondition();
    }

}
