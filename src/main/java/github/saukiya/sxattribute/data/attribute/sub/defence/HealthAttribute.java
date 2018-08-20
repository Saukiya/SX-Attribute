package github.saukiya.sxattribute.data.attribute.sub.defence;

import github.saukiya.sxattribute.data.attribute.SXAttributeType;
import github.saukiya.sxattribute.data.attribute.SubAttribute;
import github.saukiya.sxattribute.data.eventdata.EventData;
import github.saukiya.sxattribute.data.eventdata.sub.PlayerEventData;
import github.saukiya.sxattribute.util.Config;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * @author Saukiya
 */
public class HealthAttribute extends SubAttribute {

    /**
     * double[0] 生命值
     */
    public HealthAttribute() {
        super("Health", 1, SXAttributeType.UPDATE);
    }

    @Override
    public void eventMethod(EventData eventData) {
        if (eventData instanceof PlayerEventData) {
            Player player = ((PlayerEventData) eventData).getPlayer();
            if (player.getHealth() > getAttributes()[0]) player.setHealth(getAttributes()[0]);
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(getAttributes()[0]);
            int healthScale = Config.getConfig().getInt(Config.HEALTH_SCALED_VALUE);
            if (Config.isHealthScaled() && healthScale < player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue()) {
                player.setHealthScaled(true);
                player.setHealthScale(Config.getConfig().getInt(Config.HEALTH_SCALED_VALUE));
            } else {
                player.setHealthScaled(false);
            }
        }
    }

    @Override
    public String getPlaceholder(String string) {
        return string.equalsIgnoreCase("Health") ? getDf().format(getAttributes()[0]) : null;
    }

    @Override
    public List<String> getPlaceholders() {
        return Collections.singletonList("Health");
    }

    @Override
    public boolean loadAttribute(String lore) {
        if (lore.contains(Config.getConfig().getString(Config.NAME_HEALTH))) {
            getAttributes()[0] += Double.valueOf(getNumber(lore));
            return true;
        }
        return false;
    }

    @Override
    public void correct() {
        if (getAttributes()[0] <= 0) getAttributes()[1] = 1D;
    }

    @Override
    public double getValue() {
        return getAttributes()[0] * Config.getConfig().getInt(Config.VALUE_HEALTH);
    }
}
