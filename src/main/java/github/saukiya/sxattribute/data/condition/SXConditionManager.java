package github.saukiya.sxattribute.data.condition;

import github.saukiya.sxattribute.SXAttribute;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;

import java.util.Collections;
import java.util.List;

/**
 * 条件管理器
 *
 * @author Saukiya
 */
public class SXConditionManager implements Listener {

    public SXConditionManager() {
        Bukkit.getPluginManager().registerEvents(this, SXAttribute.getInst());
        int size = SubCondition.getConditions().size();
        Collections.sort(SubCondition.getConditions());
        for (int i = 0; i < size; i++) {
            SubCondition.getConditions().get(i).setPriority(i).onEnable();
        }
        SXAttribute.getInst().getLogger().info("Loaded " + size + " Condition");
    }


    @EventHandler
    public void onSubAttributePluginEnableEvent(PluginEnableEvent event) {
        for (SubCondition condition : SubCondition.getConditions()) {
            if (condition.getPlugin().equals(event.getPlugin()) && condition instanceof Listener) {
                Bukkit.getPluginManager().registerEvents((Listener) condition, condition.getPlugin());
            }
        }
    }

    public void onConditionDisable() {
        for (SubCondition condition : SubCondition.getConditions()) {
            condition.onDisable();
        }
    }

    /**
     * 判断物品是否符合使用条件
     *
     * @param entity 实体
     * @param list   识别列表
     * @param type   物品所处位置
     * @return boolean
     */
    public boolean isUse(LivingEntity entity, EquipmentType type, List<String> list) {
        for (String lore : list) {
            for (SubCondition condition : SubCondition.getConditions()) {
                if (condition.containsType(type)) {
                    if (!condition.determine(entity, null, lore)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}