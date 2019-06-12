package github.saukiya.sxattribute.data.condition.sub;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.condition.SXConditionReturnType;
import github.saukiya.sxattribute.data.condition.SubCondition;
import github.saukiya.sxattribute.util.Config;
import github.saukiya.sxattribute.util.Message;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * 限制职业(权限)
 *
 * @author Saukiya
 */
public class RoleCondition extends SubCondition {

    public RoleCondition() {
        super("Role");
    }

    @Override
    public SXConditionReturnType determine(LivingEntity entity, ItemStack item, String lore) {
        if (lore.contains(Config.getConfig().getString(Config.NAME_ROLE)) && entity instanceof Player && !entity.hasPermission(SXAttribute.getPluginName() + "." + getText(lore))) {
            if (item != null) Message.send(entity, Message.PLAYER__NO_ROLE, getItemName(item));
            return SXConditionReturnType.ITEM;
        }
        return SXConditionReturnType.NULL;
    }

    /**
     * 获取lore内的中文 (职业)
     *
     * @param lore String
     * @return String 中文
     */
    private String getText(String lore) {
        String str = lore.replaceAll("[^\u0391-\uFFE5]", "");
        if (lore.contains(":") || lore.contains("：")) {
            str = lore.replace("：", ":");
            str = str.replace(str.split(":")[0] + ":", "").replaceAll("[^\u0391-\uFFE5]", "");
        }
        return str;
    }
}
