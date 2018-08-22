package github.saukiya.sxattribute.data.condition.sub;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.condition.SubCondition;
import github.saukiya.sxattribute.util.Config;
import github.saukiya.sxattribute.util.Message;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.text.ParseException;

/**
 * @author Saukiya
 */
public class ExpiryTimeCondition extends SubCondition {

    public ExpiryTimeCondition() {
        super("ExpiryTime");
    }

    @Override
    public boolean determine(LivingEntity entity, ItemStack item, String lore) {
        if (lore.contains(Config.getConfig().getString(Config.NAME_EXPIRY_TIME))) {
            String timeStr = getTime(lore);
            try {
                if (System.currentTimeMillis() > SXAttribute.getSdf().parse(timeStr).getTime()) {
                    if (item != null) Message.send(entity, Message.PLAYER__OVERDUE_ITEM, getItemName(item), timeStr);
                    return true;
                }
            } catch (ParseException e) {
                Location loc = entity.getLocation();
                Bukkit.getConsoleSender().sendMessage("[" + SXAttribute.getPluginName() + "] §cItem §4" + getItemName(item) + "§c Time Format Error: §4" + lore);
                Bukkit.getConsoleSender().sendMessage("[" + SXAttribute.getPluginName() + "] §cEntity: §4" + (entity.getCustomName() != null ? entity.getCustomName() : entity.getName()) + "§c To Location: §4[" + loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "]");
                return true;
            }
        }
        return false;
    }
}
