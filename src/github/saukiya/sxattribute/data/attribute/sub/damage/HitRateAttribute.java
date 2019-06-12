package github.saukiya.sxattribute.data.attribute.sub.damage;

import github.saukiya.sxattribute.data.attribute.SXAttributeType;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.eventdata.EventData;
import github.saukiya.sxattribute.util.Config;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * 命中
 *
 * @author Saukiya
 */
public class HitRateAttribute extends SubAttribute {

    /**
     * double[0] 命中几率
     */
    public HitRateAttribute() {
        super("HitRate", 1, SXAttributeType.OTHER);
    }

    @Override
    public void eventMethod(EventData eventData) {
    }

    @Override
    public String getPlaceholder(Player player, String string) {
        return string.equalsIgnoreCase("HitRate") ? getDf().format(getAttributes()[0]) : null;
    }

    @Override
    public List<String> getPlaceholders() {
        return Collections.singletonList("HitRate");
    }

    @Override
    public boolean loadAttribute(String lore) {
        if (lore.contains(Config.getConfig().getString(Config.NAME_HIT_RATE))) {
            getAttributes()[0] += Double.valueOf(getNumber(lore));
            return true;
        }
        return false;
    }

    @Override
    public double getValue() {
        return getAttributes()[0] * Config.getConfig().getInt(Config.VALUE_HIT_RATE);
    }
}
