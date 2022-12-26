package github.saukiya.sxattribute.data.condition.sub;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.condition.SubCondition;
import github.saukiya.sxattribute.util.Config;
import github.saukiya.sxattribute.util.Message;
import github.saukiya.sxattribute.util.TimeUtil;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;


/**
 * 限制时间
 *
 * @author Saukiya
 */
public class ExpiryTime extends SubCondition {

    public ExpiryTime() {
        super(SXAttribute.getInst());
    }

    @Override
    public boolean determine(LivingEntity entity, ItemStack item, String lore) {
        if (lore.contains(Config.getConfig().getString(Config.NAME_EXPIRY_TIME))) {
            String timeStr = getTime(lore);
            try {
                if (System.currentTimeMillis() > TimeUtil.getSdf().parseFormLong(timeStr)) {
                    if (item != null) Message.send(entity, Message.PLAYER__OVERDUE_ITEM, getItemName(item), timeStr);
                    return false;
                }
            } catch (Exception e) {
                SXAttribute.getInst().getLogger().warning("Exception: " + e.getClass().getSimpleName());
                SXAttribute.getInst().getLogger().warning("Item " + getItemName(item) + " Time Format Error: '" + lore + "' -> '" + timeStr + "'");
                if (entity != null) {
                    Location loc = entity.getLocation();
                    SXAttribute.getInst().getLogger().info("Entity: " + (entity.getCustomName() != null ? entity.getCustomName() : entity.getName()) + " To Location: [" + loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() + "]");
                }
                return false;
            }
        }
        return true;
    }

    /**
     * 获取lore内的时间
     *
     * @param lore String
     * @return String
     */
    private String getTime(String lore) {
        String str = lore.replace(Config.getConfig().getString(Config.NAME_EXPIRY_TIME), "").replaceAll("§+[a-z0-9]", "");
        if (str.contains(": ") || str.contains("： ")) {
            str = str.replace("： ", ": ");
            str = str.replace(str.split(":")[0] + ": ", "");
            return str;
        }
        return str;
    }
}