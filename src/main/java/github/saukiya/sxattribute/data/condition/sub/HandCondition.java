package github.saukiya.sxattribute.data.condition.sub;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.condition.SXConditionType;
import github.saukiya.sxattribute.data.condition.SubCondition;
import github.saukiya.sxattribute.util.Config;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

/**
 * 手持免疫
 * @author Saukiya
 */
public class HandCondition extends SubCondition {

    private final SXAttribute plugin;

    public HandCondition(SXAttribute plugin) {
        super("Hand", SXConditionType.HAND);
        this.plugin = plugin;
    }

    @Override
    public boolean determine(LivingEntity entity, ItemStack item, String lore) {
        return Config.getConfig().getStringList(Config.NAME_ARMOR).stream().anyMatch(lore::contains) || !plugin.getRegisterSlotManager().getRegisterSlotMap().values().stream().noneMatch(registerSlot -> lore.contains(registerSlot.getName()));
    }
}
