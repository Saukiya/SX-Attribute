package github.saukiya.sxattribute.data.condition.sub;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.condition.SubCondition;
import github.saukiya.sxattribute.util.Config;
import github.saukiya.sxattribute.util.Message;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @author Saukiya
 */
public class RoleCondition extends SubCondition {

    public RoleCondition() {
        super("Role");
    }

    @Override
    public boolean determine(LivingEntity entity, ItemStack item, String lore) {
        if (lore.contains(Config.getConfig().getString(Config.NAME_ROLE)) && entity instanceof Player && !entity.hasPermission(SXAttribute.getPluginName() + "." + getText(lore))) {
            if (item != null) Message.send(entity, Message.PLAYER__NO_ROLE, getItemName(item));
            return true;
        }
        return false;
    }
}
