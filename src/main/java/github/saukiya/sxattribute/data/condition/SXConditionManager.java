package github.saukiya.sxattribute.data.condition;

import github.saukiya.sxattribute.data.condition.sub.*;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * 条件管理器
 *
 * @author Saukiya
 */
public class SXConditionManager {

    @Getter
    private final ConditionMap conditionMap = SubCondition.conditionMap;

    public SXConditionManager(JavaPlugin plugin) {
        new MainHandCondition().registerCondition(plugin);
        new OffHandCondition().registerCondition(plugin);
        new HandCondition().registerCondition(plugin);
        new LimitLevelCondition().registerCondition(plugin);
        new RoleCondition().registerCondition(plugin);
        new ExpiryTimeCondition().registerCondition(plugin);
        new AttackSpeedCondition().registerCondition(plugin);
        new DurabilityCondition().registerCondition(plugin);
    }

    public void onConditionEnable() {
        conditionMap.values().forEach(SubCondition::onEnable);
    }

    public void onConditionDisable() {
        conditionMap.values().forEach(SubCondition::onDisable);
    }

}
