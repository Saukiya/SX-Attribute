package github.saukiya.sxattribute.util;

import github.saukiya.sxattribute.SXAttribute;
import github.saukiya.sxattribute.data.attribute.SXAttributeData;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import me.clip.placeholderapi.external.EZPlaceholderHook;
import org.bukkit.entity.Player;

public class Placeholders extends EZPlaceholderHook {

    private final SXAttribute plugin;

    public Placeholders(SXAttribute plugin) {
        super(plugin, "sx");
        this.plugin = plugin;
        this.hook();
    }

    @Override
    public String onPlaceholderRequest(Player player, String string) {
        SXAttributeData attributeData = plugin.getAttributeManager().getEntityData(player);
        if (string.equalsIgnoreCase("Money") && SXAttribute.isVault())
            return SXAttribute.getDf().format(MoneyUtil.get(player));
        if (string.equalsIgnoreCase("Value")) return SXAttribute.getDf().format(attributeData.getValue());
        for (SubAttribute attribute : attributeData.getAttributeMap().values()) {
            String str = attribute.getPlaceholder(player, string);
            if (str != null) return str;
        }
        return "N/A";
    }

}
