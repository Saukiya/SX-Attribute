package github.saukiya.sxattribute.data.attribute.sub.other;

import github.saukiya.sxattribute.data.attribute.SXAttributeType;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.eventdata.EventData;
import github.saukiya.sxattribute.util.Config;

import java.util.Collections;
import java.util.List;

/**
 * 经验加成
 * @author Saukiya
 */
public class ExpAdditionAttribute extends SubAttribute {

    /**
     * double[] 经验加成
     */
    public ExpAdditionAttribute() {
        super("ExpAddition", 1, SXAttributeType.OTHER);
    }

    @Override
    public void eventMethod(EventData eventData) {

    }

    @Override
    public String getPlaceholder(String string) {
        return string.equalsIgnoreCase("ExpAddition") ? getDf().format(getAttributes()[0]) : null;
    }

    @Override
    public List<String> getPlaceholders() {
        return Collections.singletonList("ExpAddition");
    }

    @Override
    public boolean loadAttribute(String lore) {
        if (lore.contains(Config.getConfig().getString(Config.NAME_EXP_ADDITION))) {
            getAttributes()[0] += Double.valueOf(getNumber(lore));
            return true;
        }
        return false;
    }

    @Override
    public double getValue() {
        return getAttributes()[0] * Config.getConfig().getInt(Config.VALUE_EXP_ADDITION);
    }
}
