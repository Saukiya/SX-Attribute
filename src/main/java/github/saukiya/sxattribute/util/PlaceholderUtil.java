package github.saukiya.sxattribute.util;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.SXAttributeData;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxitem.SXItem;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PlaceholderUtil {

    static boolean enabled;

    static Map<UUID, SXAttributeData> dataMap = new HashMap<>();

    public static void setup() {
        Bukkit.getScheduler().runTaskTimer(SXAttribute.getInst(), ()-> dataMap.clear(), 15, 15);
        try {
            new Placeholder().register();
        } catch (Exception e) {
            SXItem.getInst().getLogger().warning("Placeholder Error");
            enabled = false;
        }
    }

    public static String onPlaceholderRequest(Player player, String string, SXAttributeData attributeData) {
        if (string.equals("Money") && SXAttribute.isVault())
            return SXAttribute.getDf().format(MoneyUtil.get(player));
        if (string.equals("CombatPower")) return SXAttribute.getDf().format(attributeData.getCombatPower());
        for (SubAttribute attribute : SubAttribute.getAttributes()) {
            Object obj = attribute.getPlaceholder(attributeData.getValues(attribute), player, string);
            if (obj != null) {
                return obj instanceof Double ? SXAttribute.getDf().format(obj) : obj.toString();
            }
        }
        return "§cN/A - " + string;
    }

    static class Placeholder extends PlaceholderExpansion {

        @Override
        public String getIdentifier() {
            return "sx";
        }

        @Override
        public String getAuthor() {
            return SXAttribute.getInst().getDescription().getAuthors().toString();
        }

        @Override
        public String getVersion() {
            return SXAttribute.getInst().getDescription().getVersion();
        }

        @Override
        public String onPlaceholderRequest(Player player, String string) {
            return PlaceholderUtil.onPlaceholderRequest(player, string, dataMap.computeIfAbsent(player.getUniqueId(), k -> SXAttribute.getAttributeManager().getEntityData(player)));
        }
    }
}
