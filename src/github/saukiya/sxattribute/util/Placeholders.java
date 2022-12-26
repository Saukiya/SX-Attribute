package github.saukiya.sxattribute.util;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.SXAttributeData;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import me.clip.placeholderapi.external.EZPlaceholderHook;
import org.bukkit.entity.Player;

public class Placeholders {

    public Placeholders() {
        new Hook().hook();
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

    public class Hook extends EZPlaceholderHook {

        public Hook() {
            super(SXAttribute.getInst(), "sx");
        }

        @Override
        public String onPlaceholderRequest(Player player, String string) {
            return Placeholders.onPlaceholderRequest(player, string, SXAttribute.getAttributeManager().getEntityData(player));
        }
    }
}
