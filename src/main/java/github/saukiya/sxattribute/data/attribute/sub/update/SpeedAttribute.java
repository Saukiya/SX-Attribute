package github.saukiya.sxattribute.data.attribute.sub.update;

import github.saukiya.sxattribute.data.attribute.SXAttributeType;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.eventdata.EventData;
import github.saukiya.sxattribute.data.eventdata.sub.PlayerEventData;
import github.saukiya.sxattribute.util.Config;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * @author Saukiya
 */
public class SpeedAttribute extends SubAttribute {

    /**
     * double[0] 移动速度
     */
    public SpeedAttribute() {
        super("Speed", 1, SXAttributeType.UPDATE);
    }

    @Override
    public void eventMethod(EventData eventData) {
        if (eventData instanceof PlayerEventData) {
            Player player = ((PlayerEventData) eventData).getPlayer();
            player.setWalkSpeed((float) (getAttributes()[0] / 500D));
        }
    }

    @Override
    public String getPlaceholder(String string) {
        return string.equalsIgnoreCase("Speed") ? getDf().format(getAttributes()[0]) : null;
    }

    @Override
    public List<String> getPlaceholders() {
        return Collections.singletonList("Speed");
    }

    @Override
    public boolean loadAttribute(String lore) {
        if (lore.contains(Config.getConfig().getString(Config.NAME_SPEED))) {
            getAttributes()[0] += Double.valueOf(getNumber(lore));
            return true;
        }
        return false;
    }

    @Override
    public double getValue() {
        return getAttributes()[0] * Config.getConfig().getInt(Config.VALUE_SPEED);
    }
}
