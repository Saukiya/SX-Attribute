package github.saukiya.sxattribute.data.attribute.sub.defence;

import github.saukiya.sxattribute.data.attribute.SXAttributeType;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.eventdata.EventData;
import github.saukiya.sxattribute.util.Config;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * 韧性
 *
 * @author Saukiya
 */
public class ToughnessAttribute extends SubAttribute {

    /**
     * double[0] 韧性
     */
    public ToughnessAttribute() {
        super("Toughness", 1, SXAttributeType.OTHER);
    }

    @Override
    public void eventMethod(EventData eventData) {
    }

    @Override
    public String getPlaceholder(Player player, String string) {
        return string.equalsIgnoreCase("Toughness") ? getDf().format(getAttributes()[0]) : null;
    }

    @Override
    public List<String> getPlaceholders() {
        return Collections.singletonList("Toughness");
    }

    @Override
    public boolean loadAttribute(String lore) {
        if (lore.contains(Config.getConfig().getString(Config.NAME_TOUGHNESS))) {
            getAttributes()[0] += Double.valueOf(getNumber(lore));
            return true;
        }
        return false;
    }

    @Override
    public double getValue() {
        return getAttributes()[0] * Config.getConfig().getInt(Config.VALUE_TOUGHNESS);
    }
}
