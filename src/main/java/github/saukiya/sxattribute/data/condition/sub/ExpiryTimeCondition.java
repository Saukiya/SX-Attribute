package github.saukiya.sxattribute.data.condition.sub;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.condition.SXConditionReturnType;
import github.saukiya.sxattribute.data.condition.SubCondition;
import github.saukiya.sxattribute.util.Config;
import github.saukiya.sxattribute.util.Message;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;


/**
 * 限制时间
 *
 * @author Saukiya
 */
public class ExpiryTimeCondition extends SubCondition {

    public ExpiryTimeCondition() {
        super("ExpiryTime");
    }

    @Override
    public SXConditionReturnType determine(LivingEntity entity, ItemStack item, String lore) {
        if (lore.contains(Config.getConfig().getString(Config.NAME_EXPIRY_TIME))) {
            String timeStr = getTime(lore);
            try {
                if (System.currentTimeMillis() > SXAttribute.getSdf().parse(timeStr).getTime()) {
                    if (item != null) Message.send(entity, Message.PLAYER__OVERDUE_ITEM, getItemName(item), timeStr);
                    return SXConditionReturnType.ITEM;
                }
            } catch (Exception e) {
                Bukkit.getConsoleSender().sendMessage("[" + SXAttribute.getPluginName() + "] §cException: §4" + e.getClass().getSimpleName());
                Bukkit.getConsoleSender().sendMessage("[" + SXAttribute.getPluginName() + "] §cItem §4" + getItemName(item) + "§c Time Format Error: §r'§4" + lore + "§r' §7-> §r'§c" + timeStr + "§r'");
                if (entity != null) {
                    Location loc = entity.getLocation();
                    Bukkit.getConsoleSender().sendMessage("[" + SXAttribute.getPluginName() + "] §cEntity: §4" + (entity.getCustomName() != null ? entity.getCustomName() : entity.getName()) + "§c To Location: §4[" + loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() + "]");
                }
                return SXConditionReturnType.ITEM;
            }
        }
        return SXConditionReturnType.NULL;
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
