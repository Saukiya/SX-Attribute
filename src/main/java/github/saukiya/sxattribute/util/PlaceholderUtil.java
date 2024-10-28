package github.saukiya.sxattribute.util;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.SXAttributeData;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.tools.helper.PlaceholderHelper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PlaceholderUtil {
    
    private static PlaceholderUtil inst = new PlaceholderUtil();

    static Map<UUID, SXAttributeData> dataMap = new HashMap<>();
    
    static BukkitTask task;

    public static void setup() {
        PlaceholderHelper.setup(SXAttribute.getInst(), "sx", inst::onPlaceholderRequest);
        if (task != null) {
            Bukkit.getScheduler().cancelTask(task.getTaskId());
        }
        task = Bukkit.getScheduler().runTaskTimer(SXAttribute.getInst(), ()-> dataMap.clear(), 20, 20);
    }

    public String onPlaceholderRequest(Player player, String string) {
        return onPlaceholderRequest(player, string, dataMap.computeIfAbsent(player.getUniqueId(), k -> SXAttribute.getAttributeManager().getEntityData(player)));
    }

    public static String onPlaceholderRequest(Player player, String string, SXAttributeData attributeData) {
        if (string.equals("Money") && SXAttribute.isVault()) {
            return SXAttribute.getDf().format(MoneyUtil.get(player));
        }
        if (string.equals("CombatPower")) {
            return SXAttribute.getDf().format(attributeData.getCombatPower());
        }
        for (SubAttribute attribute : SubAttribute.getAttributes()) {
            Object obj = attribute.getPlaceholder(attributeData.getValues(attribute), player, string);
            if (obj != null) {
                return obj instanceof Double ? SXAttribute.getDf().format(obj) : obj.toString();
            }
        }
        return "§cN/A - " + string;
    }
}
